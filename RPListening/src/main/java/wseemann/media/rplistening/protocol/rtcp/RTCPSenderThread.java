/*
 * RPListening: An Open Source desktop client for Roku private listening.
 * 
 * Copyright (C) 2021 William Seemann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package wseemann.media.rplistening.protocol.rtcp;

import java.net.*;
import java.util.*;

import wseemann.media.rplistening.protocol.PrivateListeningSession;
import wseemann.media.rplistening.protocol.Source;
import wseemann.media.rplistening.protocol.utils.PacketUtils;
import wseemann.media.rplistening.protocol.utils.RTCPConstants;
import wseemann.media.rplistening.utils.Log;

/*
*   This class encapsulates the functionality to construct and send out an RTCP Packet. This
*   class provides a seperate thread to send out RTCP Packets. The thread is put to sleep
*   for a specified amount of time ( as calculated using various RTCP parameters and reception
*   feedback). When the thread moves out of the blocked (or sleep) state, it determines what
*   kind of a RTCP Packets needs to be send out , constructs the appropriate RTCP packets and
*   sends them
*/

public class RTCPSenderThread extends Thread {
	
	private static final String TAG = "RTCPSenderThread";
	
	boolean sentAppPacketOne = false;
	boolean sentAppPacketTwo = false;

	/**
	 * Sender Port for RTCP Packets
	 */
	private final int m_SendFromPort;

	/**
	 * Sender Address for RTCP Packets
	 */
	private final InetAddress m_InetAddress;

	/**
	 * Multicast Socket for sending RTCP
	 */
	private DatagramSocket m_RTCPSenderSocket;

	/**
	 * Multicast Port for RTCP Packets
	 */
	private final int m_MulticastRTCPPort;

	/**
	 * Flag used to determine when to terminate after sending a BYE
	 */
	private boolean WaitingForByeBackoff = false;

	/**
	 * Initialize Random Number Generator
	 */
	Random rnd = new Random();

	/**
	 * Constructor for the class. Takes in a TCP/IP Address and port numbers for
	 * sending and receiving RTCP Packets.
	 * 
	 * @param MulticastGroupIPAddress Dotted representation of the Multicast
	 *                                address.
	 * @param RTCPSendFromPort        Port used to send RTCP Packets
	 * @param RTCPGroupPort           Port for Multicast group (for receiving RTP
	 *                                Packets).
	 *
	 */

	public RTCPSenderThread(InetAddress MulticastGroupIPAddress, int RTCPSendFromPort, int RTCPGroupPort) {
		// TODO: Perform sanity check on group address and port number - WA
		m_InetAddress = MulticastGroupIPAddress;
		m_MulticastRTCPPort = RTCPGroupPort;
		m_SendFromPort = RTCPSendFromPort;

	}

	/**
	 * Starts the RTCPSender Thread
	 */
	public void run() {
		StartRTCPSender();
	}

	/**
	 * 
	 * Initializes the thread by creating a multicast socket on a specified address
	 * and a port It manages the thread initialization and blocking of the thread
	 * (i.e putting it to sleep for a specified amount of time) This function also
	 * implements the BYE backoff algorithm with Option B. The BYE Backoff Algorithm
	 * is used in order to avoid a flood of BYE packets when many users leave the
	 * system
	 *
	 * Note : if a client has never sent an RTP or RTCP Packet, it will not send a
	 * BYE Packet when it leaves the group. For More Information : See the Flowchart
	 *
	 */
	public void setRTCPSenderSocket(DatagramSocket RTCPSenderSocket) {
		this.m_RTCPSenderSocket = RTCPSenderSocket;
	}
	
	public void StartRTCPSender() {
		Log.d(TAG, "RTCP Sender Thread started ");

		Log.d(TAG, "RTCP Group: " + m_InetAddress.toString() + ":" + m_MulticastRTCPPort);
		Log.d(TAG, "RTCP Local port for sending: " + m_SendFromPort);

		try {
			// flag terminates the endless while loop
			boolean terminate = false;

			while (!isInterrupted() /*|| !terminate*/) {
				if (!sentAppPacketOne) {
					byte CompoundRTCPPacket[] = AssembleRTCPPacket();
					SendPacket(CompoundRTCPPacket);
					CompoundRTCPPacket = AssembleRTCPPacket();
					SendPacket(CompoundRTCPPacket);
					CompoundRTCPPacket = AssembleRTCPPacket();
					SendPacket(CompoundRTCPPacket);
				}

				// Update T and Td (Session level variables)
				PrivateListeningSession.CalculateInterval();

				Log.d(TAG, "RTCP wait");

				// If inturrepted during this sleep time, continue with execution
				int sleepResult = SleepTillInterrupted(PrivateListeningSession.T);

				if (sleepResult == 0) {
					// Sleep was interrupted, this only occurs if thread
					// was terminated to indicate a request to send a BYE packet
					WaitingForByeBackoff = true;
					PrivateListeningSession.IsByeRequested = true;
				}

				// See if it is the right time to send a RTCP packet or reschedule {{A True}}
				if ((PrivateListeningSession.TimeOfLastRTCPSent + PrivateListeningSession.T) <= PrivateListeningSession.CurrentTime()) {
					// We know that it is time to send a RTCP packet, is it a BYE packet {{B True}}
					if ((PrivateListeningSession.IsByeRequested && WaitingForByeBackoff)) {
						// If it is bye then did we ever sent anything {{C True}}
						if (PrivateListeningSession.TimeOfLastRTCPSent > 0 && PrivateListeningSession.timeOfLastRTPSent > 0) {
							// ** BYE Backoff Algorithm **
							// Yes, we did send something, so we need to send this RTCP BYE
							// but first remove all sources from the table
							PrivateListeningSession.RemoveAllSources();

							// We are not active senders anymore
							PrivateListeningSession.GetMySource().setActiveSender(false);
							PrivateListeningSession.TimeOfLastRTCPSent = PrivateListeningSession.CurrentTime();
						} else // We never sent anything and we have to quit :( do not send BYE {{C False}}
						{
							terminate = true;
						}
					} else // {{B False}}
					{
						byte CompoundRTCPPacket[] = AssembleRTCPPacket();
						SendPacket(CompoundRTCPPacket);

						// If the packet just sent was a BYE packet, then its time to terminate.
						if (PrivateListeningSession.IsByeRequested && !WaitingForByeBackoff) // {{D True}}
						{
							// We have sent a BYE packet, so its time to terminate
							terminate = true;
						} else // {{D False}}
						{
							PrivateListeningSession.TimeOfLastRTCPSent = PrivateListeningSession.CurrentTime();
						}

					}
				}

				WaitingForByeBackoff = false;
				PrivateListeningSession.tn = PrivateListeningSession.CurrentTime() + PrivateListeningSession.T;
				PrivateListeningSession.pmembers = PrivateListeningSession.GetNumberOfMembers();
			}
		} catch (Exception ex) {
			Log.d(TAG, ex.getMessage());
		} finally {
			m_RTCPSenderSocket.close();
		}
	}

	/**
	 * Provides a wrapper around java sleep to handle exceptions in case when
	 * session wants to quit. Returns 0 is sleep was interrupted and 1 if all the
	 * sleep time was consumed.
	 *
	 * @param Seconds No. of seconds to sleep
	 * @return 0 if interrupted, 1 if the sleep progressed normally
	 * @throws InterruptedException 
	 */

	int SleepTillInterrupted(double Seconds) throws InterruptedException {
		sleep((long) Seconds * 1000);
		Log.d(TAG, "In sleep function after sleep.");

		Log.d(TAG, "Just woke up after try");
		return (1);
	}

	/**
	 * Top Level Function to assemble a compound RTCP Packet. This function
	 * determines what kind of RTCP Packet needs to be created and sent out. If this
	 * source is a sender (ie. generating RTP Packets), then a Sender Report (SR) is
	 * sent out otherwise a Receiver Report (RR) is sent out. An SDES Packet is
	 * appended to the SR or RR PAcket. If a BYE was requested by the application ,
	 * a BYE PAcket is sent out.
	 *
	 *
	 * @return The Compound RTCP Packet
	 */

	public byte[] AssembleRTCPPacket() {
		byte packet[] = new byte[0];

		//
		// Generate an SR packet if I am an active sender and did send an
		// RTP packet since last time I sent an RTCP packet.
		//
		if (!sentAppPacketOne) {
			packet = PacketUtils.Append(packet, AssembleRTCPVDLYAppPacket());
			sentAppPacketOne = true;
			return packet;
		} else if (!sentAppPacketTwo) {
			packet = PacketUtils.Append(packet, AssembleRTCPCVERAppPacket());
			sentAppPacketTwo = true;
			return packet;
		} else
			packet = PacketUtils.Append(packet, AssembleRTCPReceiverReportPacket());

		// Append a BYE packet if necessary
		// if ( Session.IsByeRequested )
		// packet = PacketUtils.Append ( packet, AssembleRTCPByePacket( "Quitting") );

		return packet;
	}

	/*****************************************************************************************
	 *
	 * Functions to assemble RTCP packet components.
	 *
	 *******************************************************************************************/

	/**
	 * Creates a Receiver Report RTCP Packet.
	 *
	 *
	 * @return byte[] The Receiver Report Packet
	 */

	private byte[] AssembleRTCPReceiverReportPacket() {
		/*
		 * 0 1 2 3 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ |V=2|P| RC
		 * | PT=RR=201 | length | header
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | SSRC of
		 * sender | +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+ |
		 * SSRC_1 (SSRC of first source) | report
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ block |
		 * fraction lost | cumulative number of packets lost | 1
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | extended
		 * highest sequence number received |
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ |
		 * interarrival jitter |
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | last SR
		 * (LSR) | +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ |
		 * delay since last SR (DLSR) |
		 * +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+ | SSRC_2
		 * (SSRC of second source) | report
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ block : ...
		 * : 2 +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+ |
		 * profile-specific extensions |
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
		 * 
		 * 
		 */
		final int FIXED_HEADER_SIZE = 4; // 4 bytes

		// construct the first byte containing V, P and RC
		byte V_P_RC;
		V_P_RC = (byte) ((RTCPConstants.VERSION << 6) | (RTCPConstants.PADDING << 5) | (0x00) // take only the right
																								// most 5 bytes i.e.
																								// 00011111 = 0x1F
		);

		// SSRC of sender
		byte ss[] = PacketUtils.LongToBytes(PrivateListeningSession.SSRC, 4);

		// Payload Type = RR
		byte PT[] = PacketUtils.LongToBytes((long) RTCPConstants.RTCP_RR, 1);

		byte ReceptionReportBlocks[] = new byte[0];

		ReceptionReportBlocks = PacketUtils.Append(ReceptionReportBlocks, AssembleRTCPReceptionReport());

		// Each reception report is 24 bytes, so calculate the number of sources in the
		// reception report block and update the reception block count in the header
		byte ReceptionReports = (byte) (ReceptionReportBlocks.length / 24);

		// Reset the RC to reflect the number of reception report blocks
		V_P_RC = (byte) (V_P_RC | (byte) (ReceptionReports & 0x1F));

		byte length[] = PacketUtils.LongToBytes((FIXED_HEADER_SIZE + ss.length + ReceptionReportBlocks.length) / 4 - 1,
				2);

		byte RRPacket[] = new byte[1];
		RRPacket[0] = V_P_RC;
		RRPacket = PacketUtils.Append(RRPacket, PT);
		RRPacket = PacketUtils.Append(RRPacket, length);
		RRPacket = PacketUtils.Append(RRPacket, ss);
		RRPacket = PacketUtils.Append(RRPacket, ReceptionReportBlocks);

		Log.d(TAG, "RRPacket" + RRPacket[1]);
		return hexStringToByteArray("81c9000700000000000000000000000000000000000000000000000000000000");
	}

	/* s must be an even-length string. */
	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	private byte[] AssembleRTCPVDLYAppPacket() {
		/*
		 * 0 1 2 3 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ |V=2|P| RC
		 * | PT=RR=201 | length | header
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | SSRC of
		 * sender | +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+ |
		 * SSRC_1 (SSRC of first source) | report
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ block |
		 * fraction lost | cumulative number of packets lost | 1
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | extended
		 * highest sequence number received |
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ |
		 * interarrival jitter |
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | last SR
		 * (LSR) | +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ |
		 * delay since last SR (DLSR) |
		 * +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+ | SSRC_2
		 * (SSRC of second source) | report
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ block : ...
		 * : 2 +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+ |
		 * profile-specific extensions |
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
		 * 
		 * 
		 */
		// construct the first byte containing V, P and RC
		byte V_P_RC;
		V_P_RC = (byte) ((RTCPConstants.VERSION << 6) | (RTCPConstants.PADDING << 5) | (0x00) // take only the right
																								// most 5 bytes i.e.
																								// 00011111 = 0x1F
		);

		// Payload Type = RR
		byte PT[] = PacketUtils.LongToBytes((long) RTCPConstants.RTCP_APP, 1);

		byte length[] = PacketUtils.LongToBytes(3, 2);
		byte identifier[] = PacketUtils.LongToBytes(0, 4);

		byte RRPacket[] = new byte[1];
		RRPacket[0] = V_P_RC;
		RRPacket = PacketUtils.Append(RRPacket, PT);
		RRPacket = PacketUtils.Append(RRPacket, length);
		RRPacket = PacketUtils.Append(RRPacket, identifier);
		RRPacket = PacketUtils.Append(RRPacket, "VDLY".getBytes());
		RRPacket = PacketUtils.Append(RRPacket, PacketUtils.LongToBytes(500000, 4));

		return RRPacket;
	}

	private byte[] AssembleRTCPCVERAppPacket() {
		/*
		 * 0 1 2 3 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ |V=2|P| RC
		 * | PT=RR=201 | length | header
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | SSRC of
		 * sender | +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+ |
		 * SSRC_1 (SSRC of first source) | report
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ block |
		 * fraction lost | cumulative number of packets lost | 1
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | extended
		 * highest sequence number received |
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ |
		 * interarrival jitter |
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | last SR
		 * (LSR) | +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ |
		 * delay since last SR (DLSR) |
		 * +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+ | SSRC_2
		 * (SSRC of second source) | report
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ block : ...
		 * : 2 +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+ |
		 * profile-specific extensions |
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
		 * 
		 * 
		 */
		// construct the first byte containing V, P and RC
		byte V_P_RC;
		V_P_RC = (byte) ((RTCPConstants.VERSION << 6) | (RTCPConstants.PADDING << 5) | (0x00) // take only the right
																								// most 5 bytes i.e.
																								// 00011111 = 0x1F
		);

		// Payload Type = RR
		byte PT[] = PacketUtils.LongToBytes((long) RTCPConstants.RTCP_APP, 1);

		byte length[] = PacketUtils.LongToBytes(3, 2);
		byte identifier[] = PacketUtils.LongToBytes(0, 4);

		byte RRPacket[] = new byte[1];
		RRPacket[0] = V_P_RC;
		RRPacket = PacketUtils.Append(RRPacket, PT);
		RRPacket = PacketUtils.Append(RRPacket, length);
		RRPacket = PacketUtils.Append(RRPacket, identifier);
		RRPacket = PacketUtils.Append(RRPacket, "CVER".getBytes());
		RRPacket = PacketUtils.Append(RRPacket, PacketUtils.LongToBytes(808464434, 4));

		return RRPacket;
	}

	/**
	 * Creates the Reception reports by determining which source need to be included
	 * and makes calls to AssembleRTCPReceptionReportBlock function to generate the
	 * individual blocks. The function returns the fixed length RTCP Sender Info (
	 * 5*32 bits or 20 bytes ).
	 *
	 * @return The RTCP Reception Report Blocks
	 */

	private byte[] AssembleRTCPReceptionReport() {
		byte ReportBlock[] = new byte[0];

		// Keeps track of how many sender report blocks are generated. Make sure
		// that no more than 31 blocks are generated.
		int ReceptionReportBlocks = 0;

		Enumeration ActiveSenderCollection = PrivateListeningSession.GetSources();

		// Iterate through all the sources and generate packets for those
		// that are active senders.
		while (ReceptionReportBlocks < 31 && ActiveSenderCollection.hasMoreElements()) {
			Source s = (Source) ActiveSenderCollection.nextElement();

			// Session.outprintln ( "\ns.TimeoflastRTPArrival : " + s.TimeOfLastRTPArrival +
			// "\t"
			// + "Session.TimeOfLastRTCPSent : " + Session.TimeOfLastRTCPSent + "\n" );

			if ((s.getTimeOfLastRTPArrival() > PrivateListeningSession.TimeOfLastRTCPSent) && (s.getSsrc() != PrivateListeningSession.SSRC)) {
				ReportBlock = PacketUtils.Append(ReportBlock, AssembleRTCPReceptionReportBlock(s));
				ReceptionReportBlocks++;
			}

			// TODO : Add logic for more than 31 Recption Reports - AN

		}

		return ReportBlock;
	}

	/**
	 *
	 * Constructs a fixed length RTCP Reception. Report block ( 6*32 bits or 24
	 * bytes ) for a particular source.
	 *
	 * @param Source The source for which this Report is being constructed.
	 * @return The RTCP Reception Report Block
	 *
	 */

	private byte[] AssembleRTCPReceptionReportBlock(Source rtpSource) {
		/*
		 * +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+ | SSRC_1
		 * (SSRC of first source) | report
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ block |
		 * fraction lost | cumulative number of packets lost | 1
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | extended
		 * highest sequence number received |
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ |
		 * interarrival jitter |
		 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | last SR
		 * (LSR) | +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ |
		 * delay since last SR (DLSR) |
		 * +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+
		 * 
		 */

		byte RRBlock[] = new byte[0];

		// Update all the statistics associated with this source
		rtpSource.UpdateStatistics();

		// SSRC_n - source identifier - 32 bits
		byte SSRC[] = PacketUtils.LongToBytes((long) rtpSource.getSsrc(), 4);

		// fraction lost - 8 bits
		byte fraction_lost[] = PacketUtils.LongToBytes((long) rtpSource.getFraction(), 1);

		// cumulative number of packets lost - 24 bits
		byte pkts_lost[] = PacketUtils.LongToBytes((long) rtpSource.getLost(), 3);

		// extended highest sequence number received - 32 bits
		byte last_seq[] = PacketUtils.LongToBytes((long) rtpSource.getLastSeq(), 4);

		// interarrival jitter - 32 bits
		byte jitter[] = PacketUtils.LongToBytes((long) rtpSource.getJitter(), 4);

		// last SR timestamp(LSR) - 32 bits
		byte lst[] = PacketUtils.LongToBytes((long) rtpSource.getLst(), 4);

		// delay since last SR (DLSR) 32 bits
		byte dlsr[] = PacketUtils.LongToBytes((long) rtpSource.getDlsr(), 4);

		RRBlock = PacketUtils.Append(RRBlock, SSRC);
		RRBlock = PacketUtils.Append(RRBlock, fraction_lost);
		RRBlock = PacketUtils.Append(RRBlock, pkts_lost);
		RRBlock = PacketUtils.Append(RRBlock, last_seq);
		RRBlock = PacketUtils.Append(RRBlock, jitter);
		RRBlock = PacketUtils.Append(RRBlock, lst);
		RRBlock = PacketUtils.Append(RRBlock, dlsr);
		// Session.outprintln("fraction_lost" + RRBlock[4]);

		return RRBlock;
	}

	/**
	 * Sends the byte array RTCP packet. Zero return is error condition
	 *
	 * @param packet[] packet to be sent out.
	 * @return 1 for success, 0 for failure.
	 */

	private int SendPacket(byte packet[]) {
		DatagramPacket DGram = new DatagramPacket(packet, packet.length, m_InetAddress, m_MulticastRTCPPort);

		// Set ttl=5 and send
		try {
			m_RTCPSenderSocket.send(DGram); // ,(byte) 5 );
			return (1);
		} catch (java.io.IOException e) {
			System.err.println("Error: While sending the RTCP Packet");
			System.err.println(e);
			return (0);
		}
	}

}
