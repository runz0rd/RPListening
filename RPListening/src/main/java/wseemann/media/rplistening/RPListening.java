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

package wseemann.media.rplistening;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import picocli.CommandLine;
import picocli.CommandLine.ParseResult;
import wseemann.media.rplistening.protocol.Session;
import wseemann.media.rplistening.utils.CommandLineArgs;
import wseemann.media.rplistening.utils.Constants;
import wseemann.media.rplistening.utils.DeviceDiscovery;
import wseemann.media.rplistening.utils.Log;
import wseemann.media.rplistening.utils.ShellCommand;
import wseemann.media.rplistening.websocket.RokuWebSocketListener;
import wseemann.media.rplistening.websocket.WebSocketConnection;
import wseemann.media.rplistening.websocket.WebSocketConnectionImpl;

public class RPListening {

	private static String TAG = "RPListening";
	
	private static String testDeviceIp = null;
	private static WebSocketConnection webSocketConnection;
	private static Session session;
	private static Process ffplayProcess;
	private static String hostAddress;

	public static void main(String[] args) {
		Log.suppressLogs = true;
		
		CommandLineArgs commandLineArgs = new CommandLineArgs();
		CommandLine commandLine = new CommandLine(commandLineArgs);
		commandLine.getCommandSpec().parser().collectErrors(true);
		ParseResult parseResult = commandLine.parseArgs(args);

		if (parseResult.errors().size() != 0) {
			Log.d(TAG, parseResult.errors().get(0).getMessage());
			System.exit(0);
		} else if (commandLine.isUsageHelpRequested()) {
			commandLine.usage(System.out);
			System.exit(0);
		} else if (commandLine.isVersionHelpRequested()) {
			commandLine.printVersionHelp(System.out);
			System.exit(0);
		} else if (commandLineArgs.discoverDevices) {
			String devices = DeviceDiscovery.discoverDevices(Constants.DEVICE_DISCOVERY_URL);
			System.out.println(devices);
			System.exit(0);
		} else if (commandLineArgs.deviceIp == null && testDeviceIp == null) {
			System.exit(0);
		}

		String rokuAddress;
		
		if (commandLineArgs.deviceIp != null) {
			rokuAddress = commandLineArgs.deviceIp;
		} else {
			rokuAddress = testDeviceIp;
		}

		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println("Unable to determine localhost IP address. Exiting...");
			System.exit(0);
		}

		webSocketConnection = new WebSocketConnectionImpl("http://" + rokuAddress + ":" + Constants.ROKU_ECP_PORT,
				new RokuWebSocketListener() {

					@Override
					public void onAuthSuccess() {
						Log.d(TAG, "onAuthSuccess!");
						webSocketConnection.setAudioOutput(hostAddress + ":" + Constants.RTP_PORT);
					}

					@Override
					public void onSetAudioOutput() {
						Log.d(TAG, "onSetAudioOutput!");

						session = new Session(
							rokuAddress,
							hostAddress,
							Constants.RTP_PORT,
							Constants.RTCP_PORT,
							Constants.RTP_OUTBOUND_PORT,
							Constants.RTP_PORT,
							10000	
						);
						session.setPayloadType(Constants.RTP_PAYLOAD_TYPE);
						session.startRTPReceiverThread();

						ShellCommand shellCommand = new ShellCommand();
						ffplayProcess = shellCommand.executeCommand("");
					}

					@Override
					public void onAuthFailed() {
						
					}
				});

		webSocketConnection.connect();

		Scanner scanner = new Scanner(System.in);
		//System.out.println("Press any key to exit...");
		System.out.println("Use ctrl^c to exit...");
		scanner.nextLine();
		closeSession();
		scanner.close();
		
		System.exit(0);
	}
	
	private static synchronized void closeSession() {
		if (session != null) {
			session.stopRTCPSenderThread();
			session.stopRTPReceiverThread();
		}
		
		synchronized(ffplayProcess) {
		
		if (ffplayProcess != null) {
			try {
				ffplayProcess.destroyForcibly().wait(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		}
	}
}
