/**
 * 
 */
package rtspproxy.filter.rewrite;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.config.Config;
import rtspproxy.filter.FilterBase;
import rtspproxy.jmx.JmxManageable;
import rtspproxy.jmx.JmxManageable2;
import rtspproxy.lib.Side;
import rtspproxy.proxy.ProxyHandler;
import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;

/**
 * @author bieniekr
 * 
 */
public class UrlRewritingFilter extends FilterBase<UrlRewritingProvider>
{

    /** Logger for this class */
    private static Logger log = LoggerFactory.getLogger( UrlRewritingFilter.class );

    private static final String FilterNAME = "rewriting";

    /** the filter backend instance */
    private UrlRewritingProvider provider;

    // list of exposed session attributes
    private String[] exposedAttributes;

    private Side side;

    /**
     * construct the IoFilter around the filter class denoted by the clazz name
     * parameter.
     */
    public UrlRewritingFilter( Side side )
    {
        this.side = side;
        // this.exposedAttributes = this.provider.getWantedSessionAttributes();
        // if ( this.exposedAttributes == null )
        this.exposedAttributes = new String[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.FilterBase#getName()
     */
    @Override
    public String getName()
    {
        return FilterNAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.FilterBase#setProvider(rtspproxy.filter.GenericProvider)
     */
    @Override
    public void setProvider( UrlRewritingProvider provider )
    {
        this.provider = provider;
        exposedAttributes = provider.getWantedSessionAttributes();
        if ( exposedAttributes == null )
            exposedAttributes = new String[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.FilterBase#getProviderInterface()
     */
    @Override
    public Class<UrlRewritingProvider> getProviderInterface()
    {
        return UrlRewritingProvider.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.FilterBase#getProviderClassName()
     */
    @Override
    public String getProviderClassName()
    {
        return Config.filtersRewriteImplClass.getValue();
    }

    /**
     * process a request message
     * 
     * @return true if the caller should pass the message on, false if the
     *         message should not be passed on
     */
    protected boolean processRequest( IoSession session, RtspRequest req )
    {
        boolean passOn = true;

        if ( req.getUrl() != null ) {
            Map<String, Object> exposedSessionAttributes = new HashMap<String, Object>();

            for ( String attr : this.exposedAttributes ) {
                log.debug( "exposing session attribute: {}", attr );
                if ( session.containsAttribute( attr ) ) {
                    Object o = session.getAttribute( attr );

                    log.debug( "attribute {} found in session, val={}", attr, o );
                    exposedSessionAttributes.put( attr, o );
                }

                if ( ProxyHandler.containsSharedSessionAttribute( session, attr ) ) {
                    Object o = ProxyHandler.getSharedSessionAttribute( session, attr );

                    log.debug( "attribute {} found in shared session map, val={}", attr,
                            o );
                    exposedSessionAttributes.put( attr, o );
                }
            }

            UrlRewritingResult result = this.provider.rewriteRequestUrl( req.getUrl(),
                    req.getVerb(), session.getRemoteAddress(), req.getHeaders(),
                    exposedSessionAttributes );

            if ( result != null ) {
                URL rewritten = result.getRewrittenUrl();

                if ( rewritten != null ) {
                    log.debug( "changed request URL from '{}' to '{}'", req.getUrl(),
                            rewritten );

                    req.setUrl( rewritten );
                } else if ( result.getResponse() != null ) {
                    RtspResponse resp = result.getResponse();

                    resp.setCommonHeaders();
                    resp.setSequenceNumber( req.getSequenceNumber() );
                    if ( resp.getHeader( "Session" ) != null )
                        resp.setHeader( "Session", req.getHeader( "Session" ) );
                    log.debug( "dropped  request, return response: {}", resp );

                    session.write( resp );
                    passOn = false;
                }
            }
        }

        return passOn;
    }

    /**
     * process a response message
     */
    protected void processResponse( RtspResponse resp )
    {
        switch ( resp.getRequestVerb() )
        {
        case DESCRIBE:
            rewriteUrlHeader( "Content-base", resp );
            break;
        case PLAY:
            // rewriteUrlHeader("RTP-Info", resp);
            break;
        }
    }

    @Override
    public void messageReceived( NextFilter nextFilter, IoSession session, Object message )
            throws Exception
    {
        boolean passOn = true;

        log.debug( "Received (pre-rewriting) message:\n{}", message );

        if ( isRunning() ) {
            if ( message instanceof RtspMessage ) {
                RtspMessage rtspMessage = (RtspMessage) message;

                if ( side == Side.Client
                        && rtspMessage.getType() == RtspMessage.Type.TypeRequest )
                    passOn = processRequest( session, (RtspRequest) rtspMessage );

                else if ( side == Side.Server
                        && rtspMessage.getType() == RtspMessage.Type.TypeResponse )
                    processResponse( (RtspResponse) rtspMessage );

            } else {
                log.error( "Expecting a RtspMessage. Received a {}", //
                        message.getClass().getName() );
            }

        }

        if ( passOn ) {
            log.debug( "Sent (post-rewriting) message:\n{}", message );
            nextFilter.messageReceived( session, message );
        }
    }

    /**
     * rewrite a header
     */
    private void rewriteUrlHeader( String headerName, RtspResponse resp )
    {
        String oldHeader = resp.getHeader( headerName );

        if ( oldHeader != null ) {
            log.debug( "old content: {} header value: {}", headerName, oldHeader );

            try {
                URL header = this.provider
                        .rewriteResponseHeaderUrl( new URL( oldHeader ) );

                if ( header != null ) {
                    log.debug( "changed header {} to {}", headerName, header );

                    resp.setHeader( headerName, header.toString() );
                }
            } catch ( MalformedURLException mue ) {
                log.error( "failed to parse {} header: {}", headerName, mue );
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.jmx.JmxManageable#setMBeanServer(javax.management.MBeanServer)
     */
    public void setMBeanServer( MBeanServer mbeanServer )
    {
        if ( this.provider instanceof JmxManageable )
            ((JmxManageable) this.provider).setMBeanServer( mbeanServer );
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.FilterBase#getDetailMBean()
     */
    @Override
    public ObjectName getDetailMBean()
    {
        ObjectName name = null;

        if ( this.provider instanceof JmxManageable2 )
            name = ((JmxManageable2) this.provider).getMBean();

        return name;
    }
}
