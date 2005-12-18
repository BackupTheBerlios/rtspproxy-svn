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

	private static Logger log = Logger.getLogger( RtpClientService.class );

	private static InetSocketAddress rtpAddress = null;
	private static InetSocketAddress rtcpAddress = null;

	private static final String rtpNAME = "RtpClientService";
	private static final String rtcpNAME = "RtcpClientService";

	public void start() throws IOException, NoPortAvailableException
	{
		int rtpPort = Config.getInt( "proxy.client.rtp.port", 8002 );
		int rtcpPort = Config.getInt( "proxy.client.rtcp.port", 8003 );
		String netInterface = Config.get( "proxy.client.interface", null );
		boolean dinPorts = Config.getBoolean( "proxy.client.dynamicPorts", false );

		// If dinPorts is true, we have to first check the availability
		// of the ports and choose 2 valid ports.
		if ( dinPorts ) {
			int[] ports = PortManager.findAvailablePorts( 2, rtpPort );
			rtpPort = ports[0];
			rtcpPort = ports[1];
		}

		// Update properties with effective ports
		Config.setInt( "proxy.client.rtp.port", rtpPort );
		Config.setInt( "proxy.client.rtcp.port", rtcpPort );

		rtpAddress = new InetSocketAddress( InetAddress.getByName( netInterface ),
				rtpPort );
		rtcpAddress = new InetSocketAddress( InetAddress.getByName( netInterface ),
				rtcpPort );

		try {
			Service rtpService, rtcpService;

			rtpService = new Service( rtpNAME, TransportType.DATAGRAM, rtpAddress );
			rtcpService = new Service( rtcpNAME, TransportType.DATAGRAM, rtcpAddress );

			Reactor.getRegistry().bind( rtpService, new ClientRtpPacketHandler() );
			Reactor.getRegistry().bind( rtcpService, new ClientRtcpPacketHandler() );
			log.info( "RtpClientService Started - Listening on: "
					+ InetAddress.getByName( netInterface ) + " " + rtpPort + "-"
					+ rtcpPort );

		} catch ( IOException e ) {
			log.fatal( "Can't start RtpClientService. " + e );
			throw e;
		}
	}

	public void stop()
	{
		Reactor.getRegistry().unbind( rtpNAME );
		Reactor.getRegistry().unbind( rtcpNAME );
		log.info( "RtpClientService Stopped" );
	}

	public static IoSession newRtpSession( SocketAddress remoteAddress )
	{
		return Reactor.getRegistry().getAcceptor( rtpNAME ).newSession( remoteAddress,
				rtpAddress );
	}

	public static IoSession newRtcpSession( SocketAddress remoteAddress )
	{
		return Reactor.getRegistry().getAcceptor( rtcpNAME ).newSession( remoteAddress,
				rtcpAddress );
	}

	public static InetSocketAddress getRtpAddress()
	{
		return rtpAddress;
	}

	public static InetSocketAddress getRtcpAddress()
	{
		return rtcpAddress;
	}

	public static InetAddress getHostAddress()
	{
		/*
		 * The InetAddress (IP) is the same for both RTP and RTCP.
		 */
		return rtpAddress.getAddress();
	}

	public static int getRtpPort()
	{
		return rtpAddress.getPort();
	}

	public static int getRtcpPort()
	{
		return rtcpAddress.getPort();
	}
}
