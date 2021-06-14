package wseemann.media.rplistening.protocol.rtp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

import wseemann.media.rplistening.protocol.PrivateListeningSession;
import wseemann.media.rplistening.protocol.Source;
import wseemann.media.rplistening.utils.Log;

public class RTPReceiverThread extends Thread {

private static String TAG = "RTPThreadHandler";
	
	private boolean startedRTCPSender = false;

	/**
	 * Multicast Port for RTP Packets
	 */
	private int m_mcastPort;

	/**
	 * Sender Address for RTP Packets
	 */
	private InetAddress m_InetAddress;

	/**
	 * Sender Loopback Address for RTP Packets
	 */
	private InetAddress m_loopbackAddress;
	
	/**
	 * Sender Port for RTP Packets
	 */
	private int m_sendPort;

	/**
	 * Multicast Socket for sending RTP
	 */
	private DatagramSocket m_sockSend;
	
	private DatagramSocket RTCPSenderSocket;

	/**
	 * Initialize Random Number Generator
	 */
	static Random RandomNumGenerator = new Random();

	/**
	 * Random Offset -32 bit
	 */
	public static final short RandomOffset = (short) Math.abs(RandomNumGenerator.nextInt() & 0x000000FF);

	/**
	 * RTP Header Length = 12
	 */
	public static final int RTP_PACKET_HEADER_LENGTH = 12;

	/**********************************************************************************************
	 * RTP Header related fields
	 * 
	 **********************************************************************************************/

	/**
	 * payload type decimal 88 (hex 0x58)
	 */
	static byte PayloadType = 0;

	/**
	 * Sequence Number
	 */
	static long sequence_number; // 16 bits

	/**
	 * TimeStamp
	 */
	static long timestamp; // 32 bits

	/**
	 * Constructor for the class. Takes in a TCP/IP Address and a port number. It
	 * initializes a a new multicast socket according to the multicast address and
	 * the port number given
	 *
	 * @param MulticastAddress  Dotted representation of the Multicast address.
	 * @param SendFromLocalPort Port used to send RTP Packets.
	 * @param MulticastPort     Port for Multicast group (for receiving RTP
	 *                          Packets).
	 *
	 *
	 */

	public RTPReceiverThread(InetAddress MulticastAddress, InetAddress loopbackAddress, int SendFromLocalPort, int MulticastPort) {
		m_InetAddress = MulticastAddress;
		m_loopbackAddress = loopbackAddress;
		
		m_mcastPort = MulticastPort;
		m_sendPort = SendFromLocalPort;

		Random rnd = new Random(); // Use time as default seed

		// Start with a random sequence number
		sequence_number = (long) (Math.abs(rnd.nextInt()) & 0x000000FF);
		timestamp = PrivateListeningSession.CurrentTime() + RandomOffset;

		Log.d(TAG, "RTP Session SSRC: " + Long.toHexString(PrivateListeningSession.SSRC));
		Log.d(TAG, " Starting Seq: " + sequence_number);
		/////////////
		// Initialize a Multicast Sender Port to send RTP Packets
		Log.d(TAG, "Openning local port " + m_sendPort + " for sending RTP..");
		try {
			m_sockSend = new DatagramSocket(m_sendPort);
		} catch (SocketException e) {
			System.err.println(e);
		}
		Log.d(TAG, "Successfully openned local port " + m_sendPort);
	}

	/**
	 * Starts the RTPReceiver Thread
	 */

	public void run() {
		StartRTPReceiver();
	}

	/**
	 * Sends test packet (For debugging only).
	 */
	public void StartRTPReceiver() {
		Log.d(TAG, "RTP Thread started ");
		Log.d(TAG, "RTP Group: " + m_InetAddress + "/" + m_mcastPort);

		byte buf[] = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);

		PayloadType = PrivateListeningSession.getPayloadType();

		try {

			RTCPSenderSocket = new DatagramSocket(m_mcastPort);
			DatagramSocket loopbackSocket = new DatagramSocket(5152);
			
			// s.joinGroup ( m_InetAddress );

			while (!Thread.currentThread().isInterrupted()) {
				RTCPSenderSocket.receive(packet);
				
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						try {
							// Create a datagram packet from the RTP byte packet and set ttl and send
							DatagramPacket pkt = new DatagramPacket(packet.getData(), packet.getLength(),
									m_loopbackAddress, 5153);
							loopbackSocket.send(pkt);
						} catch (IOException e) {
							e.printStackTrace();
						}							
					}
            	};
            	Thread thread = new Thread(runnable);
            	thread.start();
				
				if (ValidateRTPPacketHeader(packet.getData())) {
					long SSRC = 0;
					int TimeStamp = 0;
					short SeqNo = 0;
					byte PT = 0;

					PT = (byte) ((buf[1] & 0xff) & 0x7f);
					SeqNo = (short) ((buf[2] << 8) | (buf[3] & 0xff));
					TimeStamp = (((buf[4] & 0xff) << 24) | ((buf[5] & 0xff) << 16) | ((buf[6] & 0xff) << 8)
							| (buf[7] & 0xff));
					SSRC = (((buf[8] & 0xff) << 24) | ((buf[9] & 0xff) << 16) | ((buf[10] & 0xff) << 8)
							| (buf[11] & 0xff));

					Log.d(TAG, "RTP (");
					Log.d(TAG, "ssrc=0x" + Long.toHexString(SSRC) + "\tts=" + TimeStamp + "\tseq=" + SeqNo + "\tpt=" + PT);
					Log.d(TAG, ")");

					// Create a RTPPacket and post it with Session.
					// If there are any interested actionListeners, they will get it.
					RTPPacket rtppkt = new RTPPacket();
					rtppkt.setCSRCCount(0);
					rtppkt.setSequenceNumber(SeqNo);
					rtppkt.setTimestamp(TimeStamp);
					rtppkt.setSSRC(SSRC);

					// the payload is after the fixed 12 byte header
					byte payload[] = new byte[packet.getLength() - RTP_PACKET_HEADER_LENGTH];

					for (int i = 0; i < payload.length; i++)
						payload[i] = buf[i + RTP_PACKET_HEADER_LENGTH];

					rtppkt.setData(payload);
					startRTCPRSender();

					// Get the source corresponding to this SSRC
					Source RTPSource = PrivateListeningSession.GetSource(SSRC);

					// Set teh Active Sender Property to true
					RTPSource.setActiveSender(true);

					// Set the time of last RTP Arrival
					RTPSource.setTimeOfLastRTPArrival(PrivateListeningSession.tc = PrivateListeningSession.CurrentTime());

					// Update the sequence number
					RTPSource.updateSeq(SeqNo);

					// if this is the first RTP Packet Received from this source then
					// store the seq no. as its base
					if (RTPSource.getNoOfRTPPacketsRcvd() == 0)
						RTPSource.setBaseSeq(SeqNo);

					// Increment the total number of RTP Packets Received
					RTPSource.setNoOfRTPPacketsRcvd(RTPSource.getNoOfRTPPacketsRcvd() + 1);
				} else {
					System.err.println("RTP Receiver: Bad RTP Packet received");
					System.err.println("From : " + packet.getAddress() + "/" + packet.getPort() + "\n" + "Length : "
							+ packet.getLength());
				}
			}

			// s.leaveGroup( m_InetAddress );

			// s.close();
		} catch (SocketException se) {
			se.printStackTrace();
			System.err.println(se);
		}

		catch (java.io.IOException e) {
			e.printStackTrace();
			Log.d(TAG, "IO exception");
		}
	}

	private synchronized void startRTCPRSender() {
		if (!startedRTCPSender) {
			startedRTCPSender = true;
			// Starts the RTCP Sender thread
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					PrivateListeningSession.m_RTCPHandler.startRTCPSenderThread(RTCPSenderSocket);
				}
			};
			Thread thread = new Thread(runnable);
			thread.start();
		}
	}

	/**
	 * Validates RTP Packet. Returns true or false corresponding to the test
	 * results.
	 *
	 * @param - packet[] The RTP Packet to be validated.
	 * @return True if validation was successful, False otherwise.
	 */
	public boolean ValidateRTPPacketHeader(byte packet[]) {
		boolean versionValid = false;
		boolean payloadTypeValid = false;

		// +-+-+-+-+-+-+-+-+
		// |V=2|P|X| CC |
		// +-+-+-+-+-+-+-+-+

		// Version MUST be 2
		if (((packet[0] & 0xC0) >> 6) == 2)
			versionValid = true;
		else
			versionValid = false;

		// +-+-+-+-+-+-+-+-+
		// |M| PT |
		// +-+-+-+-+-+-+-+-+
		// 0 1 0 1 1 0 0 0

		// Payload Type must be the same as the session's
		if ((packet[1] & 0x7F) == PrivateListeningSession.getPayloadType())
			payloadTypeValid = true;
		else
			payloadTypeValid = false;

		return (versionValid && payloadTypeValid);

	}
}
