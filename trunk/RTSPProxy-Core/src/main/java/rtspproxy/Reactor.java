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

package rtspproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.config.Config;
import rtspproxy.filter.FilterRegistry;
import rtspproxy.jmx.JmxAgent;
import rtspproxy.lib.Exceptions;
import rtspproxy.rtp.range.PortrangeRtpServerSessionFactory;

/**
 * Main reactor of RTSP proxy. This reactor assembles all required services.
 * The reactor expects a valid configuration before startup eg the global configuration must
 * have been filled before starting the reactor.
 */
public class Reactor
{

	private static Logger log = LoggerFactory.getLogger( Reactor.class );

	private static ProxyServiceRegistry registry = null;

	private static JmxAgent jmxAgent = null;

	private static boolean isStandalone = false;
	
	private static FilterRegistry filterRegistry = null;

	public static void setStandalone( boolean standalone )
	{
		isStandalone = standalone;
	}

	/**
	 * Constructor. Creates a new Reactor and starts it.
	 * The reactor relies on configuration info that has to be provided 
	 * <b>before</b> starting the reactor.
	 * @exception Exception reactor startup failed.
	 */
	static public void start() throws Exception
	{
		log.info( "Starting " + Config.getName() + " " + Config.getVersion() );

		registry = new ProxyServiceRegistry();

		// Register the "rtsp://" protocol scheme
		System.setProperty( "java.protocol.handler.pkgs", "rtspproxy" );

		ProxyService rtspService = new RtspService();
		rtspService.start();

		ProxyService rtpClientService = new RtpClientService();
		rtpClientService.start();

		ProxyService rtcpClientService = new RtcpClientService();
		rtcpClientService.start();

		ProxyService rtpServerService = new RtpServerService();
		rtpServerService.start();

		ProxyService rtcpServerService = new RtcpServerService();
		rtcpServerService.start();

		ProxyService rdtClientService = new RdtClientService();
		rdtClientService.start();

		ProxyService rdtServerService = new RdtServerService();
		rdtServerService.start();

		boolean enableJmx = Config.proxyManagementEnable.getValue();
		if ( enableJmx )
			jmxAgent = new JmxAgent();

		filterRegistry = new FilterRegistry();
		filterRegistry.populateRegistry();		
		
		PortrangeRtpServerSessionFactory portrangeFactory = new PortrangeRtpServerSessionFactory();
		portrangeFactory.setLocalAddress(rtpServerService.getAddress());
		portrangeFactory.start();
		
	}

	static public void stop()
	{
		try {
			PortrangeRtpServerSessionFactory.getInstance().stop();
			
			if ( jmxAgent != null )
				jmxAgent.stop();

			if ( registry != null )
				registry.unbindAll();
			
			log.info( "Shutdown completed" );

		} catch ( Exception e ) {
			log.warn( "Error shutting down: " + e );
			Exceptions.logStackTrace( e );
		}

		if ( isStandalone )
			Runtime.getRuntime().halt( 0 );
	}

	protected static ProxyServiceRegistry getRegistry()
	{
		return registry;
	}

}
