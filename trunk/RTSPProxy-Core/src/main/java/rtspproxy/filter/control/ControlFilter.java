/**
 * 
 */
package rtspproxy.filter.control;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.filter.FilterBase;
import rtspproxy.lib.Side;
import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 * 
 */
public class ControlFilter extends FilterBase<ControlProvider>
{

    private static Logger log = LoggerFactory.getLogger( ControlFilter.class );

    public static final String FilterNAME = "controlFilter";

    private List<ControlProvider> providers;

    private Side side = Side.Client;

    @Override
    public String getName()
    {
        return FilterNAME;
    }

    public ControlFilter( Side side )
    {
        this.side = side;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.common.IoFilterAdapter#sessionClosed(org.apache.mina.common.IoFilter.NextFilter,
     *      org.apache.mina.common.IoSession)
     */
    @Override
    public void sessionClosed( NextFilter nextFilter, IoSession session )
            throws Exception
    {
        for ( ControlProvider provider : providers )
            provider.sessionClosed( session );

        nextFilter.sessionClosed( session );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.common.IoFilterAdapter#sessionOpened(org.apache.mina.common.IoFilter.NextFilter,
     *      org.apache.mina.common.IoSession)
     */
    @Override
    public void sessionOpened( NextFilter nextFilter, IoSession session )
            throws Exception
    {
        for ( ControlProvider provider : providers )
            provider.sessionOpened( session );

        nextFilter.sessionOpened( session );
    }

    @Override
    public void messageReceived( NextFilter nextFilter, IoSession session, Object message )
            throws Exception
    {
        if ( message instanceof RtspRequest ) {
            for ( ControlProvider provider : providers )
                provider.receivedRequest( session, (RtspRequest) message );
        } else if ( message instanceof RtspResponse ) {
            for ( ControlProvider provider : providers )
                provider.receivedResponse( session, (RtspResponse) message );
        } else {
            log.error( "Expecting a RtspRequest. Received a {}", message.getClass()
                    .getName() );
        }

        // Forward message
        nextFilter.messageReceived( session, message );
    }

    @Override
    public void doConfigure( Configuration configuration )
    {
        log.debug( "Configuring control filter." );
        // TODO: implement it!
    }

}
