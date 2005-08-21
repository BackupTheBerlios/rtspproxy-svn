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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * General configuration system.
 */
public class Config
{

	private static Logger log = Logger.getLogger( Config.class );
	private static Properties properties = new Properties();

	protected Config()
	{
		String[] paths = new String[3];

		// Current directory configuration
		paths[2] = "rtspproxy.conf";
		// Per user config
		paths[1] = System.getProperty( "user.home", "" ) + File.separator
				+ ".rtspproxy.conf";
		// System wide configuration (tipical in unix systems)
		paths[0] = "/etc/rtspproxy.conf";

		for ( String path : paths ) {
			try {
				properties.load( new FileInputStream( path ) );
				log.info( "Reading configurations from '" + path + "'" );
				// break;

			} catch ( IOException e ) {
				// Silently ignore
			}
		}

		// TODO: remove this
		for ( Object key : properties.keySet() ) {
			System.out.println( (String) key + " : "
					+ properties.getProperty( (String) key ) );
		}
	}

	public static String get( String key, String defaultValue )
	{
		return properties.getProperty( key, defaultValue );
	}

	public static int getInt( String key, int defaultValue )
	{
		try {
			return Integer.parseInt( properties.getProperty( key ) );
		} catch ( Exception e ) {
			return -1;
		}
	}

	/**
	 * Get a boolean property from config.
	 * @param key the name of the property
	 * @param defaultValue its default value
	 * @return the boolean value
	 */
	public static boolean getBoolean( String key, boolean defaultValue )
	{
		String value;

		try {
			value = properties.getProperty( key );
		} catch ( Exception e ) {
			return defaultValue;
		}

		boolean boolValue = defaultValue;

		if ( value != null ) {
			// Try to convert a a String to a boolean
			if ( value.equalsIgnoreCase( "true " ) || value.equalsIgnoreCase( "yes" )
					|| value.equalsIgnoreCase( "1" ) ) {
				boolValue = true;
			} else if ( value.equalsIgnoreCase( "false" )
					|| value.equalsIgnoreCase( "no" ) || value.equalsIgnoreCase( "0" ) ) {
				boolValue = false;
			}
		}
		return boolValue;
	}
}
