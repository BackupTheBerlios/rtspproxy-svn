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
package rtspproxy.auth;

/**
 * Interface for authentication providers
 * 
 * @author Matteo Merli
 */
public interface AuthenticationProvider
{

	/**
	 * Called once at service startup. Should be used to initialize 
	 * the provider. 
	 * @throws Exception
	 */
	public void init() throws Exception;

	/**
	 * Called once at service shutdown.
	 * @throws Exception
	 */
	public void shutdown() throws Exception;

	/**
	 * Called every time that there is the need to verify the
	 * identity of a user.
	 * @param username 
	 * @param password
	 * @return true if the user succesfull authenticate with 
	 * 				the given username and password.
	 * 		  false if user is not present or the password
	 * 				is wrong. 
	 */
	public boolean isAuthenticated( String username, String password );
}
