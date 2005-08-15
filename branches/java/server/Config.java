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

import java.util.Properties;

/**
 * Configuration manager
 */
public class Config
{

	private static Properties config = new Properties();
	
	// Initialize config with default values
	static {
		// 554 is the default RTSP port
		config.setProperty( "server.rtsp.port", "5541" );
	}

	public static String getValue( String key )
	{
		return config.getProperty( key );
	}

	/**
	 * @param key the name of the property
	 * @return the integer value of the property
	 */
	public static int getValueInt( String key )
	{
		return Integer.parseInt( config.getProperty( key ) );
	}

}
