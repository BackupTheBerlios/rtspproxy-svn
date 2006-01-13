/**
 * 
 */
package rtspproxy.rdt;

import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class RdtProtocolEncoder implements ProtocolEncoder {

	private static Logger logger = LoggerFactory.getLogger(RdtProtocolEncoder.class);
	
	/**
	 * 
	 */
	RdtProtocolEncoder() {
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.filter.codec.ProtocolEncoder#encode(org.apache.mina.common.IoSession, java.lang.Object, org.apache.mina.filter.codec.ProtocolEncoderOutput)
	 */
	public void encode(IoSession ioSession, Object packet, ProtocolEncoderOutput out)
			throws Exception {
		RdtPacket rdtPacket = (RdtPacket)packet;

		if(logger.isDebugEnabled())
			logger.debug("sending RDP packet: " + rdtPacket + " to client " + ioSession.getRemoteAddress());
		
		out.write(rdtPacket.toByteBuffer());
		// out.flush();
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.filter.codec.ProtocolEncoder#dispose(org.apache.mina.common.IoSession)
	 */
	public void dispose(IoSession arg0) throws Exception {
	}

}
