package rtspproxy.filter.accounting;

import java.io.File;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.mina.common.IoSession;

import rtspproxy.config.Config;
import rtspproxy.filter.authentication.AuthenticationFilter;
import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;

/**
 * @author Matteo Merli
 */
public class PlainTextAccountingProvider implements AccountingProvider, Observer
{

	private static String datePattern = "yyyy-MM-dd HH:mm:ss Z";
	private static SimpleDateFormat format = new SimpleDateFormat( datePattern );

	// This is not static since it's a separate log
	private Logger accessLog;

	public PlainTextAccountingProvider()
	{
		accessLog = Logger.getLogger( "accessLog" );

		// Subcribe to changes notification
		Config.proxyFilterAccountingTextFile.addObserver( this );
	}

	public void init() throws Exception
	{
		// Set the file appender
		String fileName = Config.proxyFilterAccountingTextFile.getValue();
		File file = new File( fileName );
		if ( !file.isAbsolute() ) {
			file = new File( Config.getHome() + File.separator + fileName );
		}

		// if logs directory does not exists, create it
		File logs = file.getParentFile();
		if ( !logs.exists() )
			logs.mkdir();

		Layout layout = new PatternLayout( "%m%n" );
		Appender appender = new RollingFileAppender( layout, file.getAbsolutePath() );
		accessLog.setAdditivity( false );
		accessLog.addAppender( appender );
		accessLog.setLevel( Level.INFO );
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
		// StringBuilder sb = new StringBuilder();
		// sb.append( "ciao" );
		// accessLog.info( buildLogMessage( session, message, sb ) );
	}

	public void update( Observable o, Object arg )
	{
		if ( o == Config.proxyFilterAccountingTextFile ) {
			try {
				// Reload the configuration
				init();
			} catch ( Exception e ) {
			}
		}
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
}
