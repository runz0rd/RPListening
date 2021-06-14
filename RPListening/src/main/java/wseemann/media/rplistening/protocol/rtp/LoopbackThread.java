package wseemann.media.rplistening.protocol.rtp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class LoopbackThread extends Thread {
	
	private List<DatagramPacket> packets;
	private InetAddress loopbackAddress;
	private DatagramSocket loopbackSocket;
	
	public LoopbackThread(InetAddress loopbackAddress) throws SocketException {
		this.loopbackAddress = loopbackAddress;
		this.loopbackSocket = new DatagramSocket(5152);
		this.packets = new ArrayList<DatagramPacket>();
	}
	
	public void run() {
		while (true) {
			try {
				DatagramPacket packet = getPacket();
				
				if (packet != null) {
					//System.out.println("oopopopop");
					// Create a datagram packet from the RTP byte packet and set ttl and send
					DatagramPacket pkt = new DatagramPacket(packet.getData(), packet.getLength(),
							loopbackAddress, 5153);
					loopbackSocket.send(pkt);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}				
		}
	}
	
	public synchronized void addPacket(DatagramPacket packet) {
		System.out.println(packets.size());
		packets.add(packet);
	}
	
	private synchronized DatagramPacket getPacket() {
		if (packets.size() == 0) {
			return null;
		}
		DatagramPacket packet = packets.get(0);
		packets.remove(0);
		return packet;
	}
}