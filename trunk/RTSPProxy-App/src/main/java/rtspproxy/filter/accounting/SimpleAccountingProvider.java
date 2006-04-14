package rtspproxy.filter.accounting;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;
import org.dom4j.Element;

import rtspproxy.config.AAAConfigurable;
import rtspproxy.filter.authentication.AuthenticationFilter;
import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;

/**
 * @author Matteo Merli
 */
public class SimpleAccountingProvider extends AccountingProviderAdapter implements
        AccountingProvider, AAAConfigurable
{

    private static SimpleDateFormat format = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss Z" );

    // This is not static since it's a separate log
    private Logger accessLog;

    public SimpleAccountingProvider()
    {
        accessLog = Logger.getLogger( "accessLog" );
    }

    @Override
    public void messageReceived( IoSession session, RtspMessage message )
    {
        StringBuilder logMessage = new StringBuilder();
        if ( message instanceof RtspRequest ) {
            logMessage.append( ((RtspRequest) message).getVerb() ).append( " " );
            logMessage.append( ((RtspRequest) message).getUrl() );
        }
        accessLog.info( buildLogMessage( session, message, logMessage ) );
    }

    @Override
    public void messageSent( IoSession session, RtspMessage message )
    {
        StringBuilder logMessage = new StringBuilder();
        accessLog.info( buildLogMessage( session, message, logMessage ) );
    }

    private static String buildLogMessage( IoSession session, RtspMessage message,
            StringBuilder logMessage )
    {
        StringBuilder sb = new StringBuilder( 150 );
        String userName = (String) session.getAttribute( AuthenticationFilter.ATTR );
        String userAgent = message.getHeader( "User-Agent" );
        Date now = new Date();
        String dateString = format.format( now );

        sb.append( ((InetSocketAddress) session.getRemoteAddress()).getAddress()
                .getHostAddress() );
        sb.append( " - " );
        sb.append( userName != null ? userName : '-' ).append( ' ' );
        sb.append( '[' ).append( dateString ).append( "] " );
        sb.append( '"' ).append( logMessage ).append( "\" " );
        if ( userAgent != null ) {
            sb.append( '"' ).append( userAgent ).append( "\" " );
        }

        return sb.toString();
    }

    public void configure( List<Element> configElements ) throws Exception
    {
        for ( Element el : configElements ) {
            if ( el.getName().equals( "category" ) ) {
                String category = el.getTextTrim();

                if ( category == null || category.length() == 0 )
                    throw new IllegalArgumentException( "invalid log category given" );

                accessLog = Logger.getLogger( category );
            }
        }
    }
}
