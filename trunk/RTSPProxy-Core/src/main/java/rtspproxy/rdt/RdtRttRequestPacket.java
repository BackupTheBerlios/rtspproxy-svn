/**
 * 
 */
package rtspproxy.rdt;

import org.apache.mina.common.ByteBuffer;


/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class RdtRttRequestPacket extends RdtControlPacket {

	/**
	 * @param type
	 * @param needReliable
	 * @param streamId
	 */
	public RdtRttRequestPacket() {
		super(Type.RttRequest, (byte)0);
	}

	/* (non-Javadoc)
	 * @see rtspproxy.rdt.RdtPacket#toStringHelper(java.lang.StringBuffer)
	 */
	@Override
	protected void toStringHelper(StringBuffer buffer) {
	}

	@Override
	protected ByteBuffer buildHeader() {
		ByteBuffer buf = ByteBuffer.allocate(3, true);
		byte marker = 0;
		
		buf.put(marker);
		buf.put(getType().toByteArray());
		
		return buf;
	}

}
