/**
 * 
 */
package rtspproxy.filter.rewrite;

import java.util.List;

import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoFilter.NextFilter;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class ClientUrlRewritingFilter extends UrlRewritingFilter {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(ClientUrlRewritingFilter.class);

	public ClientUrlRewritingFilter(String className, List<Element> configElements) throws Exception {
		super(className, configElements);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.mina.common.IoFilterAdapter#messageReceived(org.apache.mina.common.IoFilter.NextFilter,
	 *      org.apache.mina.common.IoSession, java.lang.Object)
	 */
	@Override
	public void messageReceived(NextFilter nextFilter, IoSession session,
			Object message) throws Exception {
		boolean passOn = true;
		
		logger.debug("Received (pre-rewriting) message:\n" + message);

		if (isRunning()) {
			if (message instanceof RtspMessage) {
				RtspMessage rtspMessage = (RtspMessage) message;

				if (rtspMessage.getType() == RtspMessage.Type.TypeRequest)
					passOn = processRequest(session, (RtspRequest)rtspMessage);
			} else {
					logger.error("Expecting a RtspMessage. Received a "
						+ message.getClass().getName());
			}
			logger.debug("Sent (post-rewriting) message:\n" + message);

		}
		if(passOn)
			nextFilter.messageReceived(session, message);
	}


}
