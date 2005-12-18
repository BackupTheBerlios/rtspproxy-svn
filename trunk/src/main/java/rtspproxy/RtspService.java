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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.apache.mina.common.TransportType;
import org.apache.mina.registry.Service;

import rtspproxy.filter.RtspClientFilters;
import rtspproxy.proxy.ClientSide;
import rtspproxy.rtsp.Handler;

/**
 * @author Matteo Merli
 */
public class RtspService implements ProxyService
{

	private static Logger log = Logger.getLogger( RtspService.class );

	private static final String NAME = "RtspService";

	public void start() throws IOException
	{
		// get port and network interface from config file
		int[] ports = Config.getIntArray( "proxy.rtsp.port", Handler.DEFAULT_RTSP_PORT );
		String netInterface = Config.get( "proxy.rtsp.interface", null );

		for ( int port : ports ) {
			try {

				Service service;
				if ( netInterface == null )
					service = new Service( NAME, TransportType.SOCKET, port );
				else
					service = new Service( NAME, TransportType.SOCKET,
							new InetSocketAddress( netInterface, port ) );

				Reactor.getRegistry().bind( service, new ClientSide(),
						new RtspClientFilters() );

				log.info( "RtspService Started - Listening on: "
						+ InetAddress.getByName( netInterface ) + ":" + port );

			} catch ( IOException e ) {
				log.fatal( e.getMessage() + " (port = " + port + ")" );
				throw e;
			}
		}
	}

	public void stop() throws Exception
	{
		Reactor.getRegistry().unbind(NAME);
		log.info( "RtspService Stopped" );
	}
}
