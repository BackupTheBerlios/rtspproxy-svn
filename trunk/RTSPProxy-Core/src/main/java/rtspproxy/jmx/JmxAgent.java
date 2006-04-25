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

package rtspproxy.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import mx4j.log.Log;
import mx4j.tools.adaptor.http.HttpAdaptor;
import mx4j.tools.adaptor.http.XSLTProcessor;
import mx4j.tools.naming.NamingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.ProxyService;
import rtspproxy.RdtClientService;
import rtspproxy.RdtServerService;
import rtspproxy.Reactor;
import rtspproxy.RtcpClientService;
import rtspproxy.RtcpServerService;
import rtspproxy.RtpClientService;
import rtspproxy.RtpServerService;
import rtspproxy.RtspService;
import rtspproxy.config.Config;
import rtspproxy.filter.FilterBase;
import rtspproxy.jmx.mbeans.Filter;
import rtspproxy.jmx.mbeans.Info;
import rtspproxy.jmx.mbeans.PortrangeRtpServerFactory;
import rtspproxy.jmx.mbeans.PortrangeRtpSession;
import rtspproxy.jmx.mbeans.ProxySessionFacade;
import rtspproxy.jmx.mbeans.Service;
import rtspproxy.lib.Singleton;
import rtspproxy.proxy.ProxySession;
import rtspproxy.rtp.range.PortrangeRtpServerSession;
import rtspproxy.rtp.range.PortrangeRtpServerSessionFactory;

/**
 * Entry point class for all the JMX interface.
 * 
 * @author Matteo Merli
 */
public class JmxAgent extends Singleton
{

    private static Logger log = LoggerFactory.getLogger( JmxAgent.class );

    public static final String DOMAIN = "RtspProxy";

    public static final String SERVICES_DOMAIN = "RtspProxy.Services";

    public static final String FILTERS_DOMAIN = "RtspProxy.Filters";

    public static final String RTSP_SESSION_DOMAIN = "RtspProxy.Sessions.RTSP";

    public static final String PROXY_SESSION_DOMAIN = "RtspProxy.Sessions.Proxy";

    public static final String RTP_DYNAMIC_SESSION_DOMAIN = "RtspProxy.Sessions.RTP.dynamic";

    private MBeanServer mbeanServer = null;

    /**
     * Creates a MBean server and attach all the MBeans to it. Also starts, if
     * needed, the web console and the JMX connector server.
     */
    public JmxAgent()
    {
        // Silent mx4j info messages
        System.setProperty( "mx4j.log.priority", "warn" );

        // Redirect mx4j messages to our own logger
        Log.redirectTo( new Mx4jLoggerWrapper() );

        mbeanServer = MBeanServerFactory.createMBeanServer();

        try {

            // Basic Info
            Object infoMBean = new Info();
            ObjectName infoName = ObjectName.getInstance( DOMAIN + ":name=Info" );
            mbeanServer.registerMBean( infoMBean, infoName );

            // Parameters
            Object parametersMBean = mbeanServer.instantiate( ParametersMBean.class
                    .getName() );
            ObjectName parametersName = ObjectName.getInstance( DOMAIN
                    + ":name=Parameters" );
            mbeanServer.registerMBean( parametersMBean, parametersName );

            // Proxy Services
            ProxyService[] proxyServices = { RtspService.getInstance(),
                    RdtClientService.getInstance(), RdtServerService.getInstance(),
                    RtcpClientService.getInstance(), RtcpServerService.getInstance(),
                    RtpClientService.getInstance(), RtpServerService.getInstance() };
            ObjectName objectName;
            for ( ProxyService proxyService : proxyServices ) {
                objectName = ObjectName.getInstance( SERVICES_DOMAIN + ":name="
                        + proxyService.getName() );
                mbeanServer.registerMBean( new Service( proxyService ), objectName );
            }

            startWebConsole();
            startConnectorServer();

        } catch ( Exception e ) {
            log.error( "Exception: ", e );
            Reactor.stop();
        }
    }

    public void stop()
    {
        // TODO: Handle the shutdown of the JMX agent
    }

    private void startWebConsole() throws Exception
    {
        boolean enabled = Config.jmxWebEnable.getValue();
        if ( !enabled )
            return;

        String host = Config.jmxAddress.getValue();
        int port = Config.jmxWebPort.getValue();
        String user = Config.jmxUser.getValue();
        String password = Config.jmxPassword.getValue();

        HttpAdaptor adaptor = new HttpAdaptor();
        ObjectName name = new ObjectName( "Server:name=HttpAdaptor" );
        mbeanServer.registerMBean( adaptor, name );
        adaptor.setHost( host );
        adaptor.setPort( port );
        // MX4J HTTP adaptor only supports Basic authentication
        adaptor.setAuthenticationMethod( "basic" );
        adaptor.addAuthorization( user, password );
        adaptor.start();

        XSLTProcessor processor = new XSLTProcessor();
        processor.setUseCache( true );
        adaptor.setProcessor( processor );

        String url = "http://" + host + ":" + port + "/";
        log.info( "Started web console. Accepting connections on {}", url );
    }

    @SuppressWarnings("unchecked")
    private void startConnectorServer() throws Exception
    {
        boolean enabled = Config.jmxConnectorServiceEnable.getValue();
        if ( !enabled )
            return;

        // Register and start the rmiregistry MBean, needed by JSR 160
        // RMIConnectorServer
        ObjectName namingName = ObjectName.getInstance( "naming:type=rmiregistry" );
        NamingService namingService = new NamingService();
        mbeanServer.registerMBean( namingService, namingName );
        namingService.start();
        int namingPort = ((Integer) mbeanServer.getAttribute( namingName, "Port" ))
                .intValue();

        String jndiPath = "/rtspproxy";
        String host = Config.jmxAddress.getValue();
        String uri = "service:jmx:rmi://" + host + "/jndi/rmi://" + host + ":"
                + namingPort + jndiPath;

        JMXServiceURL url = new JMXServiceURL( uri );

        // Remote Authentication
        JMXAuthenticator authenticator = new Authenticator();
        Map<String, JMXAuthenticator> environment = new HashMap<String, JMXAuthenticator>();
        environment.put( JMXConnectorServer.AUTHENTICATOR, authenticator );

        // Create and start the RMIConnectorServer
        JMXConnectorServer connectorServer = JMXConnectorServerFactory
                .newJMXConnectorServer( url, environment, mbeanServer );
        connectorServer.start();

        log.info( "Started JMX connector server. Service url: {}", uri );
    }

    /**
     * get the singleton instance
     */
    public static JmxAgent getInstance()
    {
        return (JmxAgent) Singleton.getInstance( JmxAgent.class );
    }

    /**
     * register a MBean as a management facade to a filter implementation
     */
    public void registerFilter( FilterBase filter )
    {
        boolean enabled = Config.jmxConnectorServiceEnable.getValue();
        if ( !enabled )
            return;

        try {
            Filter mbean = new Filter( filter );

            mbeanServer.registerMBean( mbean, mbean.getName() );
            filter.setMbeanName( mbean.getName() );
            if ( filter instanceof JmxManageable )
                ((JmxManageable) filter).setMBeanServer( mbeanServer );
        } catch ( Exception e ) {
            log.error( "failed to register filter MBean: filter=" + filter, e );
        }
    }

    /**
     * @return Returns the mbeanServer.
     */
    public MBeanServer getMbeanServer()
    {
        return mbeanServer;
    }

    /**
     * register a proxy session
     * 
     */
    public void registerProxySession( ProxySession session )
    {
        boolean enabled = Config.jmxEnable.getValue();
        if ( !enabled )
            return;

        try {
            ProxySessionFacade mbean = new ProxySessionFacade( session );
            ObjectName name = mbean.buildName();

            mbeanServer.registerMBean( mbean, name );
            session.setObjectName( name );
        } catch ( Exception e ) {
            log.error( "failed to register proxy session MBean: session=" + session, e );
        }
    }

    /**
     * unregister a proxy session
     */
    public void unregisterProxySession( ProxySession session )
    {
        boolean enabled = Config.jmxConnectorServiceEnable.getValue();
        if ( !enabled )
            return;

        try {
            ObjectName name = session.getObjectName();

            if ( name != null ) {
                mbeanServer.unregisterMBean( name );
                session.setObjectName( null );
            }
        } catch ( InstanceNotFoundException infe ) {
            log.debug( "internal problem: MBean not found, name={}", session
                    .getObjectName(), infe );
        } catch ( Exception e ) {
            log.error( "failed to register proxy session MBean: session={}", session, e );
        }

    }

    /**
     * register a proxy session
     * 
     */
    public void registerPortRangeRtpServerSessionfactory(
            PortrangeRtpServerSessionFactory sessionFactory )
    {
        boolean enabled = Config.jmxConnectorServiceEnable.getValue();
        if ( !enabled )
            return;

        try {
            ObjectName objectName = ObjectName.getInstance( SERVICES_DOMAIN
                    + ":name=PortrangeRtpServerSessionFactory" );

            mbeanServer.registerMBean( new PortrangeRtpServerFactory( sessionFactory ),
                    objectName );
        } catch ( Exception e ) {
            log.info( "failed to register PortrangeRtpServerFactory MBean", e );
        }
    }

    /**
     * register a proxy session
     * 
     */
    public void unregisterPortRangeRtpServerSessionfactory()
    {
        boolean enabled = Config.jmxConnectorServiceEnable.getValue();
        if ( !enabled )
            return;

        try {
            ObjectName objectName = ObjectName.getInstance( SERVICES_DOMAIN
                    + ":name=PortrangeRtpServerSessionFactory" );

            mbeanServer.unregisterMBean( objectName );
        } catch ( InstanceNotFoundException infe ) {
            log.debug( "PortrangeRtpServerFactory MBean not found", infe );
        } catch ( Exception e ) {
            log.info( "failed to register PortrangeRtpServerFactory MBean", e );
        }
    }

    /**
     * register a generated RTP server session in the portrange case
     */
    public void registerPortrangeRtpServerSession( PortrangeRtpServerSession session )
    {
        boolean enabled = Config.jmxConnectorServiceEnable.getValue();
        if ( !enabled )
            return;

        try {
            PortrangeRtpSession mbean = new PortrangeRtpSession( session );
            ObjectName name = mbean.buildName();

            mbeanServer.registerMBean( mbean, name );
            session.setObjectName( name );
        } catch ( Exception e ) {
            log.error( "failed to register proxy session MBean: session={}", session, e );
        }
    }

    /**
     * register a generated RTP server session in the portrange case
     */
    public void unregisterPortrangeRtpServerSession( PortrangeRtpServerSession session )
    {
        boolean enabled = Config.jmxConnectorServiceEnable.getValue();
        if ( !enabled )
            return;

        try {
            ObjectName name = session.getObjectName();

            if ( name != null ) {
                mbeanServer.unregisterMBean( name );
                session.setObjectName( null );
            }
        } catch ( InstanceNotFoundException infe ) {
            log.debug( "internal problem: MBean not found, name="
                    + session.getObjectName(), infe );
        } catch ( Exception e ) {
            log.error( "failed to register proxy session MBean: session={}", session, e );
        }
    }

}
