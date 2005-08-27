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

import rtspproxy.lib.NoPortAvailableException;
import rtspproxy.lib.PortManager;
import rtspproxy.proxy.ClientRtcpPacketHandler;
import rtspproxy.proxy.ClientRtpPacketHandler;

/**
 * This service is responsible of receiving and sending RTP and RTCP packets to
 * clients.
 * 
 * @author Matteo Merli
 */
public class RtpClientService implements ProxyService
{

	static Logger log = Logger.getLogger( RtpClientService.class );

	public void start() throws IOException, NoPortAvailableException
	{
		int rtpPort = Config.getInt( "proxy.client.rtp.port", 6792 );
		int rtcpPort = Config.getInt( "proxy.client.rtcp.port", 6793 );
		String netInterface = Config.get( "proxy.client.interface", null );
		boolean dinPorts = Config.getBoolean( "port.client.dynamicPorts", false );

		// If dinPorts is true, we have to first check the availability
		// of the ports and choose 2 valid ports.
		if ( dinPorts ) {
			int[] ports = PortManager.findAvailablePorts( 2, rtpPort );
			rtpPort = ports[0];
			rtcpPort = ports[1];
		}

		InetSocketAddress rtpAddr = new InetSocketAddress(
				InetAddress.getByName( netInterface ), rtpPort );
		InetSocketAddress rtcpAddr = new InetSocketAddress(
				InetAddress.getByName( netInterface ), rtcpPort );

		try {
			Service rtpService, rtcpService;

			rtpService = new Service( "RtpClientService", TransportType.DATAGRAM, rtpAddr );
			rtcpService = new Service( "RtcpClientService", TransportType.DATAGRAM,
					rtcpAddr );

			Reactor.getRegistry().bind( rtpService, new ClientRtpPacketHandler() );
			Reactor.getRegistry().bind( rtcpService, new ClientRtcpPacketHandler() );
			log.info( "Listening on ports: " + rtpPort + "-" + rtcpPort );

		} catch ( IOException e ) {
			log.fatal( "Can't start the service. " + e );
			throw e;
		}
	}

	public void stop()
	{
		for ( Object service : Reactor.getRegistry().getServices( "RtpClientService" ) ) {
			Reactor.getRegistry().unbind( (Service) service );
		}
		for ( Object service : Reactor.getRegistry().getServices( "RtcpClientService" ) ) {
			Reactor.getRegistry().unbind( (Service) service );
		}
	}
}
