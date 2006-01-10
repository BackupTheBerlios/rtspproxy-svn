/**
 * 
 */
package rtspproxy.rdt;

import org.apache.mina.common.ByteBuffer;


/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class RdtLatencyReportPacket extends RdtControlPacket {

	private int serverTimeout;

	/**
	 * @param type
	 * @param needReliable
	 * @param streamId
	 */
	public RdtLatencyReportPacket(int serverTimeout) {
		super(Type.LatencyReport, (byte)0);
		
		this.serverTimeout = serverTimeout;
	}

	/* (non-Javadoc)
	 * @see rtspproxy.rdt.RdtPacket#toStringHelper(java.lang.StringBuffer)
	 */
	@Override
	protected void toStringHelper(StringBuffer buffer) {
		buffer.append(" serverTimeout=" + this.serverTimeout);
	}

	/**
	 * @return Returns the serverTimeout.
	 */
	public int getServerTimeout() {
		return serverTimeout;
	}

	/**
	 * @param serverTimeout The serverTimeout to set.
	 */
	public void setServerTimeout(int serverTimeout) {
		this.serverTimeout = serverTimeout;
	}
	
	@Override
	protected ByteBuffer buildHeader() {
		ByteBuffer buf = ByteBuffer.allocate(3);
		byte marker = 0;
		
		buf.setAutoExpand(true);
		if(isLengthIncluded())
			marker |= (1<<7) | (1<<1); // bit 1 is needed because it appears in the trace
		buf.put(marker);
		buf.put(getType().toByteArray());
		
		if(isLengthIncluded()) {
			// add 2 bytes for length to packet size of 3
			// TODO find a cleaner way.
			buf.put(encodeShort((short)9));
		}
		buf.put(encodeInt(this.serverTimeout));
		buf.limit(buf.position());
		
		return buf;
	}

}
