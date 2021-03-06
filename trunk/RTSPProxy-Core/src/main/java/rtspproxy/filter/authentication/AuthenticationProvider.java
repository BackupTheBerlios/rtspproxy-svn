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
package rtspproxy.filter.authentication;

import rtspproxy.filter.GenericProvider;
import rtspproxy.filter.authentication.scheme.Credentials;

/**
 * Interface for authentication providers
 * 
 * @author Matteo Merli
 */
public interface AuthenticationProvider extends GenericProvider
{

	/**
	 * Called every time that there is the need to verify the identity of a
	 * user.
	 * 
	 * @param credentials
	 *        User credentials (username and password)
	 * @return true if the user succesfull authenticate with the given username
	 *         and password. false if user is not present or the password is
	 *         wrong.
	 */
	public boolean isAuthenticated( Credentials credentials );
	
	public String getPassword( String username );	
}
