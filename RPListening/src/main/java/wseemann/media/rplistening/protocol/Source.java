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

package wseemann.media.rplistening.protocol;

/**
 * This class encapsulates all the per source state information. Every source
 * keeps track of all the other sources in the multicast group, from which it
 * has received a RTP or RTCP Packet. It is necessry to keep track of per state
 * source information in order to provide effective reception quality feedback
 * to all the sources that are in the multicast group.
 */

public class Source {

	/**
	 * source SSRC uint 32.
	 */
	private long ssrc; // unsigned 32 bits

	/**
	 * Fraction of RTP data packets from source SSRC lost since the previous SR or
	 * RR packet was sent, expressed as a fixed point number with the binary point
	 * at the left edge of the field. To get the actual fraction multiply by 256 and
	 * take the integral part
	 */
	private double fraction; // 8 bits

	/**
	 * Cumulative number of packets lost (signed 24bits).
	 *
	 */
	private long lost; // signed 24 bits

	/**
	 * extended highest sequence number received.
	 */
	private long lastSeq; // unsigned 32 bits

	/**
	 * Interarrival jitter.
	 */
	private long jitter; // unsigned 32 bits

	/**
	 * Last SR Packet from this source.
	 */
	private long lst; // unsigned 32 bits

	/**
	 * Delay since last SR packet.
	 */
	private double dlsr;

	/**
	 * Is this source and ActiveSender.
	 */
	private boolean activeSender;

	/**
	 * Time the last RTCP Packet was received from this source.
	 */
	private double timeOfLastRTCPArrival;

	/**
	 * Time the last RTP Packet was received from this source.
	 */
	private double timeOfLastRTPArrival;

	/**
	 * Time the last Sender Report RTCP Packet was received from this source.
	 */
	private double timeOfLastSRRcvd;

	/**
	 * Total Number of RTP Packets Received from this source
	 */
	private int noOfRTPPacketsRcvd;

	/**
	 * Sequence Number of the first RTP packet received from this source
	 */
	private long baseSeq;

	/**
	 * Number of RTP Packets Expected from this source
	 */
	private long expected;

	/**
	 * No of RTP Packets expected last time a Reception Report was sent
	 */
	private long expectedPrior;

	/**
	 * No of RTP Packets received last time a Reception Report was sent
	 */
	private long receivedPrior;

	/**
	 * Highest Sequence number received from this source
	 */
	private long maxSeq;

	/**
	 * Keep track of the wrapping around of RTP sequence numbers, since RTP Seq No.
	 * are only 16 bits
	 */
	private long cycles;

	/**
	 * Since Packets lost is a 24 bit number, it should be clamped at WRAPMAX =
	 * 0xFFFFFFFF
	 */
	private long WRAPMAX = 0xFFFFFFFF;
	
	/**
	 * Constructor requires an SSRC for it to be a valid source. The constructor
	 * initializes all the source class members to a default value
	 *
	 * @param sourceSSRC SSRC of the new source
	 */

	Source(long sourceSSRC) {
		long time = Session.CurrentTime();
		setSsrc(sourceSSRC);
		setFraction(0);
		setLost(0);
		setLastSeq(0);
		setJitter(0);
		setLst(0);
		setDlsr(0);
		setActiveSender(false);
		setTimeOfLastRTCPArrival(time);
		setTimeOfLastRTPArrival(time);
		setTimeOfLastSRRcvd(time);
		setNoOfRTPPacketsRcvd(0);
		setBaseSeq(0);
		setExpectedPrior(0);
		setReceivedPrior(0);
	}

	public long getSsrc() {
		return ssrc;
	}

	public void setSsrc(long ssrc) {
		this.ssrc = ssrc;
	}

	public double getFraction() {
		return fraction;
	}

	public void setFraction(double fraction) {
		this.fraction = fraction;
	}

	public long getLost() {
		return lost;
	}

	public void setLost(long lost) {
		this.lost = lost;
	}

	public long getLastSeq() {
		return lastSeq;
	}

	public void setLastSeq(long lastSeq) {
		this.lastSeq = lastSeq;
	}

	public long getJitter() {
		return jitter;
	}

	public void setJitter(long jitter) {
		this.jitter = jitter;
	}

	public long getLst() {
		return lst;
	}

	public void setLst(long lst) {
		this.lst = lst;
	}

	public double getDlsr() {
		return dlsr;
	}

	public void setDlsr(double dlsr) {
		this.dlsr = dlsr;
	}

	public boolean isActiveSender() {
		return activeSender;
	}

	public void setActiveSender(boolean activeSender) {
		this.activeSender = activeSender;
	}

	public double getTimeOfLastRTCPArrival() {
		return timeOfLastRTCPArrival;
	}

	public void setTimeOfLastRTCPArrival(double timeOfLastRTCPArrival) {
		this.timeOfLastRTCPArrival = timeOfLastRTCPArrival;
	}

	public double getTimeOfLastRTPArrival() {
		return timeOfLastRTPArrival;
	}

	public void setTimeOfLastRTPArrival(double timeOfLastRTPArrival) {
		this.timeOfLastRTPArrival = timeOfLastRTPArrival;
	}

	public double getTimeOfLastSRRcvd() {
		return timeOfLastSRRcvd;
	}

	public void setTimeOfLastSRRcvd(double timeOfLastSRRcvd) {
		this.timeOfLastSRRcvd = timeOfLastSRRcvd;
	}

	public int getNoOfRTPPacketsRcvd() {
		return noOfRTPPacketsRcvd;
	}

	public void setNoOfRTPPacketsRcvd(int noOfRTPPacketsRcvd) {
		this.noOfRTPPacketsRcvd = noOfRTPPacketsRcvd;
	}

	public long getBaseSeq() {
		return baseSeq;
	}

	public void setBaseSeq(long baseSeq) {
		this.baseSeq = baseSeq;
	}

	public long getExpected() {
		return expected;
	}

	public void setExpected(long expected) {
		this.expected = expected;
	}

	public long getExpectedPrior() {
		return expectedPrior;
	}

	public void setExpectedPrior(long expectedPrior) {
		this.expectedPrior = expectedPrior;
	}

	public long getReceivedPrior() {
		return receivedPrior;
	}

	public void setReceivedPrior(long receivedPrior) {
		this.receivedPrior = receivedPrior;
	}

	public long getMaxSeq() {
		return maxSeq;
	}

	public void setMaxSeq(long maxSeq) {
		this.maxSeq = maxSeq;
	}

	public long getCycles() {
		return cycles;
	}

	public void setCycles(long cycles) {
		this.cycles = cycles;
	}

	public long getWRAPMAX() {
		return WRAPMAX;
	}

	public void setWRAPMAX(long wRAPMAX) {
		WRAPMAX = wRAPMAX;
	}
	
	/**
	 * Returns the extended maximum sequence for a source considering that sequences
	 * cycle.
	 *
	 * @return Sequence Number
	 *
	 */
	public long getExtendedMax() {
		return (getCycles() + getMaxSeq());
	}

	/**
	 * This safe sequence update function will try to determine if seq has wrapped
	 * over resulting in a new cycle. It sets the cycle -- source level variable
	 * which keeps track of wraparounds.
	 *
	 * @param seq Sequence Number
	 *
	 */
	public void updateSeq(long seq) {
		// If the diferrence between max_seq and seq
		// is more than 1000, then we can assume that
		// cycle has wrapped around.
		if (getMaxSeq() == 0)
			setMaxSeq(seq);
		else {
			if (getMaxSeq() - seq > 0.5 * getWRAPMAX())
				setCycles(getCycles() + getWRAPMAX());

			setMaxSeq(seq);
		}

	}

	/**
	 * Updates the various statistics for this source e.g. Packets Lost, Fraction
	 * lost Delay since last SR etc, according to the data gathered since a last SR
	 * or RR was sent out. This method is called prior to sending a Sender
	 * Report(SR)or a Receiver Report(RR) which will include a Reception Report
	 * block about this source.
	 * 
	 */
	public int UpdateStatistics() {
		// Set all the relevant parameters

		// Calculate the highest sequence number received in an RTP Data Packet from
		// this source
		setLastSeq(getExtendedMax());

		// Number of Packets lost = Number of Packets expected - Number of Packets
		// actually rcvd
		setExpected(getExtendedMax() - getBaseSeq() + 1);
		setLost(getExpected() - getNoOfRTPPacketsRcvd());

		// Clamping at 0xffffff
		if (getLost() > 0xffffff)
			setLost(0xffffff);

		// Calculate the fraction lost
		long expected_interval = getExpected() - getExpectedPrior();
		setExpectedPrior(getExpected());

		long received_interval = getNoOfRTPPacketsRcvd() - getReceivedPrior();
		setReceivedPrior(getNoOfRTPPacketsRcvd());

		long lost_interval = expected_interval - received_interval;

		if (expected_interval == 0 || lost_interval <= 0)
			setFraction(0);
		else
			setFraction((lost_interval << 8) / expected_interval);

		// dlsr - express it in units of 1/65336 seconds
		setDlsr((getTimeOfLastSRRcvd() - Session.CurrentTime()) / 65536);

		return 0;
	}

}
