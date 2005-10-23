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

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

import rtspproxy.rtp.RtpPacket;

public class ClientRtpPacketHandler extends IoHandlerAdapter
{

	static Logger log = Logger.getLogger( ClientRtpPacketHandler.class );

	@Override
	public void sessionCreated( IoSession session ) throws Exception
	{

	}

	@Override
	public void messageReceived( IoSession session, Object buffer ) throws Exception
	{
		RtpPacket packet = new RtpPacket( (ByteBuffer) buffer );
		log.debug( "Received RTP packet: " + packet.getSequence() );
		
		Track track = (Track)session.getAttribute( "track" ); 
		/* = Track.getByClientSSRC( packet.getSsrc() );

		if ( track == null ) {
			// drop packet
			log.debug( "Invalid SSRC identifier: " + Long.toHexString( packet.getSsrc() ) );
			return;
		}
		*/
		track.forwardRtpToServer( packet );
	}

	@Override
	public void exceptionCaught( IoSession session, Throwable cause ) throws Exception
	{
		log.debug( "Exception: " + cause );
		session.close();
	}
}
