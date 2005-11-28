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
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoFilter;
import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.TransportType;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.registry.Service;

import rtspproxy.auth.AuthenticationFilter;
import rtspproxy.auth.IpAddressFilter;
import rtspproxy.filter.impl.RequestUrlRewritingImpl;
import rtspproxy.lib.Exceptions;
import rtspproxy.proxy.ClientSide;
import rtspproxy.rtsp.Handler;
import rtspproxy.rtsp.RtspDecoder;
import rtspproxy.rtsp.RtspEncoder;

/**
 * @author Matteo Merli
 */
public class RtspService implements ProxyService
{

	private static Logger log = Logger.getLogger( RtspService.class );

	private static ProtocolCodecFactory codecFactory = new ProtocolCodecFactory()
	{

		// Decoders can be shared 
		private ProtocolEncoder rtspEncoder = new RtspEncoder();
		private ProtocolDecoder rtspDecoder = new RtspDecoder();

		public ProtocolEncoder getEncoder()
		{
			return rtspEncoder;
		}

		public ProtocolDecoder getDecoder()
		{
			return rtspDecoder;
		}
	};

	private static IoFilter codecFilter = new ProtocolCodecFilter( codecFactory );

	public void start() throws IOException
	{
		// get port and network interface from config file
		int[] ports = Config.getIntArray( "proxy.rtsp.port", Handler.DEFAULT_RTSP_PORT );
		String netInterface = Config.get( "proxy.rtsp.interface", null );

		for ( int port : ports ) {
			try {

				Service service;
				if ( netInterface == null )
					service = new Service( "RtspService", TransportType.SOCKET, port );
				else
					service = new Service( "RtspService", TransportType.SOCKET,
							new InetSocketAddress( netInterface, port ) );

				Reactor.getRegistry().bind( service, new ClientSide() );

				log.info( "RtspService Started - Listening on: "
						+ InetAddress.getByName( netInterface ) + ":" + port );

			} catch ( IOException e ) {
				log.fatal( e.getMessage() + " (port = " + port + ")" );
				throw e;
			}
		}

		IoAcceptor acceptor = Reactor.getRegistry().getAcceptor( TransportType.SOCKET );
		IoFilterChain filterChain = acceptor.getFilterChain();
		try {
			boolean enableIpAddressFilter = Config.getBoolean(
					"proxy.auth.ipAddressFilter.enable", false );
			if ( enableIpAddressFilter )
				filterChain.addLast( "ipfilter", new IpAddressFilter() );

			// The codec filter is always present
			filterChain.addLast( "codec", codecFilter );

			boolean enableAuthenticationFilter = Config.getBoolean(
					"proxy.auth.authentication.enable", false );
			if ( enableAuthenticationFilter )
				filterChain.addLast( "authentication", new AuthenticationFilter() );

			for ( Object obj : filterChain.getAll() ) {
				log.debug( "Filter: " + filterChain.getName( (IoFilter) obj ) );
			}

		} catch ( Exception e ) {
			log.fatal( "Cannot register session filter: " + e );
			Exceptions.logStackTrace( e );
			Reactor.stop();
		}
	}

	public void stop() throws Exception
	{
		for ( Object service : Reactor.getRegistry().getServices( "RtspService" ) ) {
			Reactor.getRegistry().unbind( (Service) service );
		}

		log.info( "RtspService Stopped" );
	}
}
