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
public class ClientSide extends ProtocolHandlerAdapter
{
	static Logger log = Logger.getLogger( ClientSide.class );

	@Override
	public void sessionCreated( ProtocolSession session )
	{
		log.info( "New connection from " + session.getRemoteAddress() );
		// Creates a new ProxySession
		ProxySession proxySession = new ProxySession( session );
		session.setAttribute( "proxySession", proxySession );
	}

	@Override
	public void sessionClosed( ProtocolSession session )
	{
		log.info("Client connection closed");
		ProxySession proxySession = (ProxySession) ( session.getAttribute( "proxySession" ) );
		proxySession.closeAll();
	}

	@Override
	public void exceptionCaught( ProtocolSession session, Throwable cause )
			throws Exception
	{
		// close all: same as sessionClosed()
		log.info( "Exception:" + cause.getMessage() );
		sessionClosed( session );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestAnnounce(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestAnnounce( ProxySession proxySession, RtspRequest request )
	{
		log.debug( "REQUEST ANNOUNCE" );
		proxySession.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestDescribe(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestDescribe( ProxySession proxySession, RtspRequest request )
	{
		log.debug( "REQUEST DESCRIBE" );
		proxySession.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestGetParam(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestGetParam( ProxySession proxySession, RtspRequest request )
	{
		log.debug( "REQUEST GET_PARAMETER" );
		proxySession.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestOptions(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestOptions( ProxySession proxySession, RtspRequest request )
	{
		log.debug( "REQUEST OPTIONS" );
		proxySession.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestPause(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestPause( ProxySession proxySession, RtspRequest request )
	{
		log.debug( "REQUEST PAUSE" );
		proxySession.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestPlay(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestPlay( ProxySession proxySession, RtspRequest request )
	{
		log.debug( "REQUEST PLAY" );
		proxySession.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestRecord(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestRecord( ProxySession proxySession, RtspRequest request )
	{
		log.debug( "REQUEST RECORD" );
		proxySession.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestRedirect(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestRedirect( ProxySession proxySession, RtspRequest request )
	{
		log.debug( "REQUEST REDIRECT" );
		proxySession.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestSetParam(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestSetParam( ProxySession proxySession, RtspRequest request )
	{
		log.debug( "REQUEST SET_PARAMETER" );
		proxySession.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestSetup(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestSetup( ProxySession proxySession, RtspRequest request )
	{
		log.debug( "REQUEST SETUP" );
		proxySession.passSetupRequestToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestTeardown(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestTeardown( ProxySession proxySession, RtspRequest request )
	{
		log.debug( "REQUEST TEARDOWN" );
		proxySession.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponseAnnounce(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponseAnnounce( ProxySession proxySession, RtspResponse response )
	{
		log.debug("RESPONSE ANNOUNCE");
		proxySession.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponseDescribe(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponseDescribe( ProxySession proxySession, RtspResponse response )
	{
		log.debug("RESPONSE DESCRIBE");
		proxySession.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponseGetParam(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponseGetParam( ProxySession proxySession, RtspResponse response )
	{
		proxySession.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponseOptions(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponseOptions( ProxySession proxySession, RtspResponse response )
	{
		proxySession.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponsePause(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponsePause( ProxySession proxySession, RtspResponse response )
	{
		proxySession.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponsePlay(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponsePlay( ProxySession proxySession, RtspResponse response )
	{
		proxySession.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponseRecord(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponseRecord( ProxySession proxySession, RtspResponse response )
	{
		proxySession.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponseRedirect(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponseRedirect( ProxySession proxySession, RtspResponse response )
	{
		proxySession.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponseSetParam(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponseSetParam( ProxySession proxySession, RtspResponse response )
	{
		proxySession.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponseSetup(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponseSetup( ProxySession proxySession, RtspResponse response )
	{
		proxySession.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponseTeardown(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponseTeardown( ProxySession proxySession, RtspResponse response )
	{
		proxySession.passToServer( response );
	}

	@Override
	public void messageReceived( ProtocolSession session, Object message )
	{
		RtspMessage rtspMessage = (RtspMessage) message;
		log.debug( "Received message:\n" + message );

		ProxySession proxySession = (ProxySession) ( session.getAttribute( "proxySession" ) );

		switch ( rtspMessage.getType() ) {
			case TypeRequest:
				RtspRequest request = (RtspRequest) rtspMessage;
				switch ( request.getVerb() ) {
					case ANNOUNCE:
						onRequestAnnounce( proxySession, request );
						break;
					case DESCRIBE:
						onRequestDescribe( proxySession, request );
						break;
					case GET_PARAMETER:
						onRequestGetParam( proxySession, request );
						break;
					case OPTIONS:
						onRequestOptions( proxySession, request );
						break;
					case PAUSE:
						onRequestPause( proxySession, request );
						break;
					case PLAY:
						onRequestPlay( proxySession, request );
						break;
					case RECORD:
						onRequestRecord( proxySession, request );
						break;
					case REDIRECT:
						onRequestRedirect( proxySession, request );
						break;
					case SET_PARAMETER:
						onRequestSetParam( proxySession, request );
						break;
					case SETUP:
						onRequestSetup( proxySession, request );
						break;
					case TEARDOWN:
						onRequestTeardown( proxySession, request );
					default:
						break;
				}
				break;

			case TypeResponse:
				RtspResponse response = (RtspResponse) rtspMessage;
				switch ( response.getRequestVerb() ) {
					case ANNOUNCE:
						onResponseAnnounce( proxySession, response );
						break;
					case DESCRIBE:
						onResponseDescribe( proxySession, response );
						break;
					case GET_PARAMETER:
						onResponseGetParam( proxySession, response );
						break;
					case OPTIONS:
						onResponseOptions( proxySession, response );
						break;
					case PAUSE:
						onResponsePause( proxySession, response );
						break;
					case PLAY:
						onResponsePlay( proxySession, response );
						break;
					case RECORD:
						onResponseRecord( proxySession, response );
						break;
					case REDIRECT:
						onResponseRedirect( proxySession, response );
						break;
					case SET_PARAMETER:
						onResponseSetParam( proxySession, response );
						break;
					case SETUP:
						onResponseSetup( proxySession, response );
						break;
					case TEARDOWN:
						onResponseTeardown( proxySession, response );
					default:
						break;
				}
				break;

			default:
				break;
		}
	}

}
