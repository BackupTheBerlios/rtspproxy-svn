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

import org.apache.log4j.Logger;

import rtspproxy.lib.SignalInterceptor;
import rtspproxy.lib.SignalInterceptorException;

/**
 * 
 */
public class Main extends SignalInterceptor
{

	static Logger log = Logger.getLogger( "rtspproxy" );

	public static void main( String[] args )
	{
		// TODO: remove this temp stuffs
		/*
		 * for ( Object key : System.getProperties().keySet() ) { String value =
		 * System.getProperty( (String)key ); System.out.println( key + " : " +
		 * value ); }
		 */

		// Register the "rtsp://" protocol scheme
		System.setProperty( "java.protocol.handler.pkgs", "rtspproxy" );

		new Config();

		// Register the signal handler
		try {
			Main main = new Main();
			main.register( "TERM" );
			main.register( "INT" );
			log.debug( "Signal handlig enabled." );

		} catch ( SignalInterceptorException sie ) {
			log.warn( "Unable to register signal handling." );
		}

		try {
			log.info( "Starting " + Config.getName() + " " + Config.getVersion() );
			Reactor.setStandalone( true );
			Reactor.start();

		} catch ( Exception e ) {
			log.fatal( "Exception in the reactor: " + e );
			e.printStackTrace();
			System.exit( -1 );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.lib.SignalInterceptor#handle(java.lang.String)
	 */
	@Override
	protected boolean handle( String signalName )
	{
		log.info( "Received signal: " + signalName );
		try {
			log.info( "Stopping " + Config.getName() + " " + Config.getVersion() );
			Reactor.stop();

		} catch ( Exception e ) {
			log.fatal( "Exception in the reactor: " + e );
			e.printStackTrace();
		}

		return true;
	}

}
