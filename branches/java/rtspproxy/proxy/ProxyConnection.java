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

import java.net.Socket;

import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;

/**
 * Wraps a proxy connection, managing server and client sides.
 */
public class ProxyConnection
{

	private ClientSide clientSide;
	private ServerSide serverSide;

	public ProxyConnection( Socket clientSocket )
	{
		clientSide = new ClientSide( this, clientSocket );
		serverSide = new ServerSide( this );
	}

	public void passToServer( RtspMessage message )
	{
	}

	public void passToClient( RtspMessage message )
	{
	}

	// Special cases
	public void passSetupRequestToServer( RtspRequest request )
	{
	}

	public void passSetupResponseToClient( RtspResponse response )
	{
	}

}
