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
import java.net.SocketAddress;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.TransportType;
import org.apache.mina.registry.Service;

import rtspproxy.lib.NoPortAvailableException;
import rtspproxy.lib.PortManager;
import rtspproxy.proxy.ClientRdtPacketHandler;

/**
 * This service is responsible of receiving and sending RTP and RTCP packets to
 * clients.
 * 
 * @author Matteo Merli
 */
public class RdtClientService implements ProxyService
{

	private static Logger log = Logger.getLogger( RtpClientService.class );

	private static InetSocketAddress rdtAddress = null;
	
	private static final String NAME = "RdtClientService";

	public void start() throws IOException, NoPortAvailableException
	{
		int rdtPort = Config.getInt( "proxy.client.rdt.port", 8018 );
		String netInterface = Config.get( "proxy.client.interface", null );
		boolean dinPorts = Config.getBoolean( "proxy.client.dynamicPorts", false );

		// If dinPorts is true, we have to first check the availability
		// of the ports and choose 2 valid ports.
		if ( dinPorts ) {
			int[] ports = PortManager.findAvailablePorts( 1, rdtPort );
			rdtPort = ports[0];
		}

		rdtAddress = new InetSocketAddress( InetAddress.getByName( netInterface ),
				rdtPort );

		try {
			Service rdtService;

			rdtService = new Service( NAME, TransportType.DATAGRAM,
					rdtAddress );
			
			Reactor.getRegistry().bind( rdtService, new ClientRdtPacketHandler() );
			log.info( "RdtClientService Started - Listening on: "
					+ InetAddress.getByName( netInterface ) + " " + rdtPort  );

		} catch ( IOException e ) {
			log.fatal( "Can't start RdtClientService. " + e );
			throw e;
		}
	}

	public void stop()
	{
		Reactor.getRegistry().unbind( NAME );
		log.info( "RdtClientService Stopped" );
	}

	public static IoSession newRdtSession( SocketAddress remoteAddress )
	{
		return Reactor.getRegistry().getAcceptor( NAME ).newSession(
				remoteAddress, rdtAddress );
	}

	public static InetSocketAddress getRdtAddress()
	{
		return rdtAddress;
	}

	public static InetAddress getHostAddress()
	{
		return rdtAddress.getAddress();
	}

	public static int getPort()
	{
		return rdtAddress.getPort();
	}

}
