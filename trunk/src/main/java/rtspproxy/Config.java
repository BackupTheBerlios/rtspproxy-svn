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
		String[] paths = new String[4];

		// Current directory configuration
		paths[3] = "rtspproxy.properties";

		// RtspProxy home folder
		paths[2] = System.getProperty( "rtspproxy.home" ) + File.separator + "conf"
				+ File.separator + "rtspproxy.properties";

		// Per user config
		paths[1] = System.getProperty( "user.home", "" ) + File.separator
				+ ".rtspproxy.properties";
		// System wide configuration (tipical in unix systems)
		paths[0] = "/etc/rtspproxy.properties";

		for ( String path : paths ) {
			try {
				properties.load( new FileInputStream( path ) );
				log.debug( "Reading configurations from '" + path + "'" );
				// break;

			} catch ( IOException e ) {
				// Silently ignore
			}
		}

		for ( Object key : properties.keySet() ) {
			log.debug( (String) key + " : " + properties.getProperty( (String) key ) );
		}
	}

	public static String get( String key, String defaultValue )
	{
		return properties.getProperty( key, defaultValue );
	}

	/**
	 * @param key
	 * @param defaultValue
	 * @return the value of an integer property
	 */
	public static int getInt( String key, int defaultValue )
	{
		try {
			return Integer.parseInt( properties.getProperty( key ) );
		} catch ( Exception e ) {
			return defaultValue;
		}
	}

	/**
	 * Convert a list of comma separated integers string into an array of
	 * integers.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static int[] getIntArray( String key, int defaultValue )
	{
		try {
			String toks[] = properties.getProperty( key ).split( "," );
			int res[] = new int[toks.length];
			int i = 0;
			for ( String tok : toks ) {
				res[i++] = Integer.parseInt( tok.trim() );
			}
			return res;

		} catch ( Exception e ) {
			int res[] = { defaultValue };
			return res;
		}
	}

	/**
	 * Get a boolean property from config.
	 * 
	 * @param key
	 *        the name of the property
	 * @param defaultValue
	 *        its default value
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

		// Try to convert a a String to a boolean
		if ( value.equalsIgnoreCase( "true" ) || value.equalsIgnoreCase( "yes" )
				|| value.equalsIgnoreCase( "1" ) ) {
			return true;
		} else
			if ( value.equalsIgnoreCase( "false" ) || value.equalsIgnoreCase( "no" )
					|| value.equalsIgnoreCase( "0" ) ) {
				return false;
			}

		return defaultValue;
	}

	public static void set( String key, String value )
	{
		properties.setProperty( key, value );
	}

	public static void setBoolean( String key, boolean value )
	{
		properties.setProperty( key, value ? "true" : "false" );
	}

	/**
	 * @param key
	 * @param value
	 */
	public static void setInt( String key, int value )
	{
		properties.setProperty( key, Integer.toString( value ) );
	}

	public static void setIntArray( String key, int[] values )
	{
		StringBuilder build = new StringBuilder();
		for ( int i = 0; i < values.length; i++ ) {
			if ( i > 0 )
				build.append( ", " );
			build.append( Integer.toString( values[i] ) );
		}
		properties.setProperty( key, build.toString() );
	}
}
