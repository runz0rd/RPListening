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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import wseemann.media.rplistening.websocket.model.AuthChallengeResponse;

public class AuthUtils {

	private AuthUtils() {
		
	}
	
	public static String generateAuthResponse(AuthChallengeResponse authChallengeReponse) {
		String hash = "";
		MessageDigest instance;

		StringBuilder sb = new StringBuilder();
		sb.append(authChallengeReponse.getParamChallenge());
		sb.append(generateHash("95E610D0-7C29-44EF-FB0F-97F1FCE4C297", 9));

		byte[] bytes = sb.toString().getBytes();
		
		try {
			instance = MessageDigest.getInstance("SHA-1");
			instance.update(bytes, 0, bytes.length);
			hash = Base64.getEncoder().encodeToString(instance.digest());
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		
		return hash;
	}
	
	public static String decodeResponse(String text) {
		return new String(Base64.getDecoder().decode(text.getBytes()));
	}
	
	private static String generateHash(String str, int i) {
		StringBuilder sb = new StringBuilder();
		
		for (int i2 = 0; i2 < str.length(); i2++) {
			sb.append(hash(str.charAt(i2), i));
		}
		
		return sb.toString();
	}

	private static char hash(char c, int i) {
		int i2 = (c < '0' || c > '9') ? (c < 'A' || c > 'F') ? -1 : (c - 'A') + 10 : c - '0';
		
		if (i2 < 0) {
			return c;
		}
		
		int i3 = ((15 - i2) + i) & 15;
		
		return (char) (i3 < 10 ? i3 + 48 : (i3 + 65) - 10);
	}
}
