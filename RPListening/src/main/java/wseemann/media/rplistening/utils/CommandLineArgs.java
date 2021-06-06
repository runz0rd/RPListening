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

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "rplistening", mixinStandardHelpOptions = true, version = "rplistening 1.0",
description = "RPListening is an Open Source desktop client for Roku private listening..")
public class CommandLineArgs implements Runnable {

	@Option(names = "-i", required = false, description = "The IP address of the input device. Use the -d option to find this.")
	//@Parameters(index = "0", description = "The IP address of the input device. Use the -d option to find this.")
    public String deviceIp;
	
	@Option(names = "-d", description = "Discover devices.")
    public boolean discoverDevices;

	@Override
	public void run() {
		
	}
}
