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

package wseemann.media.rplistening.protocol.utils;

import wseemann.media.rplistening.utils.Log;

/**
 * This class provides generic packet assembly and disassembly functions, which
 * are used in various parts of the project e.g. assembling and disassembling
 * RTP and RTCP packets etc.
 *
 */
public class PacketUtils {
	
	private final static String TAG = "PacketUtils";
	
	/**
	 * Append two byte arrays. Appends packet B at the end of Packet A (Assuming
	 * Bytes as elements). Returns packet ( AB ).
	 *
	 * @param packetA The first packet.
	 * @param packetB The second packet.
	 * @return The desired packet which is A concatenated with B.
	 */
	public synchronized static byte[] Append(byte[] packetA, byte[] packetB) {
		// Create a new array whose size is equal to sum of packets
		// being added
		byte packetAB[] = new byte[packetA.length + packetB.length];

		// First paste in packetA
		for (int i = 0; i < packetA.length; i++)
			packetAB[i] = packetA[i];

		// Now start pasting packetB
		for (int i = 0; i < packetB.length; i++)
			packetAB[i + packetA.length] = packetB[i];

		return packetAB;
	}

	/**
	 * Convert signed int to long by taking 2's complement if necessary.
	 *
	 * @param intToConvert The signed integer which will be converted to Long.
	 * @return The unsigned long representation of the signed int.
	 */
	public synchronized static long ConvertSignedIntToLong(int intToConvert) {
		int in = intToConvert;
		Log.d(TAG, String.valueOf(in));

		in = (in << 1) >> 1;

		long Lin = (long) in;
		Lin = Lin + 0x7FFFFFFF;

		return Lin;
	}

	/**
	 * Convert 64 bit long to n bytes.
	 *
	 * @param ldata The long from which the n byte array will be constructed.
	 * @param n     The desired number of bytes to convert the long to.
	 * @return The desired byte array which is populated with the long value.
	 */
	public synchronized static byte[] LongToBytes(long ldata, int n) {
		byte [] buff = new byte[n];

		for (int i = n - 1; i >= 0; i--) {
			// Keep assigning the right most 8 bits to the
			// byte arrays while shift 8 bits during each iteration
			buff[i] = (byte) ldata;
			ldata = ldata >> 8;
		}
		return buff;
	}
}