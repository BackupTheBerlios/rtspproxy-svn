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


public class ClientPacketHandler extends IoHandlerAdapter
{

	static Logger log = Logger.getLogger( ClientPacketHandler.class );
	

	@Override
	public void sessionCreated( IoSession session ) throws Exception
	{
		
	}

	@Override
	public void dataRead( IoSession session, ByteBuffer packet ) throws Exception
	{
		DataTunnel dataTunnel = (DataTunnel) session.getAttribute( "dataTunnel" );
		PacketType packetType = (PacketType) session.getAttribute( "sessionType" );
		if ( dataTunnel != null )
			dataTunnel.passToServer( packet, packetType );
	}

	@Override
	public void exceptionCaught( IoSession session, Throwable cause ) throws Exception
	{
		log.debug( "Exception: " + cause );
		session.close();
	}
}
