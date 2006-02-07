/**
 * 
 */
package rtspproxy.rtp.range;

import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.filter.ThreadPoolFilter;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class RtpRtcpFilterChainBuilder implements IoFilterChainBuilder {

	private ThreadPoolFilter filter = new ThreadPoolFilter("RtpRtcpPortRangeThreadPool");
	
	RtpRtcpFilterChainBuilder() {}
	
	/* (non-Javadoc)
	 * @see org.apache.mina.common.IoFilterChainBuilder#buildFilterChain(org.apache.mina.common.IoFilterChain)
	 */
	public void buildFilterChain(IoFilterChain arg0) throws Exception {
	}

	void setPoolSize(int size) {
		this.filter.setMaximumPoolSize(size);
	}
}
