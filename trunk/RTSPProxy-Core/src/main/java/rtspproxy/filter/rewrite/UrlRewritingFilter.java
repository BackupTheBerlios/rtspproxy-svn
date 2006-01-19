/**
 * 
 */
package rtspproxy.filter.rewrite;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.mina.common.IoFilter;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoFilter.NextFilter;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.filter.FilterBase;
import rtspproxy.jmx.JmxManageable;
import rtspproxy.jmx.JmxManageable2;
import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;

/**
 * @author bieniekr
 * 
 */
public abstract class UrlRewritingFilter extends FilterBase implements JmxManageable {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(UrlRewritingFilter.class);

	public static final String FilterNAME = "rewriting";

	// the filter instance
	protected UrlRewritingProvider provider;

	/**
	 * construct the IoFilter around the filter class denoted by the clazz name
	 * parameter.
	 */
	public UrlRewritingFilter(String className, List<Element> configElements)
			throws Exception {
		super(FilterNAME, className, "rewriting");

		this.provider = (UrlRewritingProvider)loadConfigInitProvider(className, 
				UrlRewritingProvider.class, 
				configElements);
	}

	public abstract void messageReceived(NextFilter nextFilter, IoSession session,
			Object message) throws Exception;
	
	/**
	 * process a request message
	 * @return true if the caller should pass the message on, false if the message should not be
	 * passed on
	 */
	protected boolean processRequest(IoSession session, RtspRequest req) {
		boolean passOn = true;
		
		if (req.getUrl() != null) {
			UrlRewritingResult result = this.provider.rewriteRequestUrl(req.getUrl(), req.getVerb(), 
					session.getRemoteAddress(), req.getHeaders()); 
			
			if(result != null) {
				URL rewritten = result.getRewrittenUrl();

				if (rewritten != null) {
					logger.debug("changed request URL from '" + req.getUrl()
						+ "' to '" + rewritten + "'");

					req.setUrl(rewritten);
				} else if(result.getResponse() != null) {
					RtspResponse resp = result.getResponse();

					resp.setCommonHeaders();
					resp.setSequenceNumber(req.getSequenceNumber());
					logger.debug("dropped  request, return response: " + resp);

					session.write(resp);
					passOn = false;
				}
			}
		}
		
		return passOn;
	}
	
	/**
	 * process a response message
	 */
	protected void processResponse(RtspResponse resp) {
		switch (resp.getRequestVerb()) {
		case DESCRIBE:
			rewriteUrlHeader("Content-base", resp);
			break;
		case PLAY:
			// rewriteUrlHeader("RTP-Info", resp);
			break;
		}		
	}
	
	/**
	 * rewrite a header
	 */
	private void rewriteUrlHeader(String headerName, RtspResponse resp) {
		String oldHeader = resp.getHeader(headerName);

		if (oldHeader != null) {
			logger.debug("old content " + headerName + " header value: "
					+ oldHeader);

			try {
				URL header = this.provider.rewriteResponseHeaderUrl(new URL(
						oldHeader));

				if (header != null) {
					logger.debug("changed header " + headerName + " to "
							+ header);

					resp.setHeader(headerName, header.toString());
				}
			} catch (MalformedURLException mue) {
				logger.error("failed to parse " + headerName + " header", mue);
			}
		}
	}

	/* (non-Javadoc)
	 * @see rtspproxy.jmx.JmxManageable#setMBeanServer(javax.management.MBeanServer)
	 */
	public void setMBeanServer(MBeanServer mbeanServer) {
		if(this.provider instanceof JmxManageable)
			((JmxManageable)this.provider).setMBeanServer(mbeanServer);
	}

	/* (non-Javadoc)
	 * @see rtspproxy.filter.FilterBase#getDetailMBean()
	 */
	@Override
	public ObjectName getDetailMBean() {
		ObjectName name = null;
		
		if(this.provider instanceof JmxManageable2)
			name = ((JmxManageable2)this.provider).getMBean();
		
		return name;
	}
}
