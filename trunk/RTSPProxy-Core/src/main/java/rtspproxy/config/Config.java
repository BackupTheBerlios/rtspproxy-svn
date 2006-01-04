package rtspproxy.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.PropertyConfigurator;

import rtspproxy.lib.Singleton;
import rtspproxy.rtsp.Handler;

public class Config extends Singleton implements Observer
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
					+ "output of the program.",
			"/rtspproxy/log/debug" // xpathExpr
	);

	public static final BooleanParameter logLogToFile = new BooleanParameter(
			"log.logtofile", // name
			false, // default value
			true, // mutable
			"If you want to save to a file the debug output	set this to Yes",
			"/rtspproxy/log/logToFile" // xpathExpr
	);

	public static final StringParameter logFile = new StringParameter( "log.file", // name
			"logs/rtspproxy.log", // default value
			true, // mutable
			"Here you specify the file to log to.",
			"/rtspproxy/log/file"  // xpathExpr
	);

	public static final IntegerParameter threadPoolSize = new IntegerParameter(
			"thread.pool.size", // name
			new Integer( 0 ), // min value
			new Integer( 2147483647 ), // max value
			new Integer( 10 ), // default value
			true, // mutable
			"Maximum size of the thread pool. The thread pool is shared "
					+ "between all services found in RtspProxy.",
			"/rtspproxy/threadPoolSize" // xpathExpr
	);

	public static final IntegerParameter proxyRtspPort = new IntegerParameter(
			"proxy.rtsp.port", // name
			new Integer( 0 ), // min value
			new Integer( 65536 ), // max value
			new Integer( Handler.DEFAULT_RTSP_PORT ), // default value
			true, // mutable
			"This is the port which the proxy will listen for "
					+ "RTSP connection. The default is 554, like normal RTSP servers.",
			"/rtspproxy/proxy/rtspPort" // xpathExpr
			);

	public static final StringParameter proxyClientInterface = new StringParameter(
			"proxy.client.interface", // name
			null, // default value
			false, // mutable
			"Specify a network interface. Default is to listen on all interfaces." ,
			"/rtspproxy/proxy/client/interface" // xpathExpr
			);

	public static final StringParameter proxyServerInterface = new StringParameter(
			"proxy.server.interface", // name
			null, // default value
			false, // mutable
			"Specify a network interface. Default is to listen on all interfaces.",
			"/rtspproxy/proxy/server/interface" // pathExpr
			);

	public static final IntegerParameter proxyServerRtpPort = new IntegerParameter(
			"proxy.server.rtp.port", // name
			new Integer( 0 ), // min value
			new Integer( 65536 ), // max value
			new Integer( 8000 ), // default value
			true, // mutable
			"Port to listen for RTP packets arriving from servers.",
			"/rtspproxy/proxy/server/rtpPort" // xpathExpr
			);

	public static final IntegerParameter proxyServerRtcpPort = new IntegerParameter(
			"proxy.server.rtcp.port", // name
			new Integer( 0 ), // min value
			new Integer( 65536 ), // max value
			new Integer( 8001 ), // default value
			true, // mutable
			"Port to listen for RTCP packets arriving from servers.",
			"/rtspproxy/proxy/server/rtcpPort" // xpathExpr
			);

	public static final IntegerParameter proxyClientRtpPort = new IntegerParameter(
			"proxy.client.rtp.port", // name
			new Integer( 0 ), // min value
			new Integer( 65536 ), // max value
			new Integer( 8002 ), // default value
			true, // mutable
			"Port to listen for RTP packets arriving from clients.",
			"/rtspproxy/proxy/client/rtpPort" // xpathExpr
			);

	public static final IntegerParameter proxyClientRtcpPort = new IntegerParameter(
			"proxy.client.rtcp.port", // name
			new Integer( 0 ), // min value
			new Integer( 65536 ), // max value
			new Integer( 8003 ), // default value
			true, // mutable
			"Port to listen for RTCP packets arriving from clients.",
			"/rtspproxy/proxy/client/rtcpPort" // xpathExpr
			);

	public static final IntegerParameter proxyServerRdtPort = new IntegerParameter(
			"proxy.server.rdt.port", // name
			new Integer( 0 ), // min value
			new Integer( 65536 ), // max value
			new Integer( 8020 ), // default value
			true, // mutable
			"Port to listen for RDT packets arriving from servers.",
			"/rtspproxy/proxy/server/rdtPort" // xpathExpr
			);

	public static final IntegerParameter proxyClientRdtPort = new IntegerParameter(
			"proxy.client.rdt.port", // name
			new Integer( 0 ), // min value
			new Integer( 65536 ), // max value
			new Integer( 8022 ), // default value
			true, // mutable
			"Port to listen for RDT packets arriving from clients.",
			"/rtspproxy/proxy/server/rdtPort" // xpathExpr
			);

	// // IP address filter

	public static final BooleanParameter proxyFilterIpaddressEnable = new BooleanParameter(
			"proxy.filter.ipaddress.enable", // name
			false, // default value
			true, // mutable
			"Enable or disable the IP address filtering system.",
			null // xpathExpr
			);

	public static final StringParameter proxyFilterIpaddressImplementationClass = new StringParameter(
			"proxy.filter.ipaddress.implementationClass", // name
			"rtspproxy.filter.ipaddress.PlainTextIpAddressProvider", // default
			// value
			false, // mutable
			"Use an alternative backend class. This can be any class "
					+ "that implements the rtspproxy.filter.ipaddress.IpAddressProvider "
					+ "interface.",
			null // xpathExpr
			);

	public static final StringParameter proxyFilterIpaddressTextFile = new StringParameter(
			"proxy.filter.ipaddress.text.file", // name
			"conf/ipfilter.txt", // default value
			false, // mutable
			"Plain Text based implementation specific configuration",
			null // xpathExpr
			);

	// // Authentication filter

	public static final BooleanParameter proxyFilterAuthenticationEnable = new BooleanParameter(
			"proxy.filter.authentication.enable", // name
			false, // default value
			true, // mutable
			"Enable or disable the authentication system.",
			null // xpathExpr
			 );

	public static final StringParameter proxyFilterAuthenticationScheme = new StringParameter(
			"proxy.filter.authentication.scheme", // name
			"Basic", // default value
			false, // mutable
			"Authentication Scheme. This could be Basic (the default), Digest or any "
					+ "other supported scheme.",
					null // xpathExpr
					 );

	public static final StringParameter proxyFilterAuthenticationImplementationClass = new StringParameter(
			"proxy.filter.authentication.implementationClass", // name
			"rtspproxy.filter.authentication.PlainTextAuthenticationProvider", // default
			// value
			false, // mutable
			"Use an alternative backend class. This can be any class "
					+ "that implements the rtspproxy.filter.authentication.AuthenticationProvider "
					+ "interface.",
					null // xpathExpr
					 );

	public static final StringParameter proxyFilterAuthenticationTextFile = new StringParameter(
			"proxy.filter.authentication.text.file", // name
			"conf/users.txt", // default value
			false, // mutable
			"Plain Text based implementation specific configuration",
			null // xpathExpr
			 );

	// /////////////////////////////////////////////////////////

	// Accounting filter

	public static final BooleanParameter proxyFilterAccountingEnable = new BooleanParameter(
			"proxy.filter.accounting.enable", // name
			true, // default value
			true, // mutable
			"Controls the activation of the Accounting subsystem.",
			null // xpathExpr
			 );

	public static final StringParameter proxyFilterAccountingImplementationClass = new StringParameter(
			"proxy.filter.accounting.implementationClass", // name
			"rtspproxy.filter.accounting.PlainTextAccountingProvider", // default
			// value
			false, // mutable
			"Use an alternative backend class. This can be any class "
					+ "that implements the rtspproxy.filter.accounting.AccountingProvider "
					+ "interface.",
					null // xpathExpr
					 );

	public static final StringParameter proxyFilterAccountingTextFile = new StringParameter(
			"proxy.filter.accounting.text.file", // name
			"logs/access.log", // default value
			false, // mutable
			"Plain Text based implementation specific configuration",
			null // xpathExpr
			 );

	// /////////////////////////////////////////////////////////

	// JMX

	public static final BooleanParameter proxyManagementEnable = new BooleanParameter(
			"proxy.management.enable", // name
			false, // default value
			false, // mutable
			"Controls the activation of the management subsystem (JMX).",
			"/rtspproxy/jmx/manageable" // xpathExpr
			);

	public static final StringParameter proxyManagementHost = new StringParameter(
			"proxy.management.host", // name
			"localhost", // default value
			false, // mutable
			"Host to bind the management services. Default is localhost, and the services "
					+ "will only be reachable from local machine.",
			"/rtspproxy/jmx/interface" // xpathExpr
			 );

	public static final StringParameter proxyManagementUser = new StringParameter(
			"proxy.management.user", // name
			null, // default value
			true, // mutable
			"Remote management administrator user name.",
			"/rtspproxy/jmx/user" // xpathExpr
			 );
	
	public static final StringParameter proxyManagementPassword = new StringParameter(
			"proxy.management.password", // name
			null, // default value
			true, // mutable
			"Remote management administrator password.",
			"/rtspproxy/jmx/password" // xpathExpr
			 );

	public static final BooleanParameter proxyManagementWebEnable = new BooleanParameter(
			"proxy.management.web.enable", // name
			false, // default value
			false, // mutable
			"Controls the activation of the Web management console.",
			"/rtspproxy/jmx/web/manageable" // xpathExpr
			 );

	public static final IntegerParameter proxyManagementWebPort = new IntegerParameter(
			"proxy.management.web.port", // name
			new Integer( 0 ), // min value
			new Integer( 65536 ), // max value
			new Integer( 8000 ), // default value
			false, // mutable
			"TCP port to be used for the Web Console.",
			"/rtspproxy/jmx/web/port" // xpathExpr
			 );

	public static final BooleanParameter proxyManagementRemoteEnable = new BooleanParameter(
			"proxy.management.remote.enable", // name
			false, // default value
			false, // mutable
			"Controls the activation of the JMX connector server.",
			"/rtspproxy/jmx/connectorService/manageable" // xpathExpr
			 );

	// /////////////////////////////////////////////////////////

	private static String rtspproxyHome;

	private static String name;

	private static String version;

	private static String proxySignature;

	private static Date startDate;

	// /////////////////////////////////////////////////////////
	
	// filter configurations from XML
	private static List<AAAConfig> authenticationFilters = new ArrayList<AAAConfig>();

	// filter configurations from XML
	private static List<AAAConfig> authorizationFilters = new ArrayList<AAAConfig>();

	// filter configurations from XML
	private static List<AAAConfig> accountingFilters = new ArrayList<AAAConfig>();

	static void addAuthenticationFilter(AAAConfig config) {
		authenticationFilters.add(config);
	}
	
	static void addAuthorizationFilter(AAAConfig config) {
		authorizationFilters.add(config);
	}
	
	static void addAccountingFilter(AAAConfig config) {
		accountingFilters.add(config);
	}
	
	public static List<AAAConfig> getAuthenticationFilters() {
		return Collections.unmodifiableList(authenticationFilters);
	}
	
	public static List<AAAConfig> getAuthorizationFilters() {
		return Collections.unmodifiableList(authorizationFilters);
	}
	
	public static List<AAAConfig> getAccountingFilters() {
		return Collections.unmodifiableList(accountingFilters);
	}
	
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
			jarProps.load( Thread.currentThread().getContextClassLoader().getResourceAsStream(
					"META-INF/application.properties" ) );
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

		startDate = new Date();

		// Subscribe to parameter changes notification
		logDebug.addObserver( this );
		logFile.addObserver( this );
		logLogToFile.addObserver( this );
	}

	/**
	 * Manage parameters value changes
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update( Observable o, Object arg )
	{
		if ( !( o instanceof Parameter ) )
			throw new IllegalArgumentException( "Only observe parameters" );

		if ( o == logDebug || o == logFile || o == logLogToFile ) {
			updateDebugSettings();
			// log.debug( "Updated logger settings." );
		}
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

	public static Date getStartDate()
	{
		return startDate;
	}

	// /////////////////////////////////////////////////////////

	protected static void updateDebugSettings()
	{
		Properties logProperties = new Properties();
		// common properties
		logProperties.setProperty( "log4j.appender.A1.layout",
				"org.apache.log4j.PatternLayout" );

		if ( logDebug.getValue() ) {
			/*
			 * For debug messages we want to have a special layout
			 */
			logProperties.setProperty( "log4j.appender.A1.layout.ConversionPattern",
					"%9r %5p [%t] %c - %m%n" );
			logProperties.setProperty( "log4j.rootLogger", "DEBUG, A1" );

		} else {
			// only write important messages
			logProperties.setProperty( "log4j.appender.A1.layout.ConversionPattern",
					"%5p - %d - %m%n" );
			logProperties.setProperty( "log4j.rootLogger", "INFO, A1" );
		}

		if ( logLogToFile.getValue() == true ) {
			// save logs in a file
			File file = new File( logFile.getValue() );
			if ( !file.isAbsolute() ) {
				file = new File( rtspproxyHome + File.separator + logFile.getValue() );
			}

			logProperties.setProperty( "log4j.appender.A1",
					"org.apache.log4j.RollingFileAppender" );
			logProperties.setProperty( "log4j.appender.A1.File", file.getAbsolutePath() );

			// if logs directory does not exists, create it
			File logs = file.getParentFile();
			if ( !logs.exists() )
				logs.mkdir();

		} else {
			// Log to console
			logProperties.setProperty( "log4j.appender.A1",
					"org.apache.log4j.ConsoleAppender" );
		}

		PropertyConfigurator.configure( logProperties );
	}

	/**
	 * @return a String containing all the parameters
	 */
	public static String debugParameters()
	{
		StringBuilder sb = new StringBuilder();
		Map<String, Parameter> parameters = new TreeMap<String, Parameter>(
				Config.getParametersMap() );
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
