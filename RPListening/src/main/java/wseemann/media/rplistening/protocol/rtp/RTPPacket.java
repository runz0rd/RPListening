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

/**
 * This class encapsulates all the necessary parameters of a RTP Packet that
 * needs to be handed to the Application when a RTP Packet is received.
 */
public class RTPPacket {
	/**
	 * The CSRC count contains the number of CSRC identifiers that follow the fixed
	 * header.
	 */
	private long csrcCount;

	/**
	 * The sequence number increments by one for each RTP data packet sent, and may
	 * be used by the receiver to detect packet loss and to restore packet sequence.
	 * The initial value of the sequence number is random (unpredictable) to make
	 * known-plaintext attacks on encryption more difficult, even if the source
	 * itself does not encrypt, because the packets may flow through a translator
	 * that does.
	 */
	private long sequenceNumber;

	/**
	 * The timestamp reflects the sampling instant of the first octet in the RTP
	 * data packet.
	 */
	private long timestamp;

	/**
	 * The SSRC field identifies the synchronization source. This identifier is
	 * chosen randomly, with the intent that no two synchronization sources within
	 * the same RTP session will have the same SSRC identifier.
	 */
	private long ssrc;

	/**
	 * The actual payload contained in a RTP Packet
	 */
	private byte [] data;

	public void setCSRCCount(long csrcCount) {
		this.csrcCount = csrcCount;
	}

	public void setSequenceNumber(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setSSRC(long ssrc) {
		this.ssrc = ssrc;
	}

	public void setData(byte data[]) {
		this.data = data;
	}
}