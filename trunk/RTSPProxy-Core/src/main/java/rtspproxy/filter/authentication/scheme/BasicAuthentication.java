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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.RtspService;
import rtspproxy.lib.Base64;
import rtspproxy.lib.StringUtil;
import rtspproxy.rtsp.RtspMessage;

/**
 * Implementation of the Basic authentication scheme.
 * 
 * @author Matteo Merli
 */
public class BasicAuthentication implements AuthenticationScheme
{

	private static Logger log = LoggerFactory.getLogger( BasicAuthentication.class );

	private String realm;

	public BasicAuthentication()
	{
		// Initiazialize the realm string
		realm = "realm=\"RtspProxy @ "
				+ RtspService.getInstance().getAddress().getHostAddress() + "\"";
	}

	public String getName()
	{
		return "Basic";
	}

	public Credentials getCredentials( RtspMessage message )
	{
		String username;
		String password;

		String authString = message.getHeader( "Proxy-Authorization" );

		try {
			// authString = Basic [base64 data]
			authString = authString.split( " " )[1];
			// Basic scheme credential are BASE64 encoded.
			byte[] decBytes = Base64.decode( authString );
			String auth = StringUtil.toString( decBytes );

			log.debug( "auth: " + auth );
			username = auth.split( ":", 2 )[0];
			password = auth.split( ":", 2 )[1];
			log.debug( "username=" + username + " - password=" + password );
		} catch ( Exception e ) {
			log.warn( "Malformed authString: " + authString );
			return null;
		}

		return new Credentials( username, password );
	}

	public String getChallenge()
	{
		// The Basic authentication challenge is simply composed of the Realm
		return realm;
	}

	public boolean computeAuthentication( Credentials credentials, String storedPassword )
	{
		// In basic authentication the password is supplied in clear text
		return storedPassword.equals( credentials.getPassword() );
	}

}
