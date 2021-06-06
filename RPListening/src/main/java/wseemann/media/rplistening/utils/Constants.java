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

package wseemann.media.rplistening.utils;

public class Constants {

	private Constants() {
		
	}
	
	public static String DEVICE_DISCOVERY_URL = "http://239.255.255.250:1900";
	public static int ROKU_ECP_PORT = 8060;
	public static int RTP_PORT = 6970;
	public static int RTP_OUTBOUND_PORT = 6971;
	public static int RTCP_PORT = 5150;
	public static int RTCP_OUTBOUND_PORT = 5051;
	public static int RTP_PAYLOAD_TYPE = 97;
	
	public static String SDP_FILE = "v=0\n"
			+ "o=- 0 0 IN IP4 127.0.0.1\n"
			+ "s=-\n"
			+ "c=IN IP4 127.0.0.1\n"
			+ "m=audio 5153 RTP/AVP 97\n"
			+ "a=rtpmap:97 opus/48000/2";
	
	public static String FFPLAY_CMD = " | <ffplay>"
			+ " -hide_banner -loglevel error -protocol_whitelist pipe,file,udp,rtp -vn -nodisp -nostats -i -";
	
	public static String [] FFPLAY_PATH_CMD = {"bash", "-c", "which ffplay"};
}
