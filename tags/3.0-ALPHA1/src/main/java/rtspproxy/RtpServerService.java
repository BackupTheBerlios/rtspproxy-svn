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

import rtspproxy.lib.PortManager;
import rtspproxy.proxy.ServerRtcpPacketHandler;
import rtspproxy.proxy.ServerRtpPacketHandler;

/**
 * @author Matteo Merli
 */
public class RtpServerService implements ProxyService
{

	static Logger log = Logger.getLogger( RtpServerService.class );

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.ProxyService#start()
	 */
	public void start() throws Exception
	{
		int rtpPort = Config.getInt( "proxy.server.rtp.port", 6970 );
		int rtcpPort = Config.getInt( "proxy.server.rtcp.port", 6971 );
		String netInterface = Config.get( "proxy.server.interface", null );
		boolean dinPorts = Config.getBoolean( "proxy.server.dynamicPorts", false );

		// If dinPorts is true, we have to first check the availability
		// of the ports and choose 2 valid ports.
		if ( dinPorts ) {
			int[] ports = PortManager.findAvailablePorts( 2, rtpPort );
			rtpPort = ports[0];
			rtcpPort = ports[1];
		}

		// Update properties with effective ports
		Config.setInt( "proxy.server.rtp.port", rtpPort );
		Config.setInt( "proxy.server.rtcp.port", rtcpPort );

		InetSocketAddress rtpAddr = new InetSocketAddress(
				InetAddress.getByName( netInterface ), rtpPort );
		InetSocketAddress rtcpAddr = new InetSocketAddress(
				InetAddress.getByName( netInterface ), rtcpPort );

		try {
			Service rtpService, rtcpService;

			rtpService = new Service( "RtpServerService", TransportType.DATAGRAM, rtpAddr );
			rtcpService = new Service( "RtcpServerService", TransportType.DATAGRAM,
					rtcpAddr );

			Reactor.getRegistry().bind( rtpService, new ServerRtpPacketHandler() );
			Reactor.getRegistry().bind( rtcpService, new ServerRtcpPacketHandler() );
			log.info( "Listening on: " + InetAddress.getByName( netInterface ) + " "+ rtpPort
					+ "-" + rtcpPort );

		} catch ( IOException e ) {
			log.fatal( "Can't start the service. " + e );
			throw e;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.ProxyService#stop()
	 */
	public void stop() throws Exception
	{
		for ( Object service : Reactor.getRegistry().getServices( "RtpServerService" ) ) {
			Reactor.getRegistry().unbind( (Service) service );
		}
		for ( Object service : Reactor.getRegistry().getServices( "RtcpServerService" ) ) {
			Reactor.getRegistry().unbind( (Service) service );
		}
	}

}
