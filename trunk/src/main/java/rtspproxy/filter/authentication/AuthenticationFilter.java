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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;

import rtspproxy.Reactor;
import rtspproxy.config.Config;
import rtspproxy.filter.authentication.scheme.AuthenticationScheme;
import rtspproxy.filter.authentication.scheme.BasicAuthentication;
import rtspproxy.filter.authentication.scheme.Credentials;
import rtspproxy.filter.authentication.scheme.DigestAuthentication;
import rtspproxy.rtsp.RtspCode;
import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;

/**
 * @author Matteo Merli
 */
public class AuthenticationFilter extends IoFilterAdapter
{

	private static Logger log = Logger.getLogger( AuthenticationFilter.class );

	private static final String ATTR = AuthenticationFilter.class.toString() + "Attr";

	private static final Map<String, Class> schemeRegistry = new HashMap<String, Class>();

	static {
		// Fill in known schemes
		schemeRegistry.put( "basic", BasicAuthentication.class );
		schemeRegistry.put( "digest", DigestAuthentication.class );
	}

	/**
	 * Backend provider.
	 */
	private AuthenticationProvider provider;

	/** Different authentication schemes implementation */
	private AuthenticationScheme scheme = null;

	/**
	 * Construct a new AuthenticationFilter. Looks at the configuration to load
	 * the choseen backend implementation.
	 */
	public AuthenticationFilter()
	{
		// Check which backend implementation to use
		// Default is plain-text implementation
		String className = Config.proxyFilterAuthenticationImplementationClass.getValue();

		Class providerClass;
		try {
			providerClass = Class.forName( className );

		} catch ( ClassNotFoundException e ) {
			log.fatal( "Invalid AuthenticationProvider class: " + className );
			Reactor.stop();
			return;
		}

		// Check if the class implements the IpAddressProvider interfaces
		boolean found = false;
		for ( Class interFace : providerClass.getInterfaces() ) {
			if ( AuthenticationProvider.class.equals( interFace ) ) {
				found = true;
				break;
			}
		}

		if ( !found ) {
			log.fatal( "Class (" + providerClass
					+ ") does not implement the AuthenticationProvider interface." );
			Reactor.stop();
			return;
		}

		try {
			provider = (AuthenticationProvider) providerClass.newInstance();
			provider.init();

		} catch ( Exception e ) {
			log.fatal( "Error starting AuthenticationProvider: " + e );
			Reactor.stop();
			return;
		}

		// Validate the choosen authentication scheme
		String schemeName = Config.proxyFilterAuthenticationScheme.getValue();
		Class schemeClass = schemeRegistry.get( schemeName.toLowerCase() );
		if ( schemeClass == null ) {
			// scheme not found
			log.fatal( "Authentication Scheme not found: " + schemeName
					+ ". Valid values are: "
					+ Arrays.toString( schemeRegistry.keySet().toArray() ) );
			Reactor.stop();
			return;
		}

		// Instanciate the selected scheme
		try {
			scheme = (AuthenticationScheme) schemeClass.newInstance();
		} catch ( Exception e ) {
		}

		log.info( "Using AuthenticationFilter " + scheme.getName() + " (" + className
				+ ")" );
	}

	public void messageReceived( NextFilter nextFilter, IoSession session, Object message )
			throws Exception
	{
		if ( !(message instanceof RtspRequest) ) {
			// Shouldn't happen
			log.warn( "Object message is not a RTSP message" );
			return;
		}

		if ( session.getAttribute( ATTR ) != null ) {
			// Client already autheticated
			log.debug( "Already authenticaed: " + session.getAttribute( ATTR ) );
			nextFilter.messageReceived( session, message );
		}

		String authString = ((RtspMessage) message).getHeader( "Proxy-Authorization" );
		if ( authString == null ) {
			log.debug( "RTSP message: \n" + message );
			RtspResponse response = RtspResponse
					.errorResponse( RtspCode.ProxyAuthenticationRequired );
			response.setHeader( "Proxy-Authenticate", scheme.getName() + " "
					+ scheme.getChallenge() );

			log.debug( "Sending RTSP message: \n" + response );

			session.write( response );
			return;
		}

		if ( !validateAuthenticationScheme( authString ) ) {
			RtspResponse response = RtspResponse.errorResponse( RtspCode.BadRequest );

			session.write( response );
			return;
		}

		log.debug( "RTSP message: \n" + message );

		// Check the authentication credentials
		Credentials credentials = scheme.getCredentials( (RtspMessage) message );

		boolean authenticationOk = false;
		if ( credentials != null ) {
			String password = provider.getPassword( credentials.getUserName() );
			if ( password != null )
				if ( scheme.computeAuthentication( credentials, password ) == true )
					authenticationOk = true;
		}

		if ( !authenticationOk ) {
			log.warn( "Authentication failed for user: " + credentials );
			RtspResponse response = RtspResponse
					.errorResponse( RtspCode.ProxyAuthenticationRequired );
			response.setHeader( "Proxy-Authenticate", scheme.getName() + " "
					+ scheme.getChallenge() );

			session.write( response );
			return;
		}

		log.debug( "Authentication succesfull for user: " + credentials );

		/*
		 * Mark the session with an "authenticated" attribute. This will prevent
		 * the check for the credentials for every message received.
		 */
		session.setAttribute( ATTR, credentials.getUserName() );

		// Forward message
		nextFilter.messageReceived( session, message );
	}

	/**
	 * Gets the authentication scheme stated by the client.
	 * 
	 * @param authString
	 * @return
	 */
	private boolean validateAuthenticationScheme( String authString )
	{
		String schemeName;
		try {
			schemeName = authString.split( " " )[0];
		} catch ( IndexOutOfBoundsException e ) {
			// Malformed auth string
			return false;
		}

		if ( schemeName.equalsIgnoreCase( scheme.getName() ) )
			return true;

		// Scheme not valid
		return false;
	}

}
