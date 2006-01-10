/**
 * 
 */
package rtspproxy.rdt;

import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;

/**
 * Builds filter chain operating on RDT connection.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class RdtFilterChainBuilder implements IoFilterChainBuilder {

	private RdtProtocolCodecFactory codecFactory = new RdtProtocolCodecFactory();
	
	// filter name fields
	public static final String rdtCODEC = "rdtProtocolCodec";
	
	/**
	 * 
	 */
	public RdtFilterChainBuilder() {
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.common.IoFilterChainBuilder#buildFilterChain(org.apache.mina.common.IoFilterChain)
	 */
	public void buildFilterChain(IoFilterChain chain) throws Exception {
		
		chain.addLast(rdtCODEC, new ProtocolCodecFilter(codecFactory));
	}

}
