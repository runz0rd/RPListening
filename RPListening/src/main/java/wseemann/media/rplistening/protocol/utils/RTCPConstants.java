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

/*
*   This class provides constants associated with RTCP Packets
*/
public class RTCPConstants extends Object {

	/**
	 * Version =2
	 */
	public static final byte VERSION = 2;

	/**
	 * Padding =0
	 */
	public static final byte PADDING = 0;

	/**
	 * RTCP TYPES
	 */

	public static final int RTCP_RR = (int) 201;
	public static final int RTCP_APP = (int) 204;
}