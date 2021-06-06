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

package wseemann.media.rplistening.protocol.rtp;

import java.net.*;

/**
 * This class creates and starts the RTP receiver thread
 * 
 */
public class RTPThreadHandler {
	/**
	 * Reference to the RTCP Sender Thread
	 * 
	 */
	private RTPReceiverThread rtpReceiverThread;

	/**
	 * Constructor creates the receiver threads. (Does not start the
	 * threads)
	 *
	 * @param MulticastAddress  Dotted representation of the Multicast address.
	 * @param SendFromLocalPort Port used to send RTP Packets.
	 * @param MulticastPort     Port for Multicast group (for receiving RTP
	 *                          Packets).
	 */
	public RTPThreadHandler(InetAddress multicastAddress, InetAddress loopbackAddress, int sendFromLocalPort, int multicastPort) {
		rtpReceiverThread = new RTPReceiverThread(multicastAddress, loopbackAddress, sendFromLocalPort, multicastPort);
	}

	/**
	 * Starts the RTCP Sender thread.
	 */
	public void startRTPReceiverThread() {
		rtpReceiverThread.start();
	}

	/**
	 * Interrupts a running RTP receiver thread.
	 */
	public void stopRTPReceiverThread() {
		rtpReceiverThread.interrupt();
	}
}