/**
 * 
 */
package rtspproxy.filter.rewrite;

import java.net.URL;

import rtspproxy.rtsp.RtspResponse;

/**
 * This object is passed back as a result of the URL request rewriting process.
 * It should contain either a modified URL or a RTSP response message o be returned
 * to the client.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class UrlRewritingResult {
	
	// rewritten URL
	private URL rewrittenUrl;
	
	// response object
	private RtspResponse response;
	
	/**
	 * construct with URL
	 */
	public UrlRewritingResult(URL rewrittenUrl) {
		this.rewrittenUrl = rewrittenUrl;
	}
	
	/**
	 * construct with response
	 */
	public UrlRewritingResult(RtspResponse response) {
		this.response = response;
	}

	public RtspResponse getResponse() {
		return response;
	}

	public URL getRewrittenUrl() {
		return rewrittenUrl;
	}
}
