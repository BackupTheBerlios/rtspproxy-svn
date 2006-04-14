/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   Copyright (C) 2005 - Matteo Merli - matteo.merli@gmail.com            *
 *                                                                         *
 ***************************************************************************/

/*
 * $Id$
 * 
 * $URL$
 * 
 */

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

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import rtspproxy.lib.Singleton;
import rtspproxy.rtsp.Handler;

/**
 * 
 * @author Matteo Merli
 */
public class Config extends Singleton implements Observer
{

    /** Map that contains all the application parameters. */
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

    public static final BooleanParameter debugEnabled = new BooleanParameter( "debug", // name
            false, // default value
            false, // mutable
            "Enables or disable application-wide debug messages." );

    public static final IntegerParameter threadPoolSize = new IntegerParameter(
            "threadPoolSize", // name
            new Integer( 0 ), // min value
            new Integer( 2147483647 ), // max value
            new Integer( 10 ), // default value
            true, // mutable
            "Maximum size of the thread pool. The thread pool is shared "
                    + "between all services found in RtspProxy." );

    public static final IntegerParameter proxyRtspPort = new IntegerParameter(
            "proxy.rtspPort", // name
            new Integer( 0 ), // min value
            new Integer( 65536 ), // max value
            new Integer( Handler.DEFAULT_RTSP_PORT ), // default value
            true, // mutable
            "This is the port which the proxy will listen for "
                    + "RTSP connection. The default is 554, like normal RTSP servers." );

    public static final StringParameter proxyClientInterface = new StringParameter(
            "proxy.client.interface", // name
            null, // default value
            false, // mutable
            "Specify a network interface. Default is to listen on all interfaces." );

    public static final StringParameter proxyServerInterface = new StringParameter(
            "proxy.server.interface", // name
            null, // default value
            false, // mutable
            "Specify a network interface. Default is to listen on all interfaces." );

    public static final StringParameter proxyClientAddress = new StringParameter(
            "proxy.client.address", // name
            null, // default value
            false, // mutable
            "Specify a network address. Default is to listen on all addresses" );

    public static final StringParameter proxyServerAddress = new StringParameter(
            "proxy.server.address", // name
            null, // default value
            false, // mutable
            "Specify a network address." );

    public static final IntegerParameter proxyServerRtpPort = new IntegerParameter(
            "proxy.server.rtpPort", // name
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

    public static final BooleanParameter proxyServerRtpMultiplePorts = new BooleanParameter(
            "proxy.server.rtpUsePortRange", // name
            false, // default value
            false, // mutable
            "Enables the RTP/RTCP multiport handling." );

    public static final IntegerParameter proxyServerRtpMinPort = new IntegerParameter(
            "proxy.server.rtpPortrange.minPort", // name
            new Integer( 0 ), // min value
            new Integer( 65536 ), // max value
            new Integer( 9000 ), // default value
            true, // mutable
            "Port to listen for RTP packets arriving from servers." );

    public static final IntegerParameter proxyServerRtpMaxPort = new IntegerParameter(
            "proxy.server.rtpPortrange.maxPort", // name
            new Integer( 0 ), // min value
            new Integer( 65536 ), // max value
            new Integer( 9100 ), // default value
            true, // mutable
            "Port to listen for RTP packets arriving from servers." );

    public static final IntegerParameter proxyServerRtpIdleTimeout = new IntegerParameter(
            "proxy.server.rtpPortrange.idleTimeout", // name
            new Integer( 0 ), // min value
            new Integer( 86400 ), // max value
            new Integer( 3600 ), // default value
            true, // mutable
            "Timeout an open RTP server port may linger around." );

    public static final IntegerParameter proxyServerRtpIdleScanInterval = new IntegerParameter(
            "proxy.server.rtpPortrange.idleScanInterval", // name
            new Integer( 0 ), // min value
            new Integer( 86400 ), // max value
            new Integer( 1800 ), // default value
            true, // mutable
            "Scan interval on idle RTP server ports." );

    public static final IntegerParameter proxyServerRtpThreadPoolSize = new IntegerParameter(
            "proxy.server.rtpPortrange.threadPoolSize", // name
            new Integer( 0 ), // min value
            new Integer( 2147483647 ), // max value
            new Integer( 10 ), // default value
            true, // mutable
            "Scan interval on idle RTP server ports." );

    public static final IntegerParameter proxyClientRtpPort = new IntegerParameter(
            "proxy.client.rtpPort", // name
            new Integer( 0 ), // min value
            new Integer( 65536 ), // max value
            new Integer( 8002 ), // default value
            true, // mutable
            "Port to listen for RTP packets arriving from clients." );

    public static final IntegerParameter proxyClientRtcpPort = new IntegerParameter(
            "proxy.client.rtcpPort", // name
            new Integer( 0 ), // min value
            new Integer( 65536 ), // max value
            new Integer( 8003 ), // default value
            true, // mutable
            "Port to listen for RTCP packets arriving from clients." );

    public static final IntegerParameter proxyServerRdtPort = new IntegerParameter(
            "proxy.server.rdtPort", // name
            new Integer( 0 ), // min value
            new Integer( 65536 ), // max value
            new Integer( 8020 ), // default value
            true, // mutable
            "Port to listen for RDT packets arriving from servers." );

    public static final IntegerParameter proxyClientRdtPort = new IntegerParameter(
            "proxy.client.rdtPort", // name
            new Integer( 0 ), // min value
            new Integer( 65536 ), // max value
            new Integer( 8022 ), // default value
            true, // mutable
            "Port to listen for RDT packets arriving from clients." );

    public static final BooleanParameter proxyTransportRtpEnable = new BooleanParameter(
            "proxy.transport.rtp", // name
            true, // default value
            false, // mutable
            "Enables the UDP/AVP/RTP transport." );

    public static final BooleanParameter proxyTransportRdtEnable = new BooleanParameter(
            "proxy.transport.rdt", // name
            true, // default value
            false, // mutable
            "Enables the x-udp-rdt transport." );

    public static final BooleanParameter proxyLowerTransportSuppress = new BooleanParameter(
            "proxy.transport.hacks.lowerTransportSuppress", // name
            false, // default value
            false, // mutable
            "Enables the x-udp-rdt transport." );

    public static final BooleanParameter proxyRtspTransportSsrcDisable = new BooleanParameter(
            "proxy.transport.hacks.rtspTransportSsrcDisable", // name
            false, // default value
            false, // mutable
            "Disable the output of the SSRC transport attribute" );

    public static final BooleanParameter proxyRtspTransportSourceDisable = new BooleanParameter(
            "proxy.transport.rtspTransportSourceDisable", // name
            false, // default value
            false, // mutable
            "Disable the output of the SOURCE transport attribute" );

    public static final BooleanParameter proxyRtspKeepAlive = new BooleanParameter(
            "proxy.transport.hacks.rtspKeepAlive", // name
            false, // default value
            false, // mutable
            "Enable keep-alive on RTSP connections to remote servers." );

    public static final BooleanParameter proxyRtspAllowBrokenHeaders = new BooleanParameter(
            "proxy.transport.hacks.rtspAllowBrokenHeaders", // name
            false, // default value
            false, // mutable
            "Allow certain work-arounds for clients generating non-conformant RTSP protocol traffic." );

    public static final BooleanParameter proxyRtspOfferSsrcToServer = new BooleanParameter(
            "proxy.transport.hacks.offerRemoteSsrc", // name
            false, // default value
            false, // mutable
            "Allow certain work-arounds for clients generating non-conformant RTSP protocol traffic." );

    public static final BooleanParameter proxyServerRtpSsrcUnreliable = new BooleanParameter(
            "proxy.transport.hacks.rtpSsrcUnreliable", // name
            false, // default value
            false, // mutable
            "Disable the evaluation of the SSRC send by the remote streaming server." );

    // /////////////////////////////////////////////////////////

    // JMX

    public static final BooleanParameter jmxEnable = new BooleanParameter( "jmx.enable", // name
            false, // default value
            false, // mutable
            "Controls the activation of the management subsystem (JMX)." );

    public static final StringParameter jmxAddress = new StringParameter( "jmx.address", // name
            "localhost", // default value
            false, // mutable
            "Host to bind the management services. Default is localhost, and the services "
                    + "will only be reachable from local machine." );

    public static final StringParameter jmxUser = new StringParameter( "jmx.user", // name
            "", // default value
            true, // mutable
            "Remote management administrator user name." );

    public static final StringParameter jmxPassword = new StringParameter(
            "jmx.password", // name
            "", // default value
            true, // mutable
            "Remote management administrator password." );

    public static final BooleanParameter jmxWebEnable = new BooleanParameter(
            "jmx.web.enable", // name
            false, // default value
            false, // mutable
            "Controls the activation of the Web management console." );

    public static final IntegerParameter jmxWebPort = new IntegerParameter(
            "jmx.web.port", // name
            new Integer( 0 ), // min value
            new Integer( 65536 ), // max value
            new Integer( 8000 ), // default value
            false, // mutable
            "TCP port to be used for the Web Console." );

    public static final BooleanParameter jmxConnectorServiceEnable = new BooleanParameter(
            "jmx.connectorService.enable", // name
            false, // default value
            false, // mutable
            "Controls the activation of the JMX connector server." );

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

    static void addAuthenticationFilter( AAAConfig config )
    {
        authenticationFilters.add( config );
    }

    static void addIpAddressFilter( AAAConfig config )
    {
        ipAddressFilters.add( config );
    }

    static void addAccountingFilter( AAAConfig config )
    {
        accountingFilters.add( config );
    }

    static void addUrlRewritingFilter( AAAConfig config )
    {
        urlRewritingFilters.add( config );
    }

    public static void addControlFilter( AAAConfig aaa )
    {
        controlFilters.add( aaa );
    }

    public static List<AAAConfig> getAuthenticationFilters()
    {
        return Collections.unmodifiableList( authenticationFilters );
    }

    public static List<AAAConfig> getIpAddressFilters()
    {
        return Collections.unmodifiableList( ipAddressFilters );
    }

    public static List<AAAConfig> getAccountingFilters()
    {
        return Collections.unmodifiableList( accountingFilters );
    }

    public static List<AAAConfig> getUrlRewritingFilters()
    {
        return Collections.unmodifiableList( urlRewritingFilters );
    }

    public static List<AAAConfig> getControlFilters()
    {
        return Collections.unmodifiableList( controlFilters );
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
        sb.append( name ).append( ' ' ).append( version );
        sb.append( " (" ).append( System.getProperty( "os.name" ) );
        sb.append( " / " ).append( System.getProperty( "os.version" ) );
        sb.append( " / " ).append( System.getProperty( "os.arch" ) );
        sb.append( ')' );
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
        if ( !(o instanceof Parameter) )
            throw new IllegalArgumentException( "Only observe parameters" );
    }

    /**
     * @return the application base dir or null if the home directory cannot be
     *         determined.
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
        Logger rootLogger = Logger.getRootLogger();
        if ( debugEnabled.getValue() ) {
            rootLogger.setLevel( Level.DEBUG );
        }

        /*
         * else use the default level set in the log4j configuration file, which
         * is INFO
         */
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
            sb.append( '\n' );
        }

        return sb.toString();
    }

}
