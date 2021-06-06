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

public class AuthChallengeResponse {
	
	private String notify;
	@SerializedName(value = "param-challenge")
	private String paramChallenge;
	private String timestamp;

	public AuthChallengeResponse() {
		
	}

	public String getNotify() {
		return notify;
	}

	public void setNotify(String notify) {
		this.notify = notify;
	}
	
	public String getParamChallenge() {
		return paramChallenge;
	}

	public void setParamChallenge(String paramChallenge) {
		this.paramChallenge = paramChallenge;
	}

	private String getTimestamp() {
		return timestamp;
	}

	private void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
}

