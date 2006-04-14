/**
 * 
 */
package rtspproxy.filter.control;

import java.util.List;

import org.apache.mina.common.IoSession;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 * 
 */
public class ServerControlFilter extends ControlFilter
{

    private static Logger log = LoggerFactory.getLogger( ClientControlFilter.class );

    /**
     * @param className
     * @param configElements
     * @param typeName
     */
    public ServerControlFilter( String className, List<Element> configElements )
    {
        super( className, configElements, "serverControl" );
    }

    @Override
    public void messageReceived( NextFilter nextFilter, IoSession session, Object message )
            throws Exception
    {
        if ( provider != null && isRunning() ) {
            if ( message instanceof RtspResponse )
                provider.processResponse( session, (RtspResponse) message );
            else
                log.error( "Expecting a RtspResponse. Received a {}", message.getClass()
                        .getName() );
        }

        // Forward message
        nextFilter.messageReceived( session, message );
    }

    @Override
    public void messageSent( NextFilter nextFilter, IoSession session, Object message )
            throws Exception
    {
        if ( provider != null && isRunning() ) {
            if ( message instanceof RtspRequest )
                provider.processRequest( session, (RtspRequest) message );
            else
                log.error( "Expecting a Rtsprequest. Received a {}",
                        message.getClass().getName() );
        }

        // Forward message
        nextFilter.messageSent( session, message );
    }

}
