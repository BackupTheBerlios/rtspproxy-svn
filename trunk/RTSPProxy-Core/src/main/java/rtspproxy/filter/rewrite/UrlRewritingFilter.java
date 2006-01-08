/**
 * 
 */
package rtspproxy.filter.rewrite;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.mina.common.IoFilter;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoFilter.NextFilter;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.filter.FilterBase;
import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;

/**
 * @author bieniekr
 * 
 */
public abstract class UrlRewritingFilter extends FilterBase {
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
	 */
	protected void processRequest(RtspRequest req) {
		if (req.getUrl() != null) {
			URL rewritten = this.provider.rewriteRequestUrl(req.getUrl());

			if (rewritten != null) {
				logger.debug("changed request URL from '" + req.getUrl()
						+ "' to '" + rewritten + "'");

				req.setUrl(rewritten);
			}
		}
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
}
