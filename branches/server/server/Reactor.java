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

package server;

import org.apache.log4j.Logger;
import org.apache.mina.common.TransportType;
import org.apache.mina.registry.Service;
import org.apache.mina.registry.ServiceRegistry;
import org.apache.mina.registry.SimpleServiceRegistry;


/**
 * @author mat
 *
 */
public class Reactor
{
	static Logger log = Logger.getLogger( Reactor.class );
	
	public Reactor() {
		int port = Config.getValueInt( "server.rtsp.port" );
		try {
			ServiceRegistry registry = new SimpleServiceRegistry();

			Service service = new Service( "proxysession", TransportType.SOCKET, port );
			registry.bind( service, new ServerProvider() );
			log.info( "Listening on port: " + port );
		} catch ( Exception e ) {
			log.fatal( e.getMessage() + " (port = " + port + ")" );
		}
	}
}
