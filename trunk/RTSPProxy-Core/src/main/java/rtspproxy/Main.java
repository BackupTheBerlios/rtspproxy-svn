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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import rtspproxy.lib.Exceptions;

/**
 * 
 */
public class Main
{

	static Logger log = Logger.getLogger( "rtspproxy" );

	public static void main( String[] args )
	{
		// Configure the logger with default settings
		// useful to track pre-config file errors
		BasicConfigurator.configure();

		// Register the signal handler
		Runtime.getRuntime().addShutdownHook( new ShutdownHandler() );

		try {
			Reactor.setStandalone( true );
			Reactor.start();

		} catch ( Exception e ) {
			log.fatal( "Exception in the reactor: " + e );
			Exceptions.logStackTrace( e );
			System.exit( -1 );
		}
	}

}
