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

import java.net.URI;

import org.apache.log4j.Logger;

/**
 * 
 */
public class MediaObject
{

	static Logger log = Logger.getLogger( MediaObject.class );

	static String fileSeparator = System.getProperty( "file.separator" );

	public static MediaObject resolveURL( URI url )
	{
		// Since we are not supporting virtual hosting,
		// we only care about the second part of the url
		String path = url.getPath();
		StringBuilder absPathBuild = new StringBuilder(
				Config.getValue( "server.document.root" ) );
		absPathBuild.append( fileSeparator );
		if ( fileSeparator != "/" )
			path.replaceAll( "/", fileSeparator );
		absPathBuild.append( path );
		String absPath = absPathBuild.toString();
		
		// now absPath should contains the absoulute path on disk
		// of the media object 

		log.debug( "Abs Path: " + absPath );
		return null;
	}
}
