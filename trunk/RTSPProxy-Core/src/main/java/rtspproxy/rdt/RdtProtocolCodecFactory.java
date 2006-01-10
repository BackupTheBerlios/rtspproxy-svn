/**
 * 
 */
package rtspproxy.rdt;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * Codec factory for RDT protocol coder and decoder. Uses shared codec instances because
 * RDT PDU are self-contained and do not contain states across PDUs
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 */
public class RdtProtocolCodecFactory implements ProtocolCodecFactory {

	private RdtProtocolEncoder encoder = new RdtProtocolEncoder();
	
	private RdtProtocolDecoder decoder = new RdtProtocolDecoder();
	
	/**
	 * 
	 */
	RdtProtocolCodecFactory() {
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.filter.codec.ProtocolCodecFactory#getEncoder()
	 */
	public ProtocolEncoder getEncoder() {
		return encoder;
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.filter.codec.ProtocolCodecFactory#getDecoder()
	 */
	public ProtocolDecoder getDecoder() {
		return decoder;
	}

}
