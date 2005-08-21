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

/**
 * 
 */
public class Main
{

	static Logger log = Logger.getLogger( "rtspproxy" );

	public static void main( String[] args )
	{
		// TODO: remove this temp stuffs
		/*
		for ( Object key : System.getProperties().keySet() ) {
			String value = System.getProperty( (String)key );
			System.out.println( key + " : " + value );
		}
		*/
		/*Properties prop = new Properties();
		prop.setProperty( "log4j.rootLogger", "DEBUG, A1" );
		prop.setProperty( "log4j.appender.A1", "org.apache.log4j.ConsoleAppender" );
		prop.setProperty( "log4j.appender.A1.layout", "org.apache.log4j.PatternLayout" );
		prop.setProperty( "log4j.appender.stdout.layout.ConversionPattern", "%5p [%t] (%F:%L) - %m%n" );
		PropertyConfigurator.configure( prop );
		*/ 
		
		BasicConfigurator.configure();
		
		// Register the "rtsp://" protocol scheme
		System.setProperty( "java.protocol.handler.pkgs", Main.class.getPackage().getName() );
		
		new Config();
		
		try {
			log.warn( "Starting.." );
			new Reactor();

		} catch ( Exception e ) {
			log.fatal( "Exception in the reactor: " + e );
			System.exit( -1 );
		}
	}
}
