/**
 * 
 */
package rtspproxy.rdt;

import org.apache.mina.common.ByteBuffer;


/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class RdtRttResponsePacket extends RdtControlPacket {

	private int roundtripTimestampSeconds;
	private int roundtripTimestampMicroseconds;
	
	/**
	 * @param type
	 * @param needReliable
	 * @param streamId
	 */
	public RdtRttResponsePacket(int roundtripTimestampSeconds, 
			int roundtripTimestampMicroseconds) {
		super(Type.RttResponse, (byte)0);
		
		this.roundtripTimestampMicroseconds = roundtripTimestampMicroseconds;
		this.roundtripTimestampSeconds = roundtripTimestampSeconds;
	}

	/* (non-Javadoc)
	 * @see rtspproxy.rdt.RdtPacket#toStringHelper(java.lang.StringBuffer)
	 */
	@Override
	protected void toStringHelper(StringBuffer buffer) {
		buffer.append(" roundtripTimestampSeconds=" + roundtripTimestampSeconds);
		buffer.append(" roundtripTimestampMicroseconds=" + roundtripTimestampMicroseconds);
	}

	/**
	 * @return Returns the roundtripTimestampMicroseconds.
	 */
	public int getRoundtripTimestampMicroseconds() {
		return roundtripTimestampMicroseconds;
	}

	/**
	 * @param roundtripTimestampMicroseconds The roundtripTimestampMicroseconds to set.
	 */
	public void setRoundtripTimestampMicroseconds(int roundtripTimestampMicroseconds) {
		this.roundtripTimestampMicroseconds = roundtripTimestampMicroseconds;
	}

	/**
	 * @return Returns the roundtripTimestampSeconds.
	 */
	public int getRoundtripTimestampSeconds() {
		return roundtripTimestampSeconds;
	}

	/**
	 * @param roundtripTimestampSeconds The roundtripTimestampSeconds to set.
	 */
	public void setRoundtripTimestampSeconds(int roundtripTimestampSeconds) {
		this.roundtripTimestampSeconds = roundtripTimestampSeconds;
	}

	@Override
	protected ByteBuffer buildHeader() {
		ByteBuffer buf = ByteBuffer.allocate(11);
		byte marker = 0;
		
		buf.put(marker);
		buf.put(getType().toByteArray());
		
		buf.put(encodeInt(this.roundtripTimestampSeconds));
		buf.put(encodeInt(this.roundtripTimestampMicroseconds));
		buf.limit(buf.position());
		
		return buf;
	}

}
