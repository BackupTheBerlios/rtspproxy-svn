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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Connection manager.
 */
public class Connection extends Thread
{

	private Socket socket = null;

	/**
	 * Constructor. Manages a new incoming connection.
	 */
	public Connection( Socket socket )
	{
		super( "Connection" );
		this.socket = socket;
	}

	/**
	 * Implements the Thread run() method.
	 */
	public void run()
	{
		System.out.println( "New connection from " + socket.getRemoteSocketAddress() );
		try {
			PrintWriter out = new PrintWriter( socket.getOutputStream(), true );
			BufferedReader in = new BufferedReader( new InputStreamReader(
					socket.getInputStream() ) );

			String inputLine, outputLine;

			out.write( "Ciao!\r\n" );
			out.flush();
			socket.close();

		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

}
