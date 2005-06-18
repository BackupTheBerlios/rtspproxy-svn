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

package rtspproxy.lib;


/**
 * Manages all the debug messages of the application.
 * These messages can be disabled using a directive in the 
 * configuration file;
 *
 */
public class Debug
{
	private static boolean debugEnabled = true;

	public static void write( String msg) 
	{
		if ( ! debugEnabled )
			return;
		
		System.err.println( msg );
	}
	
}
