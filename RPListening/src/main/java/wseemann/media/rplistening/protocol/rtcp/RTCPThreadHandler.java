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

/**
 * This class creates and starts the RTCP sender and receiver threads
 * 
 */
public class RTCPThreadHandler {
	/**
	 * Reference to the RTCP Sender Thread
	 * 
	 */
	private final RTCPSenderThread rtcpSenderThread;

	/**
	 * Constructor creates the sender and receiver threads. (Does not start the
	 * threads)
	 *
	 * @param multicastGroupIPAddress Dotted representation of the Multicast
	 *                                address.
	 * @param rtcpSendFromPort        Port used to send RTCP Packets.
	 * @param rtcpGroupPort           Port for Multicast group (for receiving RTCP
	 *                                Packets).
	 */
	public RTCPThreadHandler(InetAddress multicastGroupIPAddress, int rtcpSendFromPort, int rtcpGroupPort) {
		rtcpSenderThread = new RTCPSenderThread(multicastGroupIPAddress, rtcpSendFromPort, rtcpGroupPort);
	}

	/**
	 * Starts the RTCP Sender thread.
	 */
	public void startRTCPSenderThread(DatagramSocket rtcpSenderSocket) {
		rtcpSenderThread.setRTCPSenderSocket(rtcpSenderSocket);
		rtcpSenderThread.start();
	}

	/**
	 * Interrupts a running RTCP sender thread. This will cause the sender to send
	 * BYE packet and finally terminate.
	 */
	public void stopRTCPSenderThread() {
		rtcpSenderThread.interrupt();
	}
}