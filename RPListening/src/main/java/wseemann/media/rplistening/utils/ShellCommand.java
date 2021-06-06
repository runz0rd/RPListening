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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ShellCommand {

	private boolean isWindows;
	
	public ShellCommand() {
		isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
	}
	
	//https://stackoverflow.com/questions/26830617/running-bash-commands-in-java
	//https://www.baeldung.com/run-shell-command-in-java
	public Process executeCommand(String command) {
		int exitCode = -1;
		Process process = null;
		
		ProcessBuilder builder = new ProcessBuilder();
		
		if (isWindows) {
		    builder.command("cmd.exe", "/c", "ffplay", "-i");
		} else {
		    //builder.command("bash", "-c", "/Users/wseemann/Desktop/private.sh");
			
			String ffplayPath = executeShellCommand(Constants.FFPLAY_PATH_CMD);
			String ffplayCmd = Constants.FFPLAY_CMD.replace("<ffplay>", ffplayPath);
			
			StringBuilder sb = new StringBuilder("echo");
			sb.append(" ");
			sb.append("\"");
			sb.append(Constants.SDP_FILE);
			sb.append("\"");
			sb.append(ffplayCmd);
			builder.command("bash", "-c", sb.toString());
		}
		
		builder.directory(new File(System.getProperty("user.home")));
		builder.redirectErrorStream(true);
		
		try {
			process = builder.start();
		
			StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
			Executors.newSingleThreadExecutor().submit(streamGobbler);
		
			//exitCode = process.waitFor();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return process; //exitCode == 0;
	}
	
	private String executeShellCommand(String[] commands) {
		String output = null;
		
		try {
			Process proc = Runtime.getRuntime().exec(commands);

			BufferedReader stdInput = new BufferedReader(new 
					InputStreamReader(proc.getInputStream()));

			String s = null;
			StringBuffer sb = new StringBuffer();
			
			while ((s = stdInput.readLine()) != null) {
				sb.append(s);
			}
			
			output = sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	private static class StreamGobbler implements Runnable {
	    private InputStream inputStream;
	    private Consumer<String> consumer;

	    public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
	        this.inputStream = inputStream;
	        this.consumer = consumer;
	    }

	    @Override
	    public void run() {
	        new BufferedReader(new InputStreamReader(inputStream)).lines()
	          .forEach(consumer);
	    }
	}
}
