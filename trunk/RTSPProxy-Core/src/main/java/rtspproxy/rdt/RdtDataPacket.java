/**
 * 
 */
package rtspproxy.rdt;

import org.apache.mina.common.ByteBuffer;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class RdtDataPacket extends RdtPacket {

	private boolean reliable;
	private boolean backToBack;
	private boolean slowData;
	private byte asmRule;
	private int timestamp;
	private short totalReliable;
	private short sequence;

	public RdtDataPacket(boolean needReliable, boolean reliable, byte streamId, short sequence, boolean backToBack, 
			boolean slowData, byte asmRule, int timestamp) {
		super(Type.Data, needReliable, streamId);
		
		this.reliable = reliable;
		this.sequence = sequence;
		this.backToBack = backToBack;
		this.slowData = slowData;
		this.asmRule = asmRule;
		this.timestamp = timestamp;
	}

	/**
	 * @return Returns the asmRule.
	 */
	public byte getAsmRule() {
		return asmRule;
	}

	/**
	 * @param asmRule The asmRule to set.
	 */
	public void setAsmRule(byte asmRule) {
		this.asmRule = asmRule;
	}

	/**
	 * @return Returns the backToBack.
	 */
	public boolean isBackToBack() {
		return backToBack;
	}

	/**
	 * @param backToBack The backToBack to set.
	 */
	public void setBackToBack(boolean backToBack) {
		this.backToBack = backToBack;
	}

	/**
	 * @return Returns the reliable.
	 */
	public boolean isReliable() {
		return reliable;
	}

	/**
	 * @param reliable The reliable to set.
	 */
	public void setReliable(boolean reliable) {
		this.reliable = reliable;
	}

	/**
	 * @return Returns the slowData.
	 */
	public boolean isSlowData() {
		return slowData;
	}

	/**
	 * @param slowData The slowData to set.
	 */
	public void setSlowData(boolean slowData) {
		this.slowData = slowData;
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
	protected void toStringHelper(StringBuffer buffer) {
		buffer.append(" reliable=" + this.reliable);
		buffer.append(" sequence="  + this.sequence);
		buffer.append(" backToback="+ backToBack);
		buffer.append(" slowData= " + slowData);
		buffer.append(" asmRule=" + asmRule);
		buffer.append(" timestamp=" + timestamp);
		buffer.append(" totalReliable=" + totalReliable);
	}

	/**
	 * @return Returns the sequence.
	 */
	public short getSequence() {
		return sequence;
	}

	/**
	 * @param sequence The sequence to set.
	 */
	public void setSequence(short sequence) {
		this.sequence = sequence;
	}

	@Override
	protected ByteBuffer buildHeader() {
		ByteBuffer buf = ByteBuffer.allocate(8, true);
		byte marker = 0, control = 0;
		
		if(isLengthIncluded())
			marker |= (1<<7);
		if(isNeedReliable())
			marker |= (1<<6);
		marker |= (getStreamId() << 1);
		if(this.reliable)
			marker |= (1<<0);		
		buf.put(marker);		

		buf.put(getType().toByteArray());

		if(isLengthIncluded()) {
			short length = 8;
			
			if(isNeedReliable())
				length += 2;
			
			buf.put(encodeShort(length));
		}
		
		if(this.backToBack)
			control |= (1<<7);
		if(slowData)
			control |= (1<<6);
		control |= this.asmRule;
		buf.put(control);
		
		buf.put(encodeInt(this.timestamp));

		if(isNeedReliable())
			buf.put(encodeShort(this.totalReliable));
		
		return buf;
	}

}
