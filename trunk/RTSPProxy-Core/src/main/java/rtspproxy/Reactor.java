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

import org.apache.log4j.Logger;

import rtspproxy.config.Config;
import rtspproxy.config.ConfigReader;
import rtspproxy.jmx.JmxAgent;
import rtspproxy.lib.Exceptions;

/**
 * 
 */
public class Reactor
{

	private static Logger log = Logger.getLogger( Reactor.class );

	private static ProxyServiceRegistry registry = null;

	private static JmxAgent jmxAgent = null;

	private static boolean isStandalone = false;

	public static void setStandalone( boolean standalone )
	{
		isStandalone = standalone;
	}

	/**
	 * Constructor. Creates a new Reactor and starts it.
	 */
	static public void start() throws Exception
	{
		// Read configuration files
		new Config();

		String[] paths = new String[5];

		// Used for testing purposes:
		// checks for the configuration file
		paths[4] = "src/resources/conf/rtspproxy.properties";

		// Current directory configuration
		paths[3] = "rtspproxy.properties";

		// RtspProxy home folder
		paths[2] = Config.getHome() + "/conf/rtspproxy.properties";

		// Per user config
		paths[1] = System.getProperty( "user.home", "" ) + "/.rtspproxy.properties";
		// System wide configuration (tipical in unix systems)
		paths[0] = "/etc/rtspproxy.properties";

		for ( String path : paths ) {
			new ConfigReader( path );
		}

		if ( log.isDebugEnabled() ) {
			log.debug( Config.debugParameters() );
		}

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
	}

	static public void stop()
	{
		try {
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
