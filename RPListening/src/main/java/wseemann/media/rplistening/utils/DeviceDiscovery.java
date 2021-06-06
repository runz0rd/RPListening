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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;

public class DeviceDiscovery {
	
	private DeviceDiscovery() {
		
	}
	
	public static String discoverDevices(String url) {
		String address = null;

		try {
			URL host = new URL(url);
			
			/* create byte arrays to hold our send and response data */
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];

			/* our M-SEARCH data as a byte array */
			String MSEARCH = "M-SEARCH * HTTP/1.1\nHost: " + host.getHost() + ":" + host.getPort()
					+ "\nMan: \"ssdp:discover\"\nST: roku:ecp\n";
			sendData = MSEARCH.getBytes();

			/* create a packet from our data destined for 239.255.255.250:1900 */
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
					InetAddress.getByName(host.getHost()), host.getPort());

			/* send packet to the socket we're creating */
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.send(sendPacket);

			/* recieve response and store in our receivePacket */
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);

			/* get the response as a string */
			String response = new String(receivePacket.getData());

			/* close the socket */
			clientSocket.close();

			/* parse the IP from the response */
			/*
			 * the response should contain a line like: Location: http://192.168.1.9:8060/
			 * and we're only interested in the address -- not the port. So we find the
			 * line, then split it at the http:// and the : to get the address.
			 */
			response = response.toLowerCase();
			address = response.split("location:")[1].split("\n")[0].split("http://")[1].split(":")[0].trim();

			/* return the IP */
			return address;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		return address;
	}
}
