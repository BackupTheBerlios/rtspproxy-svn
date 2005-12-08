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

package rtspproxy.filter.authentication.scheme;

/**
 * Base interface for diverse authentication schemes such as "Basic", "Digest"
 * and possibly others.
 * 
 * @author Matteo Merli
 */
public interface AuthenticationScheme
{

	/**
	 * @return the name of the authentication scheme.
	 */
	public String getName();
	
	public Credentials getCredentials( String authString );

}