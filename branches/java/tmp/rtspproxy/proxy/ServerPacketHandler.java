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
import org.apache.mina.io.IoHandlerAdapter;
import org.apache.mina.io.IoSession;

/**
 * @author mat
 * 
 */
public class ServerPacketHandler extends IoHandlerAdapter
{

	static Logger log = Logger.getLogger( ServerPacketHandler.class );

	/*
	 * 
	 * @see org.apache.mina.io.IoHandlerAdapter#dataRead(org.apache.mina.io.IoSession,
	 *      org.apache.mina.common.ByteBuffer)
	 */
	@Override
	public void dataRead( IoSession session, ByteBuffer packet ) throws Exception
	{
		DataTunnel dataTunnel = (DataTunnel) session.getAttribute( "dataTunnel" );
		DataTunnel.ChannelType channelType = (DataTunnel.ChannelType) session.getAttribute( "channelType" );
		if ( dataTunnel != null )
			dataTunnel.passToClient( packet, channelType );
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
