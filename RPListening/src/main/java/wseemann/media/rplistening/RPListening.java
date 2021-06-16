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

import wseemann.media.rplistening.protocol.ConnectionListener;
import wseemann.media.rplistening.protocol.PrivateListeningSession;
import wseemann.media.rplistening.ui.RPListeningApp;
import wseemann.media.rplistening.utils.CommandLineArgs;
import wseemann.media.rplistening.utils.Constants;
import wseemann.media.rplistening.utils.DeviceDiscovery;
import wseemann.media.rplistening.utils.Log;

import java.util.List;
import java.util.Scanner;

import com.jaku.api.DeviceRequests;
import com.jaku.model.Device;

import picocli.CommandLine;
import picocli.CommandLine.ParseResult;
import tornadofx.App;

public class RPListening {

	private static String TAG = "RPListening";
	
	private static String testDeviceIp = null;
	private static PrivateListeningSession session = null;	

	public static void main(String[] args) {		
		PrivateListeningSession.setDebugMode(false);
		
		//App.launch(RPListeningApp.class, args);
		
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

		String rokuIPAddress;
		
		if (commandLineArgs.deviceIp != null) {
			rokuIPAddress = commandLineArgs.deviceIp;
		} else {
			rokuIPAddress = testDeviceIp;
		}

		PrivateListeningSession.connect(rokuIPAddress, new ConnectionListener() {

			@Override
			public void onConnected(PrivateListeningSession session) {
				RPListening.session = session;
			}

			@Override
			public void onFailure(Throwable error) {
				PrivateListeningSession.disconnect(session);
				RPListening.session = null;
			}
		});
		
		Scanner scanner = new Scanner(System.in);
		//System.out.println("Press any key to exit...");
		System.out.println("Use ctrl^c to exit...");
		scanner.nextLine();
		
		PrivateListeningSession.disconnect(session);
		scanner.close();
		
		System.exit(0);
	}
}
