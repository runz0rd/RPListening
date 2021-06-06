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

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "audio-device")
public class AudioDevice {

	@Element(name="capabilities", type=Capabilities.class, required=false)
	private Capabilities capabilities;
	@Element(name="global", type=Global.class, required=false)
	private Global global;
	@Element(name="destinations", type=Destinations.class, required=false)
	private Destinations destinations;
	@Element(name="rtp-info", type=RtpInfo.class, required=false)
	private RtpInfo rtpInfo;
	
	public AudioDevice() {
		this.capabilities = new Capabilities();
		this.global = new Global();
		this.destinations = new Destinations();
		this.rtpInfo = new RtpInfo();
    }
	
	public Capabilities getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Capabilities capabilities) {
		this.capabilities = capabilities;
	}

	public Global getGlobal() {
		return global;
	}

	public void setGlobal(Global global) {
		this.global = global;
	}

	public Destinations getDestinations() {
		return destinations;
	}

	public void setDestinations(Destinations destinations) {
		this.destinations = destinations;
	}

	public RtpInfo getRtpInfo() {
		return rtpInfo;
	}

	public void setRtpInfo(RtpInfo rtpInfo) {
		this.rtpInfo = rtpInfo;
	}

	@Root(name="capabilities")
	public static class Capabilities {

		@Element(name="all-destinations", required=false)
		private String[] allDestinations;

		public String[] getAllDestinations() {
			return allDestinations;
		}

		public void setAllDestinations(String[] allDestinations) {
			this.allDestinations = allDestinations;
		}
	}
	
	@Root(name = "global")
	public static class Global {
	
		@Element(name="muted", required=false)
		private boolean muted;
		@Element(name="volume", required=false)
		private int volume;
		@Element(name="destination-list", required=false)
		private String[] destinationList;
		
		public boolean getMuted() {
			return muted;
		}

		public void setMuted(boolean muted) {
			this.muted = muted;
		}

		public int getVolume() {
			return volume;
		}

		public void setVolume(int volume) {
			this.volume = volume;
		}

		public String[] getDestinationList() {
			return destinationList;
		}

		public void setDestinationList(String[] destinationList) {
			this.destinationList = destinationList;
		}	
		
	}
	
	@Root(name="destinations")
	public static class Destinations {
		
		@ElementList(entry="destination", inline=true)
		private List<Destination> destinations;

		public List<Destination> getDestinations() {
			return destinations;
		}		
	}
	
	@Root(name="destination")
	public static class Destination {
		
		@Attribute(required=false)
		private String name;
		@Element(name="volume", required=false)
		private int volume;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getVolume() {
			return volume;
		}

		public void setVolume(int volume) {
			this.volume = volume;
		}
	}
	
	@Root(name="rtp-info")
	public static class RtpInfo {
		
		@Element(name="rtp-address", required=false)
		private String rtpAddress;
		@Element(name="rtcp-port", required=false)
		private int rtcpPort;
		@Element(name="current-buffer-delay-us", required=false)
		private int currentBufferDelayUs;
		@Element(name="client-versions", required=false)
		private int clientVersions;
		
		public String getRtpAddress() {
			return rtpAddress;
		}

		public void setRtpAddress(String rtpAddress) {
			this.rtpAddress = rtpAddress;
		}
		
		public int getRtcpPort() {
			return rtcpPort;
		}

		public void setRtcpPort(int rtcpPort) {
			this.rtcpPort = rtcpPort;
		}		
	
		public int getCurrentBufferDelayUs() {
			return currentBufferDelayUs;
		}

		public void setCurrentBufferDelayUs(int currentBufferDelayUs) {
			this.currentBufferDelayUs = currentBufferDelayUs;
		}
		
		public int getClientVersions() {
			return clientVersions;
		}

		public void setClientVersions(int clientVersions) {
			this.clientVersions = clientVersions;
		}
	}
}