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

/**
 * A server channel is created only to obtain port numbers and inform the
 * server. When the server reply with its ports, the virtual connection will be
 * finally created.
 */
public class ServerChannel extends DataChannel
{

	public ServerChannel( String remoteAddress, int dataPort, int controlPort )
	{
		if ( controlPort != 0 ) {
			
		}
	}
	
	/**
	 * Performs the actual "virtual" connection to server.
	 * @param remoteAddress Server address
	 * @param dataPort UDP data port
	 * @param controlPort UDP control port
	 */
	public void connect( String remoteAddress, int dataPort, int controlPort )
	{
		
	}
}
