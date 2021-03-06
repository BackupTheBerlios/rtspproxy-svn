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
import org.apache.mina.common.IoFilter;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import rtspproxy.rtsp.RtspCode;
import rtspproxy.rtsp.RtspDecoder;
import rtspproxy.rtsp.RtspEncoder;
import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;

/**
 * @author mat
 */
public class ClientSide extends IoHandlerAdapter
{

	static Logger log = Logger.getLogger( ClientSide.class );

	private static ProtocolCodecFactory codecFactory = new ProtocolCodecFactory()
	{

		public ProtocolEncoder newEncoder()
		{
			// Create a new encoder.
			return new RtspEncoder();
		}

		public ProtocolDecoder newDecoder()
		{
			// Create a new decoder.
			return new RtspDecoder();
		}
	};

	private static IoFilter codecFilter = new ProtocolCodecFilter( codecFactory );

	@Override
	public void sessionCreated( IoSession session ) throws Exception
	{
		session.getFilterChain().addLast( "codec", codecFilter );
		log.info( "New connection from " + session.getRemoteAddress() );
		// Creates a new ProxyHandler
		ProxyHandler proxyHandler = new ProxyHandler( session );
		session.setAttribute( "proxyHandler", proxyHandler );
	}

	@Override
	public void sessionClosed( IoSession session )
	{
		log.info( "Client connection closed" );
		ProxyHandler proxyHandler = (ProxyHandler) ( session.getAttribute( "proxyHandler" ) );
		proxyHandler.closeAll();
	}

	@Override
	public void exceptionCaught( IoSession session, Throwable cause ) throws Exception
	{
		// close all: same as sessionClosed()
		log.info( "Exception: " + cause );
		sessionClosed( session );
	}

	public void onRequestAnnounce( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST ANNOUNCE" );
		proxyHandler.passToServer( request );
	}

	public void onRequestDescribe( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST DESCRIBE" );
		proxyHandler.passToServer( request );
	}

	public void onRequestGetParam( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST GET_PARAMETER" );
		proxyHandler.passToServer( request );
	}

	public void onRequestOptions( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST OPTIONS" );
		if ( request.getUrl() == null ) {
			// There isn't a server URL, so with reply with
			// a standard set of Proxy supported Options
			RtspResponse response = new RtspResponse();
			response.setCode( RtspCode.OK );
			response.setHeader( "Public",
					"DESCRIBE, SETUP, TEARDOWN, PLAY, PAUSE, SET_PARAMETER" );
			proxyHandler.passToClient( response );
			return;
		}
		proxyHandler.passToServer( request );
	}

	public void onRequestPause( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST PAUSE" );
		proxyHandler.passToServer( request );
	}

	public void onRequestPlay( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST PLAY" );
		proxyHandler.passToServer( request );
	}

	public void onRequestRecord( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST RECORD" );
		proxyHandler.passToServer( request );
	}

	public void onRequestRedirect( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST REDIRECT" );
		proxyHandler.passToServer( request );
	}

	public void onRequestSetParam( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST SET_PARAMETER" );
		proxyHandler.passToServer( request );
	}

	public void onRequestSetup( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST SETUP" );
		proxyHandler.passSetupRequestToServer( request );
	}

	public void onRequestTeardown( ProxyHandler proxyHandler, RtspRequest request )
	{
		log.debug( "REQUEST TEARDOWN" );
		proxyHandler.passToServer( request );
	}

	public void onResponseAnnounce( ProxyHandler proxyHandler, RtspResponse response )
	{
		log.debug( "RESPONSE ANNOUNCE" );
		proxyHandler.passToServer( response );
	}

	public void onResponseDescribe( ProxyHandler proxyHandler, RtspResponse response )
	{
		log.debug( "RESPONSE DESCRIBE" );
		proxyHandler.passToServer( response );
	}

	public void onResponseGetParam( ProxyHandler proxyHandler, RtspResponse response )
	{
		log.debug( "RESPONSE GET_PARAM" );
		proxyHandler.passToServer( response );
	}

	public void onResponseOptions( ProxyHandler proxyHandler, RtspResponse response )
	{
		log.debug( "RESPONSE OPTIONS" );
		proxyHandler.passToServer( response );
	}

	public void onResponsePause( ProxyHandler proxyHandler, RtspResponse response )
	{
		log.debug( "RESPONSE PAUSE" );
		proxyHandler.passToServer( response );
	}

	public void onResponsePlay( ProxyHandler proxyHandler, RtspResponse response )
	{
		log.debug( "RESPONSE PLAY" );
		proxyHandler.passToServer( response );
	}

	public void onResponseRecord( ProxyHandler proxyHandler, RtspResponse response )
	{
		log.debug( "RESPONSE RECORD" );
		proxyHandler.passToServer( response );
	}

	public void onResponseRedirect( ProxyHandler proxyHandler, RtspResponse response )
	{
		log.debug( "RESPONSE REDIRECT" );
		proxyHandler.passToServer( response );
	}

	public void onResponseSetParam( ProxyHandler proxyHandler, RtspResponse response )
	{
		log.debug( "RESPONSE SET_PARAM" );
		proxyHandler.passToServer( response );
	}

	public void onResponseSetup( ProxyHandler proxyHandler, RtspResponse response )
	{
		log.debug( "RESPONSE SEUP" );
		proxyHandler.passToServer( response );
	}

	public void onResponseTeardown( ProxyHandler proxyHandler, RtspResponse response )
	{
		log.debug( "RESPONSE TEARDOWN" );
		proxyHandler.passToServer( response );
	}

	@Override
	public void messageReceived( IoSession session, Object message )
	{
		RtspMessage rtspMessage = (RtspMessage) message;
		log.debug( "Received message:\n" + message );

		ProxyHandler proxyHandler = (ProxyHandler) ( session.getAttribute( "proxyHandler" ) );

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
