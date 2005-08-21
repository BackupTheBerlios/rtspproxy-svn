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

package server;

import org.apache.log4j.Logger;
import org.apache.mina.protocol.ProtocolHandlerAdapter;
import org.apache.mina.protocol.ProtocolSession;

import rtspproxy.rtsp.RtspCode;
import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;

/**
 * RTSP Protocol Handler for the server.
 */
public class Server extends ProtocolHandlerAdapter
{

	static Logger log = Logger.getLogger( Server.class );

	private static String supportedRtspMethods = "DESCRIBE, SETUP, TEARDOWN, PLAY, PAUSE";

	/**
	 * Called when a client connects.
	 */
	@Override
	public void sessionCreated( ProtocolSession session )
	{
		log.info( "New connection from " + session.getRemoteAddress() );
	}

	/**
	 * Called when a client connection is closed.
	 */
	@Override
	public void sessionClosed( ProtocolSession session )
	{
		log.info( "Client connection closed" );
	}

	/**
	 * Called when a exception is raised.
	 */
	@Override
	public void exceptionCaught( ProtocolSession session, Throwable cause )
			throws Exception
	{
		log.info( "Exception: " + cause );
		if ( log.isDebugEnabled() ) {
			cause.printStackTrace();
		}
		sendError( session, RtspCode.BadRequest );
		session.close();
	}

	/**
	 * Called whenever a message is received.
	 */
	@Override
	public void messageReceived( ProtocolSession session, Object message )
			throws Exception
	{
		log.debug( "Message received: \n>>>>>>>\n" + message + "<<<<<<" );
		RtspMessage rtspMessage = (RtspMessage) message;

		switch ( rtspMessage.getType() ) {
			case TypeRequest:
				RtspRequest request = (RtspRequest) rtspMessage;

				if ( request.getHeader( "CSeq" ) != null ) {
					// If present, i save the CSeq value for using it
					// in the response message
					session.setAttribute( "CSeq", request.getHeader( "CSeq" ) );
				}

				switch ( request.getVerb() ) {
					case ANNOUNCE:
						onRequestAnnounce( session, request );
						break;
					case DESCRIBE:
						onRequestDescribe( session, request );
						break;
					case GET_PARAMETER:
						onRequestGetParam( session, request );
						break;
					case OPTIONS:
						onRequestOptions( session, request );
						break;
					case PAUSE:
						onRequestPause( session, request );
						break;
					case PLAY:
						onRequestPlay( session, request );
						break;
					case RECORD:
						onRequestRecord( session, request );
						break;
					case REDIRECT:
						onRequestRedirect( session, request );
						break;
					case SET_PARAMETER:
						onRequestSetParam( session, request );
						break;
					case SETUP:
						onRequestSetup( session, request );
						break;
					case TEARDOWN:
						onRequestTeardown( session, request );
					default:
						break;
				}
				break;

			case TypeResponse:
				// We do no send RTSP request and so we will not
				// accept RTSP responses
				sendError( session, RtspCode.NotAcceptable );
				break;

			default:
				break;
		}
	}

	/**
	 * Called when a message is sent to the client
	 */
	@Override
	public void messageSent( ProtocolSession session, Object message ) throws Exception
	{
		log.debug( "Message Sent" );
		super.messageSent( session, message );
	}

	/**
	 * Called when a new session is opened.
	 */
	@Override
	public void sessionOpened( ProtocolSession session ) throws Exception
	{
		log.debug( "Session Opened" );
		super.sessionOpened( session );
	}

	/**
	 * Sends a message to the client.
	 * 
	 * @param session
	 *        the ProtocolSession reference
	 * @param message
	 *        RTSP message to be sent
	 */
	public void sendMessage( ProtocolSession session, RtspMessage message )
	{
		String serverHeader = "RTSP Streaming Server v3.0 alpha " + "("
				+ System.getProperty( "os.name" ) + " " + System.getProperty( "os.arch" )
				+ ")";
		message.setHeader( "Server", serverHeader );

		// Get a CSeq if any
		if ( message.getHeader( "CSeq" ) == null
				&& session.getAttribute( "CSeq" ) != null )
			message.setHeader( "CSeq", (String) session.getAttribute( "CSeq" ) );

		try {
			session.write( message );
		} catch ( Exception e ) {
			log.error( e.getCause() );
		}
	}

	/**
	 * Sends an error message to the client.
	 * 
	 * @param session
	 *        the ProtocolSession reference
	 * @param code
	 *        RTSP error code
	 */
	public void sendError( ProtocolSession session, RtspCode code )
	{
		RtspResponse response = new RtspResponse();
		response.setCode( code );
		sendMessage( session, response );
	}

	public void onRequestAnnounce( ProtocolSession session, RtspRequest request )
	{
		log.debug( "REQUEST ANNOUNCE" );
	}

	public void onRequestDescribe( ProtocolSession session, RtspRequest request )
	{
		log.debug( "REQUEST DESCRIBE" );
		MediaObject.resolveURL( request.getUrl() );
		RtspResponse response = new RtspResponse();
		response.setCode( RtspCode.NotFound );
		// response.setH
	}

	public void onRequestGetParam( ProtocolSession session, RtspRequest request )
	{
		log.debug( "REQUEST GET_PARAMETER" );
	}

	/**
	 * Handles the OPTIONS RTSP method. Reply with the list of supported RTSP
	 * methods.
	 * 
	 * @param session
	 *        the current session
	 * @param request
	 *        the request message
	 */
	public void onRequestOptions( ProtocolSession session, RtspRequest request )
	{
		log.debug( "REQUEST OPTIONS" );
		RtspResponse response = new RtspResponse();
		response.setHeader( "Public", supportedRtspMethods );
		sendMessage( session, response );
	}

	public void onRequestPause( ProtocolSession session, RtspRequest request )
	{
		log.debug( "REQUEST PAUSE" );
	}

	public void onRequestPlay( ProtocolSession session, RtspRequest request )
	{
		log.debug( "REQUEST PLAY" );
	}

	public void onRequestRecord( ProtocolSession session, RtspRequest request )
	{
		log.debug( "REQUEST RECORD" );
	}

	public void onRequestRedirect( ProtocolSession session, RtspRequest request )
	{
		log.debug( "REQUEST REDIRECT" );
	}

	public void onRequestSetParam( ProtocolSession session, RtspRequest request )
	{
		log.debug( "REQUEST SET_PARAMETER" );
	}

	public void onRequestSetup( ProtocolSession session, RtspRequest request )
	{
		log.debug( "REQUEST SETUP" );
	}

	public void onRequestTeardown( ProtocolSession session, RtspRequest request )
	{
		log.debug( "REQUEST TEARDOWN" );
	}

}
