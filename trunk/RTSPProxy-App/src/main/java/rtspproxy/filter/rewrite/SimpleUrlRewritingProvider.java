/**
 * 
 */
package rtspproxy.filter.rewrite;

import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.config.ListParameter;
import rtspproxy.rtsp.RtspRequest;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 * 
 */
public class SimpleUrlRewritingProvider implements UrlRewritingProvider
{

    private static final Logger log = LoggerFactory
            .getLogger( SimpleUrlRewritingProvider.class );

    /** map with url from-->to prefix mapping (used in rewriting request URL) */
    private Map<String, String> forwardMappings = new HashMap<String, String>();

    // /** map with url from-->to prefix mapping (used in rewriting request URL)
    // */
    // private Map<URL, URL> optionsForwardMappings = new HashMap<URL, URL>();

    /** map with url to-->from prefix mapping (used in rewriting response URL) */
    private Map<String, String> reverseMappings = new HashMap<String, String>();

    private final ListParameter<RewriteMapping> rewriteRules;

    public SimpleUrlRewritingProvider()
    {
        rewriteRules = new ListParameter<RewriteMapping>(
                "filters.rewrite.rules.mapping", // name
                false, // mutable
                RewriteMapping.class, // parameter class
                "Rewriting rules" );
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.rewrite.UrlRewritingProvider#rewriteRequestUrl(java.net.URL)
     */
    public UrlRewritingResult rewriteRequestUrl( URL request, RtspRequest.Verb verb,
            SocketAddress client, Map<String, String> requestHeaders,
            Map<String, Object> exposedSessionAttributes )
    {
        UrlRewritingResult result = null;
        URL rewritten = null;
        String req = request.toString();

        log.debug( "checking request URL: {}, verb={}", req, verb );

        // TODO: OPTIONS mapping
        /*
         * if ( verb == RtspRequest.Verb.OPTIONS ) { log.debug( "handling
         * OPTIONS request" );
         * 
         * if ( (rewritten = this.optionsForwardMappings.get( request )) != null ) {
         * log.debug( "found special OPTIONS rewrite URL: {}", rewritten );
         * 
         * return new UrlRewritingResult( rewritten ); } }
         */
        for ( String prefix : forwardMappings.keySet() ) {
            if ( req.startsWith( prefix ) ) {
                log.debug( "found prefix match on {}", prefix );
                try {
                    rewritten = new URL( forwardMappings.get( prefix )
                            + req.substring( prefix.length() ) );

                } catch ( MalformedURLException mue ) {
                    log.error( "request prefix rewriting caused invalid URL", mue );
                }
            }
        }
        log.debug( "rewritten URL: {}", rewritten );

        if ( rewritten != null )
            result = new UrlRewritingResult( rewritten );

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.GenericProvider#start()
     */
    public void start() throws Exception
    {
        for ( RewriteMapping rewriteMap : rewriteRules.getElementsList() ) {
            forwardMappings.put( rewriteMap.getFrom(), rewriteMap.getTo() );
            reverseMappings.put( rewriteMap.getTo(), rewriteMap.getFrom() );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.GenericProvider#stop()
     */
    public void stop()
    {
        forwardMappings.clear();
        reverseMappings.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.rewrite.UrlRewritingProvider#rewriteResponseHeaderUrl(java.net.URL)
     */
    public URL rewriteResponseHeaderUrl( URL response )
    {
        URL rewritten = null;
        String resp = response.toString();

        log.debug( "checking response URL: {}", resp );

        for ( String prefix : reverseMappings.keySet() ) {
            if ( resp.startsWith( prefix ) ) {
                log.debug( "found prefix match on {}", prefix );
                String url = reverseMappings.get( prefix )
                        + resp.substring( prefix.length() );
                try {
                    rewritten = new URL( url );
                } catch ( MalformedURLException mue ) {
                    log.error( "response prefix rewriting caused invalid URL: {}", url );
                }
            }
        }

        log.info( "rewritten URL: {}", rewritten );
        return rewritten;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.GenericProvider#configure(org.apache.commons.configuration.Configuration)
     */
    public void configure( Configuration configuration ) throws Exception
    {
        rewriteRules.readConfiguration( configuration );
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.rewrite.UrlRewritingProvider#getWantedSessionAttributes()
     */
    public String[] getWantedSessionAttributes()
    {
        // no attributes wanted
        return null;
    }

}
