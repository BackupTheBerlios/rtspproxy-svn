package rtspproxy.filter.accounting;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.configuration.Configuration;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.config.StringParameter;
import rtspproxy.filter.authentication.AuthenticationFilter;
import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;

/**
 * @author Matteo Merli
 */
public class SimpleAccountingProvider extends AccountingProviderAdapter implements
        AccountingProvider, Observer
{

    private static final SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss Z" );

    private static Logger log = LoggerFactory.getLogger( SimpleAccountingProvider.class );

    private static final String requestMessageATTR = SimpleAccountingProvider.class.getName() + "requestATTR";

    // This is not static since it's a separate log
    private Logger accessLog = null;

    private final StringParameter loggerCatergory;

    public SimpleAccountingProvider()
    {
        loggerCatergory = new StringParameter( "filters.accounting.category", // name
                "accounting.rtspproxy", // default value
                true, // mutable
                "Log4j category name for the accounting log." );
        
        loggerCatergory.addObserver( this );
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.GenericProvider#start()
     */
    public void start() throws Exception
    {
        accessLog = LoggerFactory.getLogger( loggerCatergory.getValue() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.GenericProvider#stop()
     */
    public void stop()
    {
        accessLog = null;
    }

    @Override
    public void messageReceived( IoSession session, RtspMessage message )
    {
        if ( accessLog == null )
            return;

        StringBuilder logMessage = new StringBuilder();
        if ( message instanceof RtspRequest ) {
            logMessage.append( ((RtspRequest) message).getVerb() ).append( ' ' );
            logMessage.append( ((RtspRequest) message).getUrl() );
        }
        accessLog.info( buildLogMessage( session, message, logMessage ) );
    }

    @Override
    public void messageSent( IoSession session, RtspMessage message )
    {
        if ( accessLog == null )
            return;

        StringBuilder logMessage = new StringBuilder();
        accessLog.info( buildLogMessage( session, message, logMessage ) );
    }

    private static String buildLogMessage( IoSession session, RtspMessage message,
            StringBuilder logMessage )
    {
        StringBuilder sb = new StringBuilder( 150 );
        String userName = (String) session.getAttribute( AuthenticationFilter.getAttrName() );
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

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.GenericProvider#configure(org.apache.commons.configuration.Configuration)
     */
    public void configure( Configuration configuration ) throws Exception
    {
        loggerCatergory.readConfiguration( configuration );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update( Observable o, Object arg )
    {
        if ( o != loggerCatergory )
            return;
        
        try {
            stop();
            start();
        } catch ( Exception e ) {
            log.error( "Error restarting SimpleAccountingProvider" );
        }
    }
}
