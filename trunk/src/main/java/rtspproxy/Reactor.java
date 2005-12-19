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

/**
 * 
 */
public class Reactor
{

	private static Logger log = Logger.getLogger( Reactor.class );

	private static ProxyServiceRegistry registry = new ProxyServiceRegistry();

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
	}

	static public void stop()
	{
		try {
			registry.unbindAll();
		} catch ( Exception e ) {
			log.debug( "Error shutting down: " + e );
		}

		log.info( "Shutdown completed" );

		if ( isStandalone )
			Runtime.getRuntime().halt( 0 );
	}

	protected static ProxyServiceRegistry getRegistry()
	{
		return registry;
	}

}
