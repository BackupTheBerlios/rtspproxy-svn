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

package rtspproxy.jmx;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;

import org.apache.log4j.Logger;

import rtspproxy.config.Config;

/**
 * Authenticator for JMX connector server that reads data from configuration
 * parameters.
 * 
 * @author Matteo Merli
 */
public class Authenticator implements JMXAuthenticator
{

	private static Logger log = Logger.getLogger( Authenticator.class );

	public Subject authenticate( Object credentials ) throws SecurityException
	{
		if ( !( credentials instanceof String[] ) )
			throw new SecurityException( "Bad credentials" );

		String[] creds = (String[]) credentials;
		if ( creds.length != 2 )
			throw new SecurityException( "Bad credentials" );

		String user = creds[0];
		String password = creds[1];

		if ( user == null ) {
			log.info( "Authentication failed: null username" );
			throw new SecurityException( "Bad user name" );
		}

		if ( password == null ) {
			log.info( "Authentication failed for user " + user + " null password." );
			throw new SecurityException( "Bad password" );
		}

		// Expected values
		String adminUser = Config.proxyManagementUser.getValue();
		String adminPassword = Config.proxyManagementPassword.getValue();

		if ( !user.equals( adminUser ) || !password.equals( adminPassword ) ) {
			log.info( "Authentication failed for user " + user
					+ ". Invalid username or password." );
			throw new SecurityException( "Invalid username or password." );
		}

		log.debug( "Successful Authentication for user " + user );
		Set<JMXPrincipal> principals = new HashSet<JMXPrincipal>();
		principals.add( new JMXPrincipal( user ) );
		Subject subject = new Subject( true, principals, Collections.EMPTY_SET,
				Collections.EMPTY_SET );
		return subject;
	}
}
