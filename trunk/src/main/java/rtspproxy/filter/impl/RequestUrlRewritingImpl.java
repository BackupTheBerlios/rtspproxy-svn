/**
 * 
 */
package rtspproxy.filter.impl;

import java.net.URL;

import org.apache.log4j.Logger;

import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoFilter.NextFilter;

import rtspproxy.filter.RequestUrlRewritingFilter;
import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;

/**
 * @author bieniekr
 *
 */
public class RequestUrlRewritingImpl extends IoFilterAdapter {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger
			.getLogger(RequestUrlRewritingImpl.class);

	// the filter instance
	private RequestUrlRewritingFilter filter;
	
	/**
	 * construct the IoFilter around the filter class denoted by the clazz name parameter.
	 * 
	 * TODO: This may become obsolete if moving to OSGi bundles
	 * TODO: Make filter parametrizeable. Could be done by moving from properties to XML config file.
	 */
	public RequestUrlRewritingImpl(String clazzName) throws Exception {
		try {
			Class filterClazz = Class.forName(clazzName);
			
			this.filter = (RequestUrlRewritingFilter)filterClazz.newInstance();
			logger.info("using request URL rewriter " + clazzName);
		} catch(Exception e) {
			logger.error(e);
			
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.common.IoFilterAdapter#messageReceived(org.apache.mina.common.IoFilter.NextFilter, org.apache.mina.common.IoSession, java.lang.Object)
	 */
	@Override
	public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
		RtspMessage rtspMessage = (RtspMessage) message;

		logger.debug( "Received message:\n" + message );
		if(rtspMessage.getType() == RtspMessage.Type.TypeRequest) {
			RtspRequest request = (RtspRequest)rtspMessage;
			URL rewritten = this.filter.rewriteUrl(request.getUrl());
			
			if(rewritten != null) {
				logger.debug("changed request URL from '" + request.getUrl() + "' to '" + rewritten + "'");
				
				request.setUrl(rewritten);
			}
		}
		
		nextFilter.messageReceived(session, message);
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.common.IoFilterAdapter#messageSent(org.apache.mina.common.IoFilter.NextFilter, org.apache.mina.common.IoSession, java.lang.Object)
	 */
	@Override
	public void messageSent(NextFilter nextFilter, IoSession session, Object message) throws Exception {
		RtspMessage rtspMessage = (RtspMessage) message;

		logger.debug("Sent message:\n" + message );
		
		nextFilter.messageSent(session, message);
	}
}
