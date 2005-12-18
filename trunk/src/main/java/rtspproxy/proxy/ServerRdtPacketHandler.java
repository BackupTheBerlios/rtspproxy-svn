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

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

import rtspproxy.lib.Exceptions;
import rtspproxy.proxy.track.RdtTrack;
import rtspproxy.proxy.track.Track;

/**
 * Handles RDT packets from server and forward them to client. The RTSP 
 * session is obtained using the client IP address and port.
 * 
 * @author Matteo Merli
 */
public class ServerRdtPacketHandler extends IoHandlerAdapter
{

	private static Logger log = Logger.getLogger( ServerRdtPacketHandler.class );

	@Override
	public void sessionCreated( IoSession session ) throws Exception
	{
	}

	@Override
	public void messageReceived( IoSession session, Object buffer ) throws Exception
	{
		// RtcpPacket packet = new RtcpPacket( (ByteBuffer) buffer );
		log.debug( "Received RDT packet from server" );

		RdtTrack track = (RdtTrack)Track.getByServerAddress( (InetSocketAddress) session.getRemoteAddress() );

		if ( track == null ) {
			// drop packet
			log.debug( "Invalid address: "
					+ (InetSocketAddress) session.getRemoteAddress()
					+ " - Class: "
					+ ( (InetSocketAddress) session.getRemoteAddress() ).getAddress().getClass() );
			return;
		}

		ByteBuffer receivedBuffer = (ByteBuffer) buffer;
		byte[] bytes = new byte[receivedBuffer.limit()];
		receivedBuffer.get( bytes );
		ByteBuffer rdtPacket = ByteBuffer.wrap( bytes );
		track.forwardRdtToClient( rdtPacket );
	}

	@Override
	public void exceptionCaught( IoSession session, Throwable cause ) throws Exception
	{
		log.info( "Exception: " + cause );
		Exceptions.logStackTrace( cause );
		session.close();
	}
}
