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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import rtspproxy.lib.Debug;

/**
 * 
 */
public class Reactor
{

	private ServerSocket serverSocket = null;

	/**
	 * Constructor. Creates a new Reactor and starts it.
	 * 
	 */
	public Reactor()
	{
		boolean listening = true;
		int port = 5549;

		try {
			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress( true );
			serverSocket.bind( new InetSocketAddress( port ) );

		} catch ( IOException e ) {
			System.err.println( "Could not listen on port: " + port );
			System.exit( -1 );
		}

		while ( listening ) {
			Socket socket;
			try {
				socket = serverSocket.accept();
				new Connection( socket ).start();
			} catch ( IOException e ) {
				Debug.write( "Error accepting a connection." );
				System.exit( -1 );
			}
		}
		try {
			serverSocket.close();
		} catch ( IOException e ) {
			Debug.write( "Error closing server socket." );
		}
	}

	/**
	 * @return Returns the serverSocket.
	 */
	public ServerSocket getServerSocket()
	{
		return serverSocket;
	}

	/**
	 * @param serverSocket
	 *        The serverSocket to set.
	 */
	public void setServerSocket( ServerSocket serverSocket )
	{
		this.serverSocket = serverSocket;
	}
}
