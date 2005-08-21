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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.mina.io.socket.SocketConnector;
import org.apache.mina.protocol.ProtocolSession;
import org.apache.mina.protocol.io.IoProtocolConnector;

import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;
import rtspproxy.rtsp.RtspSession;
import rtspproxy.rtsp.RtspTransport;
import rtspproxy.rtsp.RtspTransportList;

/**
 * @author mat
 * 
 */
public class ProxyHandler
{

	private ProtocolSession clientSession = null;
	private ProtocolSession serverSession = null;

	static Logger log = Logger.getLogger( ProxyHandler.class );

	/**
	 * Creates a new ProxyHandler from a client side protocol session.
	 * 
	 * @param clientSession
	 */
	public ProxyHandler( ProtocolSession clientSession )
	{
		this.clientSession = clientSession;
	}

	public void passToServer( RtspMessage message )
	{
		log.debug( "Pass to server" );
		if ( serverSession == null && message.getType() == RtspMessage.Type.TypeResponse ) {
			log.error( "We can't send a response message to an uninitialized serverSide" );
			return;
		} else if ( serverSession == null ) {
			RtspRequest request = (RtspRequest) message;
			try {
				connectServerSide( request.getUrl() );

			} catch ( IOException e ) {
				log.error( e );
				closeAll();
				return;
			}
		}

		switch ( message.getType() ) {
			case TypeRequest:
				sendRequest( serverSession, (RtspRequest) message );
				break;

			case TypeResponse:
				sendResponse( serverSession, (RtspResponse) message );
				break;

			default:
				log.error( "Message type not valid: " + message.getType() );
		}
	}

	public void passToClient( RtspMessage message )
	{
		log.debug( "Pass to client" );
		switch ( message.getType() ) {
			case TypeRequest:
				sendRequest( clientSession, (RtspRequest) message );
				break;

			case TypeResponse:
				sendResponse( clientSession, (RtspResponse) message );
				break;

			default:
				log.error( "Message type not valid: " + message.getType() );
		}
	}

	// Special cases
	public void passSetupRequestToServer( RtspRequest request )
	{
		log.debug( "Client Transport:" + request.getHeader( "Transport" ) );
		RtspTransportList rtspTransportList = new RtspTransportList(
				request.getHeader( "Transport" ) );
		log.debug( "Parsed:" + rtspTransportList.toString() );

		for ( RtspTransport t : rtspTransportList.getList() ) {
			log.debug( "Transport:" + t );
			if ( t.getLowerTransport() == RtspTransport.LowerTransport.TCP )
				log.debug( "Transport is TCP based." );
			int clientPort[] = t.getClientPort();
			log.debug( "Client port:" + clientPort[0] + "-" + clientPort[1] );
		}

		sendRequest( serverSession, request );
	}

	/**
	 * Forward a RTSP SETUP response message to client.
	 * @param response Setup response message
	 */
	public void passSetupResponseToClient( RtspResponse response )
	{
		// If there isn't yet an rtspSession, create a new one
		RtspSession rtspSession = RtspSession.get( response.getHeader( "Session" ) );
		if ( rtspSession == null ) {
			rtspSession = (RtspSession) clientSession.getAttribute( "rtspSession" );
			if ( rtspSession == null ) {
				rtspSession = RtspSession.create();
			}
		}
		sendResponse( clientSession, response );
	}

	/**
	 * Tries to connect to remote RTSP server.
	 * @param url the URI of the server
	 * @throws IOException
	 */
	private void connectServerSide( URL url ) throws IOException
	{
		String host = url.getHost();
		int port = url.getPort();
		if ( port == -1 )
			port = 554;

		log.debug( "Connecting to '" + host + "' " + port );

		// Create TCP/IP connector.
		SocketConnector socketConnector = new SocketConnector();
		IoProtocolConnector connector = new IoProtocolConnector( socketConnector );

		ServerSideProvider serverSideProvider = new ServerSideProvider();

		log.debug( "Created new serverSideProvider" );

		// Start communication.
		log.debug( "Trying to connect." );
		serverSession = connector.connect( new InetSocketAddress( host, port ),
				serverSideProvider );

		log.debug( "Connected!" );

		// Save current ProxyHandler into the ProtocolSession
		serverSession.setAttribute( "proxySession", this );

		System.out.println( "Server session: " + serverSession.getAttributeKeys() );
	}

	public void closeAll()
	{
		if ( clientSession != null && clientSession.isConnected() )
			clientSession.close();
		if ( serverSession != null && serverSession.isConnected() )
			serverSession.close();
	}

	private void sendRequest( ProtocolSession session, RtspRequest request )
	{
		request.setCommonHeaders();
		try {
			clientSession.setAttribute( "lastRequestVerb", request.getVerb() );
			if ( serverSession != null ) {
				serverSession.setAttribute( "lastRequestVerb", request.getVerb() );
			}
			session.write( request );
		} catch ( Exception e ) {
			log.error( e.getCause() );
		}
	}

	private void sendResponse( ProtocolSession session, RtspResponse response )
	{
		response.setCommonHeaders();
		try {
			session.write( response );
		} catch ( Exception e ) {
			log.error( e.getCause() );
		}
	}

}
