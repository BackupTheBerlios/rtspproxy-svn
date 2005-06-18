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

/**
 * 
 */
public class Main
{

	public static void main( String[] args )
	{
		// TODO: remove this temp stuffs
		for ( Object key : System.getProperties().keySet() ) {
			String value = System.getProperty( (String)key );
			System.out.println( key + " : " + value );
		}

		try {
			Reactor reactor = new Reactor();

		} catch ( Exception e ) {
			System.err.println( "Exception in the reactor: " + e );
			System.exit( -1 );
		}
	}
}
