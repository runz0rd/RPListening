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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import wseemann.media.rplistening.websocket.model.QueryAudioDeviceRequest;
import wseemann.media.rplistening.websocket.model.SetAudioOutputRequest;

public class WebSocketConnectionImpl implements WebSocketConnection {
	
	private WebSocket websocket;
	private OkHttpClient client;
	private String url;
	private RokuWebSocketListener listener;

	public WebSocketConnectionImpl(final String url, final RokuWebSocketListener listener) {
		this.url = url;
		this.listener = listener;
		client = new OkHttpClient();
	}
	
	private void performAuth() {
		Request myrequest = new Request.Builder().url(url + "/ecp-session")
                .addHeader("Sec-WebSocket-Origin", "Android")
                .addHeader("Sec-WebSocket-Protocol", "ecp-2")
                .addHeader("Upgrade", "websocket")
                .addHeader("Connection", "Upgrade")
                .addHeader("Sec-WebSocket-Key", "<Enter Key Here>")
                .addHeader("Sec-WebSocket-Version", "13")
                .addHeader("Host", url.replace("http://", ""))
                .addHeader("Accept-Encoding", "gzip")
                .addHeader("User-Agent", "okhttp/3.11.0")
                .build();
		
        websocket = client.newWebSocket(myrequest, new CustomWebSocketListener(listener));
	}
	
	private void setAudioOutputInternal(String url) {
		Gson gson = new Gson();
		
		SetAudioOutputRequest setAudioOutputRequest = new SetAudioOutputRequest();
		setAudioOutputRequest.setDeviceName(url);
		setAudioOutputRequest.setRequestId("1");
		
		websocket.send(gson.toJson(setAudioOutputRequest));
	}

	private void queryAudioOutputInternal() {
		Gson gson = new Gson();
		
		QueryAudioDeviceRequest queryAudioDeviceRequest = new QueryAudioDeviceRequest();
		queryAudioDeviceRequest.setRequestId("3");
		
		websocket.send(gson.toJson(queryAudioDeviceRequest));
	}
	
	@Override
	public void connect() {
		performAuth();
		
	}

	@Override
	public void setAudioOutput(String url) {
		setAudioOutputInternal(url);
	}
	
	@Override
	public void queryAudioDevice() {
		queryAudioOutputInternal();
	}
	
	@Override
	public void disconnect() {
		if (websocket != null) {
			websocket.close(1000, null);
		}
	}
}
