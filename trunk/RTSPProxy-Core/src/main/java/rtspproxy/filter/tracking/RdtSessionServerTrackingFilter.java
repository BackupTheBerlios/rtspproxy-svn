/**
 * 
 */
package rtspproxy.filter.tracking;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.rtsp.RtspTransport;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class RdtSessionServerTrackingFilter extends RdtSessionTrackingFilter {

	private static Logger logger = LoggerFactory.getLogger(RdtSessionClientTrackingFilter.class);
	
	/**
	 * @param typeName
	 */
	public RdtSessionServerTrackingFilter() {
		super("server");
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.common.IoFilterAdapter#messageReceived(org.apache.mina.common.IoFilter.NextFilter, org.apache.mina.common.IoSession, java.lang.Object)
	 */
	@Override
	public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
		handleMessage(session, message);
		
		nextFilter.messageReceived(session, message);
	}

	@Override
	protected void handleTransportRdtUdpUnicast(IoSession session, RtspTransport transport) {
		logger.debug("handling server-side RDT/UDP/unicast header, header=" + transport);
		
		session.setAttribute(RdtSessionToken.SessionAttribute, new RdtSessionToken(session.getRemoteAddress(),
				transport.getServerPort()[0]));
	}

	
}
