package rtspproxy.filter.accounting;

import java.io.File;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.mina.common.IoSession;
import org.dom4j.Element;

import rtspproxy.config.AAAConfigurable;
import rtspproxy.config.Config;
import rtspproxy.filter.authentication.AuthenticationFilter;
import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;

/**
 * @author Matteo Merli
 */
public class PlainTextAccountingProvider extends AccountingProviderAdapter 
implements AccountingProvider,  AAAConfigurable
{

	private static String datePattern = "yyyy-MM-dd HH:mm:ss Z";
	private static SimpleDateFormat format = new SimpleDateFormat( datePattern );

	// This is not static since it's a separate log
	private Logger accessLog;

	public PlainTextAccountingProvider()
	{
		accessLog = Logger.getLogger( "accessLog" );
	}

	public void init() throws Exception
	{
		// Do nothing
	}

	public void shutdown() throws Exception
	{
		// Do nothing
	}

	public void messageReceived( IoSession session, RtspMessage message )
	{
		StringBuilder logMessage = new StringBuilder();
		if ( message instanceof RtspRequest ) {
			logMessage.append( ( (RtspRequest) message ).getVerb() ).append( " " );
			logMessage.append( ( (RtspRequest) message ).getUrl() );
		}
		accessLog.info( buildLogMessage( session, message, logMessage ) );
	}

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

		sb.append( ( (InetSocketAddress) session.getRemoteAddress() ).getAddress().getHostAddress() );
		sb.append( " - " );
		sb.append( userName != null ? userName : "-" ).append( " " );
		sb.append( "[" ).append( dateString ).append( "] " );
		sb.append( "\"" ).append( logMessage ).append( "\" " );
		if ( userAgent != null ) {
			sb.append( "\"" ).append( userAgent ).append( "\" " );
		}

		return sb.toString();
	}

	public void configure(List<Element> configElements) throws Exception {
		for(Element el : configElements) {
			if(el.getName().equals("category")) {
				String category = el.getTextTrim();
				
				if(category == null || category.length() == 0) 					
					throw new IllegalArgumentException("invalid log category given");
				
				accessLog = Logger.getLogger(category);
			}
		}
	}
}
