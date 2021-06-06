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

package wseemann.media.rplistening.websocket.model;

import com.google.gson.annotations.SerializedName;

public class SetAudioOutputRequest extends Request {

	private String request = "set-audio-output";
	@SerializedName(value = "param-audio-output")
	private String paramAudioOutput = "datagram";
	@SerializedName(value = "param-devname")
	private String deviceName;
	
	public SetAudioOutputRequest() {
		
	}
	
	public String getRequest() {
		return request;
	}

	public String getParamAudioOutput() {
		return paramAudioOutput;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
}