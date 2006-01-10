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

package rtspproxy.proxy;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.TrafficMask;

import rtspproxy.lib.Exceptions;
import rtspproxy.proxy.track.RdtTrack;
import rtspproxy.proxy.track.Track;
import rtspproxy.rdt.RdtFilterChainBuilder;
import rtspproxy.rdt.RdtPacket;
import rtspproxy.rdt.RdtPacketDecoder;

/**
 * Handles RDT packets from server and forward them to client. The RTSP 
 * session is obtained using the client IP address and port.
 * 
 * @author Matteo Merli
 */
public class ServerRdtPacketHandler extends IoHandlerAdapter
{

	private static Logger log = LoggerFactory.getLogger( ServerRdtPacketHandler.class );

	/**
	 * this sessionCreated method is an ugly hack. It suspends the session for a moment and
	 * checks the filter chain if the protocol filter has already been applied to the 
	 * session. If not, it assembles the filter chain. This should have been done by the acceptor
	 * (which he does not do (in mina 0.9.0))
	 */
	@Override
	public void sessionCreated( IoSession session ) throws Exception
	{
		TrafficMask mask = session.getTrafficMask();
		
		try {
			session.setTrafficMask(TrafficMask.NONE);
			
			IoFilterChain chain = session.getFilterChain();
			
			if(!chain.contains(RdtFilterChainBuilder.rdtCODEC)) 
				(new RdtFilterChainBuilder()).buildFilterChain(chain);
		} finally {
			session.setTrafficMask(mask);
		}
	}

	@Override
	public void messageReceived( IoSession session, Object buffer ) throws Exception
	{
		if(buffer instanceof RdtPacket) {
			RdtPacket rdtPacket = (RdtPacket)buffer;
			
			log.debug( "Received RDT packet from server, packet=" + rdtPacket );

			RdtTrack track = (RdtTrack)Track.getByServerAddress( (InetSocketAddress) session.getRemoteAddress() );

			if ( track == null ) {
				// drop packet
				log.debug( "Invalid address: "
						+ (InetSocketAddress) session.getRemoteAddress()
						+ " - Class: "
						+ ( (InetSocketAddress) session.getRemoteAddress() ).getAddress().getClass() );
				return;
			}

			track.forwardRdtToClient( rdtPacket );			
		} else {
			log.debug("invalid object passed: " + buffer.getClass().getName());
			
			throw new IllegalStateException("invalid packet on chain");
		}
	}

	@Override
	public void exceptionCaught( IoSession session, Throwable cause ) throws Exception
	{
		log.info( "Exception: " + cause );
		Exceptions.logStackTrace( cause );
		session.close();
	}
}
