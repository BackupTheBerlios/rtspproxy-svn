/**
 * 
 */
package rtspproxy.rdt;

import org.apache.mina.common.ByteBuffer;


/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class RdtStreamEndPacket extends RdtPacket {

	private boolean packetSent;
	private boolean extFlag;
	private short streamEndSequenceNumber;
	private int timestamp;
	private short totalReliable;

	/**
	 * @param type
	 * @param needReliable
	 * @param streamId
	 */
	public RdtStreamEndPacket(boolean needReliable, byte streamId, boolean packetSent, boolean extFlag,
			short streamEndSequenceNumber, int timestamp, short totalReliable) {
		super(Type.StreamEnd, needReliable, streamId);
		this.packetSent = packetSent;
		this.extFlag = extFlag;
		this.streamEndSequenceNumber = streamEndSequenceNumber;
		this.timestamp = timestamp;
		this.totalReliable = totalReliable;
	}

	/* (non-Javadoc)
	 * @see rtspproxy.rdt.RdtPacket#toStringHelper(java.lang.StringBuffer)
	 */
	@Override
	protected void toStringHelper(StringBuffer buffer) {
		buffer.append(" packetSent=" + packetSent);
		buffer.append(" extFlag=" + extFlag);
		buffer.append(" streamEndSequenceNumber=" + streamEndSequenceNumber);
		buffer.append(" timestamp=" + timestamp);
		buffer.append(" totalReliable=" + totalReliable);
	}

	/**
	 * @return Returns the extFlag.
	 */
	public boolean isExtFlag() {
		return extFlag;
	}

	/**
	 * @param extFlag The extFlag to set.
	 */
	public void setExtFlag(boolean extFlag) {
		this.extFlag = extFlag;
	}

	/**
	 * @return Returns the packetSent.
	 */
	public boolean isPacketSent() {
		return packetSent;
	}

	/**
	 * @param packetSent The packetSent to set.
	 */
	public void setPacketSent(boolean packetSent) {
		this.packetSent = packetSent;
	}

	/**
	 * @return Returns the streamEndSequenceNumber.
	 */
	public short getStreamEndSequenceNumber() {
		return streamEndSequenceNumber;
	}

	/**
	 * @param streamEndSequenceNumber The streamEndSequenceNumber to set.
	 */
	public void setStreamEndSequenceNumber(short streamEndSequenceNumber) {
		this.streamEndSequenceNumber = streamEndSequenceNumber;
	}

	/**
	 * @return Returns the timestamp.
	 */
	public int getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp The timestamp to set.
	 */
	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return Returns the totalReliable.
	 */
	public short getTotalReliable() {
		return totalReliable;
	}

	/**
	 * @param totalReliable The totalReliable to set.
	 */
	public void setTotalReliable(short totalReliable) {
		this.totalReliable = totalReliable;
	}

	@Override
	protected ByteBuffer buildHeader() {
		ByteBuffer buf = ByteBuffer.allocate(11, true);
		byte marker = 0;
		
		if(isNeedReliable())
			marker |= (1<<7);
		marker |= getStreamId() << 2;
		if(packetSent)
			marker |= (1<<1);
		if(extFlag)
			marker |= (1<<0);
		
		buf.put(marker);
		buf.put(getType().toByteArray());
		
		buf.put(encodeShort(this.streamEndSequenceNumber));
		buf.put(encodeInt(this.timestamp));
		buf.put(encodeShort(this.totalReliable));
		
		return buf;
	}

}
