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

package wseemann.media.rplistening.websocket;

import com.google.gson.Gson;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import wseemann.media.rplistening.utils.AuthUtils;
import wseemann.media.rplistening.utils.Log;
import wseemann.media.rplistening.websocket.model.AuthChallengeResponse;
import wseemann.media.rplistening.websocket.model.AuthResponse;
import wseemann.media.rplistening.websocket.model.GenericResponse;
import wseemann.media.rplistening.websocket.model.NotifyResponse;
import wseemann.media.rplistening.websocket.model.RequestResponse;

public class CustomWebSocketListener extends WebSocketListener {

	private String TAG = "CustomWebSocketListener";
	
	private RokuWebSocketListener listener;
	
	public CustomWebSocketListener(RokuWebSocketListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void onClosed(WebSocket webSocket, int code, String reason) {
		super.onClosed(webSocket, code, reason);
	}

	@Override
	public void onClosing(WebSocket webSocket, int code, String reason) {
		super.onClosing(webSocket, code, reason);
	}

	@Override
	public void onFailure(WebSocket webSocket, Throwable t, Response response) {
		super.onFailure(webSocket, t, response);
	}

	@Override
	public void onMessage(WebSocket webSocket, ByteString bytes) {
		super.onMessage(webSocket, bytes);
		Log.d(TAG, "onMessage bytes: " + bytes);
	}

	@Override
	public void onMessage(WebSocket webSocket, String text) {
		super.onMessage(webSocket, text);
		Log.d(TAG, "onMessage text: " + text);
		
		// TODO add response handler
		
		Gson gson = new Gson();
		RequestResponse requestResponse = gson.fromJson(text, RequestResponse.class);
		
		if (requestResponse.getNotify() != null) {
			NotifyResponse notifyResponse = gson.fromJson(text, NotifyResponse.class);
			
			if (notifyResponse.getNotify().equals("authenticate")) {
				AuthChallengeResponse authChallengeResponse = gson.fromJson(text, AuthChallengeResponse.class);

				AuthResponse authResponse = new AuthResponse();
				authResponse.setRequest(authChallengeResponse.getNotify());
				authResponse.setRequestId("0");
				authResponse.setParamResponse(AuthUtils.generateAuthResponse(authChallengeResponse));

				webSocket.send(gson.toJson(authResponse));
			}
		} else if (requestResponse.getResponse() != null) {
			GenericResponse genericResponse = gson.fromJson(text, GenericResponse.class);
			
			if (genericResponse.getResponse().equals("authenticate") &&
					genericResponse.getStatus().equals("200") &&
					genericResponse.getStatusMsg().equals("OK")) {
				listener.onAuthSuccess();
			} else if (genericResponse.getResponse().equals("authenticate") &&
					genericResponse.getStatus().equals("401") &&
					genericResponse.getStatusMsg().equals("Unauthorized")) {
				listener.onAuthFailed();
			} else if (genericResponse.getResponse().equals("set-audio-output") &&
					genericResponse.getStatus().equals("200") &&
					genericResponse.getStatusMsg().equals("OK")) {
				listener.onSetAudioOutput();
			} else if (genericResponse.getResponse().equals("query-audio-device") &&
					genericResponse.getStatus().equals("200") &&
					genericResponse.getStatusMsg().equals("OK")) {
				System.out.println(AuthUtils.decodeResponse(requestResponse.getContentData()));
			}
		}
	}

	@Override
	public void onOpen(WebSocket webSocket, Response response) {
		super.onOpen(webSocket, response);
	}
}
