/**
 * 
 */
package rtspproxy.filter.rewrite;

import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;

import rtspproxy.config.AAAConfigurable;
import rtspproxy.filter.GenericProviderAdapter;
import rtspproxy.rtsp.RtspRequest;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class SimpleUrlRewritingProvider extends GenericProviderAdapter
		implements UrlRewritingProvider, AAAConfigurable {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger
			.getLogger(SimpleUrlRewritingProvider.class);

	// map with url from-->to prefix mapping (used in rewriting request URL)
	private HashMap<String, String> forwardMappings = new HashMap<String, String>();
	
	// map with url from-->to prefix mapping (used in rewriting request URL)
	private HashMap<URL, URL> optionsForwardMappings = new HashMap<URL, URL>();
	
	// map with url to-->from prefix mapping (used in rewriting response URL)
	private HashMap<String, String> reverseMappings = new HashMap<String, String>();
	
	/* (non-Javadoc)
	 * @see rtspproxy.filter.rewrite.UrlRewritingProvider#rewriteRequestUrl(java.net.URL)
	 */
	public UrlRewritingResult rewriteRequestUrl(URL request, RtspRequest.Verb verb, SocketAddress client) {
		UrlRewritingResult result = null;
		URL rewritten = null;
		String req = request.toString();
		
		logger.debug("checking request URL: " + req + ", verb=" + verb);
		
		if(verb == RtspRequest.Verb.OPTIONS) {
			logger.debug("handling OPTIONS request");
			
			if((rewritten = this.optionsForwardMappings.get(request)) != null) {
				logger.debug("found special OPTIONS rewrite URL: " + rewritten);
				
				return new UrlRewritingResult(rewritten);
			}
		}
		for(String prefix : this.forwardMappings.keySet()) {
			if(req.startsWith(prefix)) {
				logger.debug("found prefix match on " + prefix);
				try {
					rewritten = new URL(this.forwardMappings.get(prefix) 
							+ req.substring(prefix.length()));
				} catch(MalformedURLException mue) {
					logger.error("request prefix rewriting caused invalid URL", mue);
				}
			}
		}
		logger.debug("rewritten URL: " + rewritten);
		
		if(rewritten != null)
			result = new UrlRewritingResult(rewritten);
		
		return result;
	}

	/* (non-Javadoc)
	 * @see rtspproxy.filter.rewrite.UrlRewritingProvider#rewriteResponseHeaderUrl(java.net.URL)
	 */
	public URL rewriteResponseHeaderUrl(URL response) {
		URL rewritten = null;
		String resp = response.toString();
		
		logger.debug("checking response URL: " + resp);
		for(String prefix : this.reverseMappings.keySet()) {
			if(resp.startsWith(prefix)) {
				logger.debug("found prefix match on " + prefix);
				try {
					rewritten = new URL(this.reverseMappings.get(prefix) 
							+ resp.substring(prefix.length()));
				} catch(MalformedURLException mue) {
					logger.error("response prefix rewriting caused invalid URL", mue);
				}
			}
		}
		logger.debug("rewritten URL: " + rewritten);
		
		return rewritten;
	}

	public void configure(List<Element> configElements) throws Exception {
		for(Element el : configElements) {
			if(el.getName().equals("mapping")) {
				Element fromEl = el.element("from");
				Element toEl = el.element("to");
				
				if(fromEl == null || toEl == null)
					throw new IllegalArgumentException("no from or to element in mapping configuration");
				
				String from = fromEl.getTextTrim();
				String to = toEl.getTextTrim();
				
				if(from == null || from.length() == 0 || to == null || to.length() == 0)
					throw new IllegalArgumentException("invalid from or to element in mapping configuration");
				
				if(from.endsWith("/"))
					from = from.substring(0, from.length()-1);
				if(to.endsWith("/"))
					to = to.substring(0, to.length()-1);

				this.forwardMappings.put(from, to);
				this.reverseMappings.put(to, from);
			} else if(el.getName().equals("map-options")) {
				Element fromEl = el.element("from");
				Element toEl = el.element("to");
				
				if(fromEl == null || toEl == null)
					throw new IllegalArgumentException("no from or to element in mapping configuration");
				
				String from = fromEl.getTextTrim();
				String to = toEl.getTextTrim();
				
				if(from == null || from.length() == 0 || to == null || to.length() == 0)
					throw new IllegalArgumentException("invalid from or to element in mapping configuration");
				
				URL fromUrl = new URL(from);
				URL toUrl = new URL(to);
				
				this.optionsForwardMappings.put(new URL(fromUrl.getProtocol(), fromUrl.getHost(), 
						fromUrl.getPort(), "/"),
						new URL(toUrl.getProtocol(), toUrl.getHost(), 
								toUrl.getPort(), "/"));
			}
		}
	}

}
