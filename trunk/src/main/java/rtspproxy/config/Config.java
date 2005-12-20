package rtspproxy.config;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.PropertyConfigurator;

import rtspproxy.lib.Singleton;
import rtspproxy.rtsp.Handler;

public class Config extends Singleton
{
	private static ConcurrentMap<String, Parameter> parameters = new ConcurrentHashMap<String, Parameter>();

	protected static void addParameter( Parameter parameter )
	{
		parameters.put( parameter.getName(), parameter );
	}

	public static Parameter getParameter( String name )
	{
		if ( name == null )
			throw new IllegalArgumentException( "name is null" );

		return parameters.get( name );
	}

	public static Collection<Parameter> getAllParameters()
	{
		return parameters.values();
	}

	protected static Map<String, Parameter> getParametersMap()
	{
		return parameters;
	}

	public static final BooleanParameter logDebug = new BooleanParameter( "log.debug", // name
			false, // default value
			true, // mutable
			"This flag let you to enable or disable the debug "
					+ "output of the program." );

	public static final BooleanParameter logLogToFile = new BooleanParameter(
			"log.logtofile", // name
			false, // default value
			false, // mutable
			"If you want to save to a file the debug output	set this to Yes" );

	public static final StringParameter logFile = new StringParameter( "log.file", // name
			"logs/rtspproxy.log", // default value
			false, // mutable
			"Here you specify the file to log to." );

	public static final IntegerListParameter proxyRtspPort = new IntegerListParameter(
			"proxy.rtsp.port", // name
			new Integer( 0 ), // min value
			new Integer( 65536 ), // max value
			new Integer( Handler.DEFAULT_RTSP_PORT ), // default value
			true, // mutable
			"This is the port which the proxy will listen for "
					+ "RTSP connection. The default is 554, like normal RTSP servers." );

	public static final StringParameter proxyClientInterface = new StringParameter(
			"proxy.client.interface", // name
			null, // default value
			true, // mutable
			"Specify a network interface. Default is to listen on all interfaces." );

	public static final StringParameter proxyServerInterface = new StringParameter(
			"proxy.server.interface", // name
			null, // default value
			true, // mutable
			"Specify a network interface. Default is to listen on all interfaces." );

	public static final IntegerParameter proxyServerRtpPort = new IntegerParameter(
			"proxy.server.rtp.port", // name
			new Integer( 0 ), // min value
			new Integer( 65536 ), // max value
			new Integer( 8000 ), // default value
			true, // mutable
			"Port to listen for RTP packets arriving from servers." );

	public static final IntegerParameter proxyServerRtcpPort = new IntegerParameter(
			"proxy.server.rtcp.port", // name
			new Integer( 0 ), // min value
			new Integer( 65536 ), // max value
			new Integer( 8001 ), // default value
			true, // mutable
			"Port to listen for RTCP packets arriving from servers." );

	public static final IntegerParameter proxyClientRtpPort = new IntegerParameter(
			"proxy.client.rtp.port", // name
			new Integer( 0 ), // min value
			new Integer( 65536 ), // max value
			new Integer( 8002 ), // default value
			true, // mutable
			"Port to listen for RTP packets arriving from clients." );

	public static final IntegerParameter proxyClientRtcpPort = new IntegerParameter(
			"proxy.client.rtcp.port", // name
			new Integer( 0 ), // min value
			new Integer( 65536 ), // max value
			new Integer( 8003 ), // default value
			true, // mutable
			"Port to listen for RTCP packets arriving from clients." );

	public static final IntegerParameter proxyServerRdtPort = new IntegerParameter(
			"proxy.server.rdt.port", // name
			new Integer( 0 ), // min value
			new Integer( 65536 ), // max value
			new Integer( 8020 ), // default value
			true, // mutable
			"Port to listen for RDT packets arriving from servers." );

	public static final IntegerParameter proxyClientRdtPort = new IntegerParameter(
			"proxy.client.rdt.port", // name
			new Integer( 0 ), // min value
			new Integer( 65536 ), // max value
			new Integer( 8022 ), // default value
			true, // mutable
			"Port to listen for RDT packets arriving from clients." );

	// // IP address filter

	public static final BooleanParameter proxyFilterIpaddressEnable = new BooleanParameter(
			"proxy.filter.ipaddress.enable", // name
			false, // default value
			true, // mutable
			"Enable or disable the IP address filtering system." );

	public static final StringParameter proxyFilterIpaddressImplementationClass = new StringParameter(
			"proxy.filter.ipaddress.implementationClass", // name
			"rtspproxy.filter.ipaddress.PlainTextIpAddressProvider", // default
			// value
			false, // mutable
			"Use an alternative backend class. This can be any class "
					+ "that implements the rtspproxy.filter.ipaddress.IpAddressProvider "
					+ "interface." );

	public static final StringParameter proxyFilterIpaddressTextFile = new StringParameter(
			"proxy.filter.ipaddress.text.file", // name
			"conf/ipfilter.txt", // default value
			false, // mutable
			"Plain Text based implementation specific configuration" );

	// // Authentication filter

	public static final BooleanParameter proxyFilterAuthenticationEnable = new BooleanParameter(
			"proxy.filter.authentication.enable", // name
			false, // default value
			true, // mutable
			"Enable or disable the authentication system." );

	public static final StringParameter proxyFilterAuthenticationScheme = new StringParameter(
			"proxy.filter.authentication.scheme", // name
			"Basic", // default value
			false, // mutable
			"Authentication Scheme. This could be Basic (the default), Digest or any "
					+ "other supported scheme." );

	public static final StringParameter proxyFilterAuthenticationImplementationClass = new StringParameter(
			"proxy.filter.authentication.implementationClass", // name
			"rtspproxy.filter.authentication.PlainTextAuthenticationProvider", // default
			// value
			false, // mutable
			"Use an alternative backend class. This can be any class "
					+ "that implements the rtspproxy.filter.authentication.AuthenticationProvider "
					+ "interface." );

	public static final StringParameter proxyFilterAuthenticationTextFile = new StringParameter(
			"proxy.filter.authentication.text.file", // name
			"conf/users.txt", // default value
			false, // mutable
			"Plain Text based implementation specific configuration" );

	// /////////////////////////////////////////////////////////

	private static String rtspproxyHome;

	private static String name;

	private static String version;

	private static String proxySignature;

	// /////////////////////////////////////////////////////////

	public Config()
	{
		// Read home directory
		rtspproxyHome = System.getProperty( "rtspproxy.home" );
		if ( rtspproxyHome == null ) {
			rtspproxyHome = System.getProperty( "user.dir" );
			if ( rtspproxyHome == null )
				rtspproxyHome = "";
		}

		// Read program name and version
		Properties jarProps = new Properties();
		try {
			jarProps.load( Thread.currentThread().getContextClassLoader()
					.getResourceAsStream( "META-INF/application.properties" ) );
			name = jarProps.getProperty( "application.name" );
			version = jarProps.getProperty( "application.version" );
		} catch ( Exception e ) {
			name = "RtspProxy";
			version = "";
		}

		// Build proxy signature
		StringBuilder sb = new StringBuilder();
		sb.append( name ).append( " " ).append( version );
		sb.append( " (" ).append( System.getProperty( "os.name" ) );
		sb.append( " / " ).append( System.getProperty( "os.version" ) );
		sb.append( " / " ).append( System.getProperty( "os.arch" ) );
		sb.append( ")" );
		proxySignature = sb.toString();
	}

	/**
	 * @return the application base dir
	 */
	public static String getHome()
	{
		return rtspproxyHome;
	}

	/**
	 * @return Returns the application name.
	 */
	public static String getName()
	{
		return name;
	}

	/**
	 * @return Returns the application version.
	 */
	public static String getVersion()
	{
		return version;
	}

	/**
	 * @return Returns the proxySignature.
	 */
	public static String getProxySignature()
	{
		return proxySignature;
	}

	// /////////////////////////////////////////////////////////

	protected static void updateDebugSettings()
	{
		Properties prop = new Properties();
		// common properties
		prop.setProperty( "log4j.appender.A1.layout", "org.apache.log4j.PatternLayout" );
		prop.setProperty( "log4j.appender.A1.layout.ConversionPattern",
				"%7p [%t] (%F:%L) - %m%n" );

		if ( logDebug.getValue() )
			prop.setProperty( "log4j.rootLogger", "DEBUG, A1" );
		else
			// only write important messages
			prop.setProperty( "log4j.rootLogger", "INFO, A1" );

		if ( logLogToFile.getValue() ) {
			// save logs in a file
			String filename = logFile.getValue();
			prop
					.setProperty( "log4j.appender.A1",
							"org.apache.log4j.RollingFileAppender" );
			prop.setProperty( "log4j.appender.A1.File", filename );

			// if logs directory does not exists, create it
			File logs = new File( rtspproxyHome + File.separator + "logs" );
			if ( !logs.exists() )
				logs.mkdir();

		} else {
			// Log to console
			prop.setProperty( "log4j.appender.A1", "org.apache.log4j.ConsoleAppender" );
		}

		PropertyConfigurator.configure( prop );
	}

	/**
	 * @return a String containing all the parameters
	 */
	public static String debugParameters()
	{
		StringBuilder sb = new StringBuilder();
		Map<String, Parameter> parameters = new TreeMap<String, Parameter>( Config
				.getParametersMap() );
		sb.append( "Parameters:\n" );
		for ( Parameter parameter : parameters.values() ) {
			sb.append( parameter.getName() );
			sb.append( ": " );
			sb.append( parameter.getStringValue() );
			sb.append( "\n" );
		}

		return sb.toString();
	}

}
