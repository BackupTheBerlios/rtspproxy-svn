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
import rtspproxy.lib.Singleton;

/**
 * Entry point class for all the JMX interface.
 * 
 * @author Matteo Merli
 */
public class JmxAgent extends Singleton
{

	private static Logger log = LoggerFactory.getLogger( JmxAgent.class );

	static final String DOMAIN = "RtspProxy";

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
		Log.redirectTo( new Slf4JLogger() );

		mbeanServer = MBeanServerFactory.createMBeanServer();

		try {

			// Basic Info
			Object infoMBean = new Info();
			ObjectName infoName = ObjectName.getInstance( DOMAIN + ":name=Info" );
			mbeanServer.registerMBean( infoMBean, infoName );

			// Parameters
			Object parametersMBean = mbeanServer.instantiate( ParametersMBean.class.getName() );
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
				objectName = ObjectName.getInstance( DOMAIN + ":name="
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
		boolean enabled = Config.proxyManagementWebEnable.getValue();
		if ( !enabled )
			return;

		String host = Config.proxyManagementHost.getValue();
		int port = Config.proxyManagementWebPort.getValue();
		String user = Config.proxyManagementUser.getValue();
		String password = Config.proxyManagementPassword.getValue();

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
		log.info( "Started web console. Accepting connections on " + url );
	}

	@SuppressWarnings("unchecked")
	private void startConnectorServer() throws Exception
	{
		boolean enabled = Config.proxyManagementRemoteEnable.getValue();
		if ( !enabled )
			return;

		// Register and start the rmiregistry MBean, needed by JSR 160
		// RMIConnectorServer
		ObjectName namingName = ObjectName.getInstance( "naming:type=rmiregistry" );
		NamingService namingService = new NamingService();
		mbeanServer.registerMBean( namingService, namingName );
		namingService.start();
		int namingPort = ( (Integer) mbeanServer.getAttribute( namingName, "Port" ) ).intValue();

		String jndiPath = "/rtspproxy";
		String host = Config.proxyManagementHost.getValue();
		String uri = "service:jmx:rmi://" + host + "/jndi/rmi://" + host + ":"
				+ namingPort + jndiPath;

		JMXServiceURL url = new JMXServiceURL( uri );

		// Remote Authentication
		JMXAuthenticator authenticator = new Authenticator();
		Map<String, JMXAuthenticator> environment = new HashMap<String, JMXAuthenticator>();
		environment.put( JMXConnectorServer.AUTHENTICATOR, authenticator );

		// Create and start the RMIConnectorServer
		JMXConnectorServer connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(
				url, environment, mbeanServer );
		connectorServer.start();

		log.info( "Started JMX connector server. Service url: " + uri );
	}

	/**
	 * get the singleton instance
	 */
	public static JmxAgent getInstance() {
		return (JmxAgent)Singleton.getInstance(JmxAgent.class);
	}
	
	/**
	 * register a MBean as a management facade to a filter implementation
	 */
	public void registerFilter(FilterBase filter) {
		boolean enabled = Config.proxyManagementRemoteEnable.getValue();
		if ( !enabled )
			return;

		try {
			Filter mbean = new Filter(filter);
			
			mbeanServer.registerMBean(mbean, mbean.getName());
			filter.setMbeanName(mbean.getName());
		} catch(Exception e) {
			log.error( "failed to register filter MBean: filter=" + filter, e );			
		}
	}
	
	/**
	 * simple wrapper to log mx4j logging info into slf4j subsystem
	 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
	 *
	 */
	public static class Slf4JLogger extends mx4j.log.Logger {

		private Logger m_logger;
		
		/**
		 * default no-op constructor
		 */
		public Slf4JLogger() {}
		
		/* (non-Javadoc)
		 * @see mx4j.log.Logger#log(int, java.lang.Object, java.lang.Throwable)
		 */
		@Override
		protected void log(int level, Object msg, Throwable t) {
			switch(level) {
			case mx4j.log.Logger.DEBUG:
				this.m_logger.debug(msg.toString(), t);
				break;
			case mx4j.log.Logger.ERROR:
				this.m_logger.error(msg.toString(), t);
				break;
			case mx4j.log.Logger.FATAL:
				this.m_logger.error(msg.toString(), t);
				break;
			case mx4j.log.Logger.INFO:
				this.m_logger.info(msg.toString(), t);
				break;
			case mx4j.log.Logger.TRACE:
				this.m_logger.debug(msg.toString(), t);
				break;
			case mx4j.log.Logger.WARN:
				this.m_logger.warn(msg.toString(), t);
				break;
			}
		}

		/* (non-Javadoc)
		 * @see mx4j.log.Logger#setCategory(java.lang.String)
		 */
		@Override
		protected void setCategory(String arg0) {
			super.setCategory(arg0);
			
			this.m_logger = LoggerFactory.getLogger(arg0);
		}		
	}
}
