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

package rtspproxy.proxy;

import org.apache.log4j.Logger;
import org.apache.mina.protocol.ProtocolHandlerAdapter;
import org.apache.mina.protocol.ProtocolSession;

import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;

/**
 * @author mat
 * 
 */
public class ServerSide extends ProtocolHandlerAdapter
{

	static Logger log = Logger.getLogger( ServerSide.class );

	@Override
	public void sessionCreated( ProtocolSession session )
	{
		log.info( "Created session to server: " + session.getRemoteAddress() );

	}

	@Override
	public void sessionClosed( ProtocolSession session )
	{
		log.info( "Server connection closed" );
		ProxyHandler proxyHandler = (ProxyHandler) ( session.getAttribute( "proxyHandler" ) );
		proxyHandler.closeAll();
	}

	@Override
	public void exceptionCaught( ProtocolSession session, Throwable cause )
			throws Exception
	{
		// close all: same as sessionClosed()
		log.info( "Exception: " + cause );
		sessionClosed( session );
	}

	public void onRequestAnnounce( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST ANNOUNCE" );
		proxyHandler.passToClient( request );
	}

	public void onRequestDescribe( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST DESCRIBE" );
		proxyHandler.passToClient( request );
	}

	public void onRequestGetParam( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST GET_PARAMETER" );
		proxyHandler.passToClient( request );
	}

	public void onRequestOptions( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST OPTIONS" );
		proxyHandler.passToClient( request );
	}

	public void onRequestPause( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST PAUSE" );
		proxyHandler.passToClient( request );
	}

	public void onRequestPlay( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST PLAY" );
		proxyHandler.passToClient( request );
	}

	public void onRequestRecord( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST RECORD" );
		proxyHandler.passToClient( request );
	}

	public void onRequestRedirect( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST REDIRECT" );
		proxyHandler.passToClient( request );
	}

	public void onRequestSetParam( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST SET_PARAMETER" );
		proxyHandler.passToClient( request );
	}

	public void onRequestSetup( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST SETUP" );
		proxyHandler.passToClient( request );
	}

	public void onRequestTeardown( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST TEARDOWN" );
		proxyHandler.passToClient( request );
	}

	public void onResponseAnnounce( ProxyHandler proxyHandler, RtspResponse response )
	{
		log.debug( "RESPONSE ANNOUNCE" );
		proxyHandler.passToClient( response );
	}

	public void onResponseDescribe( ProxyHandler proxyHandler, RtspResponse response )
	{
		log.debug( "RESPONSE DESCRIBE" );
		proxyHandler.passToClient( response );
	}

	public void onResponseGetParam( ProxyHandler proxyHandler, RtspResponse response )
	{
		
		proxyHandler.passToClient( response );
	}

	public void onResponseOptions( ProxyHandler proxyHandler, RtspResponse response )
	{
		proxyHandler.passToClient( response );
	}

	public void onResponsePause( ProxyHandler proxyHandler, RtspResponse response )
	{
		proxyHandler.passToClient( response );
	}

	public void onResponsePlay( ProxyHandler proxyHandler, RtspResponse response )
	{
		proxyHandler.passToClient( response );
	}

	public void onResponseRecord( ProxyHandler proxyHandler, RtspResponse response )
	{
		proxyHandler.passToClient( response );
	}

	public void onResponseRedirect( ProxyHandler proxyHandler, RtspResponse response )
	{
		proxyHandler.passToClient( response );
	}

	public void onResponseSetParam( ProxyHandler proxyHandler, RtspResponse response )
	{
		proxyHandler.passToClient( response );
	}

	public void onResponseSetup( ProxyHandler proxyHandler, RtspResponse response )
	{
		proxyHandler.passSetupResponseToClient( response );
	}

	public void onResponseTeardown( ProxyHandler proxyHandler, RtspResponse response )
	{
		proxyHandler.passToClient( response );
	}

	@Override
	public void messageReceived( ProtocolSession session, Object message )
	{
		RtspMessage rtspMessage = (RtspMessage) message;
		log.debug( "Received message:\n" + message );

		ProxyHandler proxyHandler = (ProxyHandler) ( session.getAttribute( "proxyHandler" ) );
		if ( proxyHandler == null ) {
			log.fatal( "proxyHandler is null" );
			throw new NullPointerException( "proxyHandler in session is null" );
		}

		switch ( rtspMessage.getType() ) {
			case TypeRequest:
				RtspRequest request = (RtspRequest) rtspMessage;
				switch ( request.getVerb() ) {
					case ANNOUNCE:
						onRequestAnnounce( proxyHandler, request );
						break;
					case DESCRIBE:
						onRequestDescribe( proxyHandler, request );
						break;
					case GET_PARAMETER:
						onRequestGetParam( proxyHandler, request );
						break;
					case OPTIONS:
						onRequestOptions( proxyHandler, request );
						break;
					case PAUSE:
						onRequestPause( proxyHandler, request );
						break;
					case PLAY:
						onRequestPlay( proxyHandler, request );
						break;
					case RECORD:
						onRequestRecord( proxyHandler, request );
						break;
					case REDIRECT:
						onRequestRedirect( proxyHandler, request );
						break;
					case SET_PARAMETER:
						onRequestSetParam( proxyHandler, request );
						break;
					case SETUP:
						onRequestSetup( proxyHandler, request );
						break;
					case TEARDOWN:
						onRequestTeardown( proxyHandler, request );
					default:
						break;
				}
				break;

			case TypeResponse:
				RtspResponse response = (RtspResponse) rtspMessage;
				switch ( response.getRequestVerb() ) {
					case ANNOUNCE:
						onResponseAnnounce( proxyHandler, response );
						break;
					case DESCRIBE:
						onResponseDescribe( proxyHandler, response );
						break;
					case GET_PARAMETER:
						onResponseGetParam( proxyHandler, response );
						break;
					case OPTIONS:
						onResponseOptions( proxyHandler, response );
						break;
					case PAUSE:
						onResponsePause( proxyHandler, response );
						break;
					case PLAY:
						onResponsePlay( proxyHandler, response );
						break;
					case RECORD:
						onResponseRecord( proxyHandler, response );
						break;
					case REDIRECT:
						onResponseRedirect( proxyHandler, response );
						break;
					case SET_PARAMETER:
						onResponseSetParam( proxyHandler, response );
						break;
					case SETUP:
						onResponseSetup( proxyHandler, response );
						break;
					case TEARDOWN:
						onResponseTeardown( proxyHandler, response );
					default:
						break;
				}
				break;

			default:
				break;
		}
	}
}
