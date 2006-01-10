package rtspproxy.rdt;

import org.apache.mina.common.ByteBuffer;


public class RdtAckPacket extends RdtControlPacket {

	private boolean lostHigh;

	public RdtAckPacket(boolean lostHigh) {
		super(Type.Ack, (byte)0);
		
		this.lostHigh = lostHigh;
	}

	@Override
	protected void toStringHelper(StringBuffer buffer) {
		buffer.append(" lostHigh=" + lostHigh);
	}

	/**
	 * @return Returns the lostHigh.
	 */
	public boolean isLostHigh() {
		return lostHigh;
	}

	/**
	 * @param lostHigh The lostHigh to set.
	 */
	public void setLostHigh(boolean lostHigh) {
		this.lostHigh = lostHigh;
	}

	@Override
	protected ByteBuffer buildHeader() {
		ByteBuffer buf = ByteBuffer.allocate(3);
		byte marker = 0;

		buf.setAutoExpand(true);
		if(isLengthIncluded())
			marker |= (1<<7);
		if(this.lostHigh)
			marker |= (1<<6);
		buf.put(marker);
		buf.put(getType().toByteArray());
		
		if(isLengthIncluded()) {
			// add 2 bytes for length to packet size of 3
			// TODO find a cleaner way.
			buf.put(encodeShort((short)5));
		}
		buf.limit(buf.position());
		
		return buf;
	}

}
