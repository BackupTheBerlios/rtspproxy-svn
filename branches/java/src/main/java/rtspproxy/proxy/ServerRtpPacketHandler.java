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

/**
 * @author Matteo Merli
 */
public class ServerRtpPacketHandler extends IoHandlerAdapter
{

	static Logger log = Logger.getLogger( ServerRtpPacketHandler.class );

	/*
	 * @see org.apache.mina.io.IoHandlerAdapter#dataRead(org.apache.mina.io.IoSession,
	 *      org.apache.mina.common.ByteBuffer)
	 */
	@Override
	public void messageReceived( IoSession session, Object packet ) throws Exception
	{
		DataTunnel dataTunnel = (DataTunnel) session.getAttribute( "dataTunnel" );
		PacketType packetType = (PacketType) session.getAttribute( "sessionType" );
		if ( dataTunnel != null )
			dataTunnel.passToClient( (ByteBuffer) packet, packetType );
		else
			log.debug( "dataTunnel is null" );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.mina.io.IoHandlerAdapter#exceptionCaught(org.apache.mina.io.IoSession,
	 *      java.lang.Throwable)
	 */
	@Override
	public void exceptionCaught( IoSession session, Throwable cause ) throws Exception
	{
		log.debug( "Exception: " + cause );
		session.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.mina.io.IoHandlerAdapter#sessionCreated(org.apache.mina.io.IoSession)
	 */
	@Override
	public void sessionCreated( IoSession session ) throws Exception
	{

	}

}
