package rtspproxy.config;

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

	public static final StringParameter proxyClientAddress = new StringParameter(
			"proxy.client.address", // name
			null, // default value
			false, // mutable
			"Specify a network address." ,
			"/rtspproxy/proxy/client/address" // xpathExpr
			);

	public static final StringParameter proxyServerAddress = new StringParameter(
			"proxy.server.address", // name
			null, // default value
			false, // mutable
			"Specify a network address.",
			"/rtspproxy/proxy/server/address" // pathExpr
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

	public static final BooleanParameter proxyServerRtpMultiplePorts = new BooleanParameter(
			"proxy.server.rtp.multiport.enable", // name
			false, // default value
			false, // mutable
			"Enables the RTP/RTCP multiport handling.",
			"/rtspproxy/proxy/server/rtpUsePortrange" // xpathExpr
			);

	public static final IntegerParameter proxyServerRtpMinPort = new IntegerParameter(
			"proxy.server.rtp.port.min", // name
			new Integer( 0 ), // min value
			new Integer( 65536 ), // max value
			new Integer( 9000 ), // default value
			true, // mutable
			"Port to listen for RTP packets arriving from servers.",
			"/rtspproxy/proxy/server/rtpPortrange/minPort" // xpathExpr
			);

	public static final IntegerParameter proxyServerRtpMaxPort = new IntegerParameter(
			"proxy.server.rtp.port.max", // name
			new Integer( 0 ), // min value
			new Integer( 65536 ), // max value
			new Integer( 9100 ), // default value
			true, // mutable
			"Port to listen for RTP packets arriving from servers.",
			"/rtspproxy/proxy/server/rtpPortrange/maxPort" // xpathExpr
			);
	
	public static final IntegerParameter proxyServerRtpIdleTimeout = new IntegerParameter(
			"proxy.server.rtp.portrange.idle.timeout", // name
			new Integer( 0 ), // min value
			new Integer( 86400 ), // max value
			new Integer( 3600 ), // default value
			true, // mutable
			"Timeout an open RTP server port may linger around.",
			"/rtspproxy/proxy/server/rtpPortrange/idleTimeout" // xpathExpr
			);	

	public static final IntegerParameter proxyServerRtpIdleScanInterval = new IntegerParameter(
			"proxy.server.rtp.portrange.idle.timeout", // name
			new Integer( 0 ), // min value
			new Integer( 86400 ), // max value
			new Integer( 1800 ), // default value
			true, // mutable
			"Scan interval on idle RTP server ports.",
			"/rtspproxy/proxy/server/rtpPortrange/idleScanInterval" // xpathExpr
			);

	public static final IntegerParameter proxyServerRtpThreadPoolSize = new IntegerParameter(
			"proxy.server.rtp.portrange.pool.size", // name
			new Integer( 0 ), // min value
			new Integer( 2147483647 ), // max value
			new Integer( 10 ), // default value
			true, // mutable
			"Scan interval on idle RTP server ports.",
			"/rtspproxy/proxy/server/rtpPortrange/threadPoolSize" // xpathExpr
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
			"/rtspproxy/proxy/client/rdtPort" // xpathExpr
			);

	public static final BooleanParameter proxyTransportRtpEnable = new BooleanParameter(
			"proxy.transport.rtp.enable", // name
			true, // default value
			false, // mutable
			"Enables the UDP/AVP/RTP transport.",
			"/rtspproxy/proxy/transport/rtp" // xpathExpr
			);
	

	public static final BooleanParameter proxyTransportRdtEnable = new BooleanParameter(
			"proxy.transport.rdt.enable", // name
			true, // default value
			false, // mutable
			"Enables the x-udp-rdt transport.",
			"/rtspproxy/proxy/transport/rdt" // xpathExpr
			);	
	

	public static final BooleanParameter proxyLowerTransportSuppress = new BooleanParameter(
			"proxy.transport.rtp.protocol.disable", // name
			false, // default value
			false, // mutable
			"Enables the x-udp-rdt transport.",
			"/rtspproxy/proxy/transport/hacks/lowerTransportSuppress" // xpathExpr
			);	

	public static final BooleanParameter proxyRtspTransportSsrcDisable = new BooleanParameter(
			"proxy.transport.rtsp.transport.ssrc.disable", // name
			false, // default value
			false, // mutable
			"disable the output of the SSRC transport attribute",
			"/rtspproxy/proxy/transport/hacks/rtspTransportSsrcDisable" // xpathExpr
			);	

	public static final BooleanParameter proxyRtspTransportSourceDisable = new BooleanParameter(
			"proxy.transport.rtsp.transport.source.disable", // name
			false, // default value
			false, // mutable
			"disable the output of the SOURCE transport attribute",
			"/rtspproxy/proxy/transport/hacks/rtspTransportSourceDisable" // xpathExpr
			);	

	public static final BooleanParameter proxyRtspKeepAlive = new BooleanParameter(
			"proxy.transport.rtsp.keepAlive.enable", // name
			false, // default value
			false, // mutable
			"Enable keep-alive on RTSP connections to remote servers.",
			"/rtspproxy/proxy/transport/hacks/rtspKeepAlive" // xpathExpr
			);
	
	public static final BooleanParameter proxyRtspAllowBrokenHeaders = new BooleanParameter(
			"proxy.transport.rtsp.broken.headers.enable", // name
			false, // default value
			false, // mutable
			"Allow certain work-arounds for clients generating non-conformant RTSP protocol traffic.",
			"/rtspproxy/proxy/transport/hacks/rtspAllowBrokenHeaders" // xpathExpr
			);

	public static final BooleanParameter proxyRtspOfferSsrcToServer = new BooleanParameter(
			"proxy.transport.rtsp.offer.ssrc.enable", // name
			false, // default value
			false, // mutable
			"Allow certain work-arounds for clients generating non-conformant RTSP protocol traffic.",
			"/rtspproxy/proxy/transport/hacks/offerRemoteSsrc" // xpathExpr
			);

	public static final BooleanParameter proxyServerRtpSsrcUnreliable = new BooleanParameter(
			"proxy.streaming.rtp.ssrc.unreliable", // name
			false, // default value
			false, // mutable
			"Disable the evaluation of the SSRC send by the remote streaming server.",
			"/rtspproxy/proxy/streaming/hacks/rtpSsrcUnreliable" // xpathExpr
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
			"/rtspproxy/jmx/address" // xpathExpr
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
	private static List<AAAConfig> ipAddressFilters = new ArrayList<AAAConfig>();

	// filter configurations from XML
	private static List<AAAConfig> accountingFilters = new ArrayList<AAAConfig>();

	// filter configurations from XML
	private static List<AAAConfig> urlRewritingFilters = new ArrayList<AAAConfig>();

	private static List<AAAConfig> controlFilters = new ArrayList<AAAConfig>();
	
	static void addAuthenticationFilter(AAAConfig config) {
		authenticationFilters.add(config);
	}
	
	static void addIpAddressFilter(AAAConfig config) {
		ipAddressFilters.add(config);
	}
	
	static void addAccountingFilter(AAAConfig config) {
		accountingFilters.add(config);
	}
	
	static void addUrlRewritingFilter(AAAConfig config) {
		urlRewritingFilters.add(config);
	}

	public static void addControlFilter(AAAConfig aaa) {
		controlFilters.add(aaa);
	}

	public static List<AAAConfig> getAuthenticationFilters() {
		return Collections.unmodifiableList(authenticationFilters);
	}
	
	public static List<AAAConfig> getIpAddressFilters() {
		return Collections.unmodifiableList(ipAddressFilters);
	}
	
	public static List<AAAConfig> getAccountingFilters() {
		return Collections.unmodifiableList(accountingFilters);
	}

	public static List<AAAConfig> getUrlRewritingFilters() {
		return Collections.unmodifiableList(urlRewritingFilters);
	}
	
	public static List<AAAConfig> getControlFilters() {
		return Collections.unmodifiableList(controlFilters);
	}
 	
	// /////////////////////////////////////////////////////////

	public Config()
	{
		// Read home directory
		rtspproxyHome = System.getProperty( "rtspproxy.home" );
		if ( rtspproxyHome == null ) {
			rtspproxyHome = System.getProperty( "user.dir" );
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
	}

	/**
	 * @return the application base dir or null if the home directory cannot be determined.
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
