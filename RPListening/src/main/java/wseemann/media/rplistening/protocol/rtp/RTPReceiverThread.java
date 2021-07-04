package wseemann.media.rplistening.protocol.rtp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

import wseemann.media.rplistening.protocol.PrivateListeningSession;
import wseemann.media.rplistening.protocol.Source;
import wseemann.media.rplistening.utils.Log;

public class RTPReceiverThread extends Thread {

	private static final String TAG = "RTPThreadHandler";
	
	private boolean startedRTCPSender = false;

	/**
	 * Multicast Port for RTP Packets
	 */
	private final int m_mcastPort;

	/**
	 * Sender Address for RTP Packets
	 */
	private final InetAddress m_InetAddress;

	/**
	 * Sender Loopback Address for RTP Packets
	 */
	private final InetAddress m_loopbackAddress;
	
	private DatagramSocket RTCPSenderSocket;
	private DatagramSocket loopbackSocket;

	/**
	 * Initialize Random Number Generator
	 */
	static Random RandomNumGenerator = new Random();

	/**
	 * Random Offset -32 bit
	 */
	public static final short RandomOffset = (short) Math.abs(RandomNumGenerator.nextInt() & 0x000000FF);

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
	 * @param loopbackAddress   Port used to send RTP Packets.
	 * @param MulticastPort     Port for Multicast group (for receiving RTP
	 *                          Packets).
	 *
	 *
	 */

	public RTPReceiverThread(InetAddress MulticastAddress, InetAddress loopbackAddress, int MulticastPort) {
		m_InetAddress = MulticastAddress;
		m_loopbackAddress = loopbackAddress;
		
		m_mcastPort = MulticastPort;

		Random rnd = new Random(); // Use time as default seed

		// Start with a random sequence number
		sequence_number = Math.abs(rnd.nextInt()) & 0x000000FF;
		timestamp = PrivateListeningSession.CurrentTime() + RandomOffset;

		Log.d(TAG, "RTP Session SSRC: " + Long.toHexString(PrivateListeningSession.SSRC));
		Log.d(TAG, " Starting Seq: " + sequence_number);
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

		byte [] buf = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);

		PayloadType = PrivateListeningSession.getPayloadType();

		try {
			RTCPSenderSocket = new DatagramSocket(m_mcastPort);
			loopbackSocket = new DatagramSocket(5152);

			while (!isInterrupted()) {
				RTCPSenderSocket.receive(packet);
				startPacketForward(packet);

				if (ValidateRTPPacketHeader(packet.getData())) {
					long SSRC;
					int TimeStamp;
					short SeqNo;
					byte PT;

					PT = (byte) ((buf[1] & 0xff) & 0x7f);
					SeqNo = (short) ((buf[2] << 8) | (buf[3] & 0xff));
					TimeStamp = (((buf[4] & 0xff) << 24) | ((buf[5] & 0xff) << 16) | ((buf[6] & 0xff) << 8)
							| (buf[7] & 0xff));
					SSRC = (((buf[8] & 0xff) << 24) | ((buf[9] & 0xff) << 16) | ((buf[10] & 0xff) << 8)
							| (buf[11] & 0xff));

					Log.d(TAG, "RTP (");
					Log.d(TAG, "ssrc=0x" + Long.toHexString(SSRC) + "\tts=" + TimeStamp + "\tseq=" + SeqNo + "\tpt=" + PT);
					Log.d(TAG, ")");

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
		} catch (IOException ex) {
			ex.printStackTrace();
			Log.d(TAG, ex.getMessage());
		}

		RTCPSenderSocket.close();
		loopbackSocket.close();
	}

	private void startPacketForward(DatagramPacket packet) {
		Runnable runnable = () -> {
			try {
				// Create a datagram packet from the RTP byte packet and set ttl and send
				DatagramPacket pkt = new DatagramPacket(packet.getData(), packet.getLength(),
						m_loopbackAddress, 5153);
				loopbackSocket.send(pkt);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}

	private synchronized void startRTCPRSender() {
		if (!startedRTCPSender) {
			startedRTCPSender = true;
			// Starts the RTCP Sender thread
			Runnable runnable = () -> PrivateListeningSession.m_RTCPHandler.startRTCPSenderThread(RTCPSenderSocket);
			Thread thread = new Thread(runnable);
			thread.start();
		}
	}

	/**
	 * Validates RTP Packet. Returns true or false corresponding to the test
	 * results.
	 *
	 * @param packet The RTP Packet to be validated.
	 * @return True if validation was successful, False otherwise.
	 */
	public boolean ValidateRTPPacketHeader(byte [] packet) {
		boolean versionValid;
		boolean payloadTypeValid;

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
