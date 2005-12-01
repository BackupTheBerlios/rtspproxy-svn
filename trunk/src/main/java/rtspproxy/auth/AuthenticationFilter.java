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

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

import rtspproxy.Config;
import rtspproxy.Reactor;
import rtspproxy.lib.Base64;
import rtspproxy.rtsp.RtspCode;
import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;

/**
 * @author Matteo Merli
 *
 */
public class AuthenticationFilter extends IoFilterAdapter
{

	private static Logger log = Logger.getLogger( AuthenticationFilter.class );

	private AuthenticationProvider provider;

	public AuthenticationFilter()
	{
		// Check which backend implementation to use
		// Default is plain-text implementation
		String className = Config.get( "proxy.auth.authentication.implementationClass",
				"rtspproxy.auth.PlainTextAuthenticationProvider" );

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

		log.info( "Using AuthenticationFilter (" + className + ")" );
	}

	public void messageReceived( NextFilter nextFilter, IoSession session, Object message )
			throws Exception
	{
		if ( !( message instanceof RtspRequest ) ) {
			// Shouldn't happen
			log.warn( "Object message is not a RTSP message" );
			return;
		}

		if ( session.getAttribute( "auth" ) != null ) {
			// Client already autheticated
			log.info( "Already authenticaed: " + session.getAttribute( "auth" ) );
			nextFilter.messageReceived( session, message );
		}

		String authString = ( (RtspMessage) message ).getHeader( "Proxy-Authorization" );
		if ( authString == null ) {
			log.debug( "RTSP message: \n" + message );
			RtspResponse response = RtspResponse.errorResponse( RtspCode.Unauthorized );
			// TODO: move the signature to something static!
			String proxySignature = "RtspProxy "
					+ Config.get( "proxy.rtsp.interface", "" );
			response.setHeader( "Proxy-Authenticate", "Basic realm=\"" + proxySignature
					+ "\"" );

			// TODO: I should be able to send a RtspMessage here using the 
			//       already provided encoder.
			WriteFuture written = session.write( ByteBuffer.wrap( response.toString().getBytes() ) );
			// Why have I to wait here????
			written.join();
			session.close();
			return;
		}

		authString = authString.split( " " )[1];
		basicAuthentication( authString );

		// Forward message
		nextFilter.messageReceived( session, message );
	}

	private boolean basicAuthentication( String authString )
	{
		byte[] decBytes = Base64.decode( authString );
		StringBuilder sb = new StringBuilder();
		for ( byte b : decBytes ) {
			sb.append( (char) b );
		}
		String auth = sb.toString();
		log.debug( "auth: " + auth );
		String username = auth.split( ":", 2 )[0];
		String password = auth.split( ":", 2 )[1];
		log.debug( "username=" + username + " - password=" + password );

		return provider.isAuthenticated( username, password );
	}
}
