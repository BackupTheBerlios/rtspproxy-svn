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

package rtspproxy.rtsp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.CharBuffer;

import rtspproxy.lib.Debug;
import rtspproxy.proxy.ProxySide;

/**
 * @author mat
 * 
 */
public class RtspProtocol
{

	/** The default port defined by the RTSP RFC. */
	public final static int DefaultRtspPort = 554;

	/**
	 * Keep the internal state of the RTSP data parsed.
	 * 
	 */
	public enum ReadState {
		/** Unrecoverable error occurred */
		Failed,
		/** Trying to resync */
		Sync,
		/** Waiting for a command */
		Ready,
		/** Reading interleaved packet */
		Packet,
		/** Reading command (request or command line */
		Command,
		/** Reading headers */
		Header,
		/** Reading body (entity) */
		Body,
		/** Fully formed message */
		Dispatch
	}

	private int cseqToSend;
	private StringBuffer buffer;
	private ReadState state;
	private RtspMessage rtspMessage;
	private RtspRequest.Verb lastRequestType;

	private Socket socket;
	private BufferedReader in = null;
	private PrintWriter out = null;

	private ProxySide parent;

	/**
	 * Constructor
	 */
	public RtspProtocol( ProxySide parent, Socket socket )
	{
		cseqToSend = 0;
		this.socket = socket;
		rtspMessage = null;
		state = ReadState.Command;
		lastRequestType = RtspRequest.Verb.None;
		this.parent = parent;
		if ( socket != null ) {
			try {
				in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
				out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(
						socket.getOutputStream() ) ), true );
			} catch ( IOException e ) {
				Debug.write( "Error opening socket streams" );
				socket = null;
			}
		}
	}

	public void setCommonHeaders( RtspMessage message )
	{
		String proxy = "RTSP Proxy v3.0 alpha " + "(" + System.getProperty( "os.name" )
				+ " " + System.getProperty( "os.arch" ) + ")";
		if ( message.getHeader( "Server" ) != null )
			message.setHeader( "Via", proxy );
		else
			message.setHeader( "Server", proxy );
	}

	private int getNextCSeq()
	{
		return ++cseqToSend;
	}

	public void sendRequest( RtspRequest request ) throws UnknownHostException,
			IOException
	{
		if ( socket == null ) {
			// The socket is not connected
			String host = request.getUrl().getHost();
			int port = request.getUrl().getPort();
			// if the port is not specified in the url
			// pick the default rtsp port
			port = ( port != -1 ) ? port : DefaultRtspPort;
			InetAddress addr = InetAddress.getByName( host );

			socket = new Socket( addr, port );
			in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
			out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(
					socket.getOutputStream() ) ), true );
		}

		setCommonHeaders( request );
		String cseq = request.getHeader( "CSeq" );
		if ( cseq == null ) {
			// CSeq is not set
			request.setHeader( "CSeq", Integer.toString( getNextCSeq() ) );
		}

		Debug.write( "SENDING REQUEST" );
		Debug.write( request.toString() );
		Debug.write( "END REQUEST" );

		out.write( request.toString() );
		out.flush();
		lastRequestType = request.getVerb();
	}

	public void sendResponse( RtspResponse response ) throws UnknownHostException
	{
		if ( socket == null ) {
			// Socket is not connected and we don't have an hostname
			// to connect for..
			Debug.write( "Cannot forward RtspResponse to server: socket not connected." );
			throw new UnknownHostException();
		}
		setCommonHeaders( response );
		String cseq = response.getHeader( "CSeq" );
		if ( cseq == null ) {
			// CSeq is not set
			response.setHeader( "CSeq", Integer.toString( getNextCSeq() ) );
		}

		Debug.write( "SENDING RESPONSE" );
		Debug.write( response.toString() );
		Debug.write( "END RESPONSE" );

		out.write( response.toString() );
		out.flush();
	}

	public void sendError( RtspCode errorCode )
	{
		// build the message
		RtspResponse response = new RtspResponse();
		response.setCode( errorCode );
		response.setHeader( "CSeq", Integer.toString( cseqToSend ) );
		setCommonHeaders( response );

		// send it
		out.write( response.toString() );
		out.flush();
	}

	/**
	 * Do the parsing on the incoming stream. If the stream does not contain the
	 * entire RTSP message wait for other data to arrive, before dispatching the
	 * message.
	 */
	private void parse() throws IOException
	{
		// qDebug() << "BUFFER(" << buffer << ")";
		// String data = in.read();

		String line;
		
		switch ( state ) {

			case Command:
				line = in.readLine();
				if ( line.startsWith( "RTSP" ) ) {
					// this is a RTSP response
					RtspCode code = RtspCode.fromString( line.split( " " )[1] );
					rtspMessage = new RtspResponse();
					( (RtspResponse) ( rtspMessage ) ).setCode( code );
					// qDebug() << "Request - code:" << code;
				} else {
					// this is a RTSP request
					String verb = line.split( " " )[0];
					URL url;
					try {
						url = new URL( line.split( " " )[1] );
					} catch ( MalformedURLException e ) {
						url = null;
						state = ReadState.Failed;
						return;
					}
					rtspMessage = new RtspRequest();
					( (RtspRequest) ( rtspMessage ) ).setVerb( verb );
					( (RtspRequest) ( rtspMessage ) ).setUrl( url );
					// qDebug() << "Request - verb:" << verb << "url:" << url;
				}
				state = ReadState.Header;
				break;

			case Header:
				// this is an header
				line = in.readLine();
				if ( line.length() == 0 ) {
					// This is the empty line that marks the end
					// of the headers section
					state = ReadState.Body;
					return;
				}

				String key = line.split( ": " )[0];
				String value = line.split( ": " )[1];
				rtspMessage.setHeader( key, value );
				// qDebug() << "Header - key:" << key << "value:" << value;
				break;

			case Body:

				int bufferLen = Integer.valueOf( rtspMessage.getHeader( "Content-Length" ) );
				if ( bufferLen == 0 ) {
					// there's no buffer to be read
					state = ReadState.Dispatch;
					return;
				}

				int bytesToRead = bufferLen - rtspMessage.getBufferSize();

				// read the content buffer
				CharBuffer buffer = CharBuffer.allocate( bytesToRead );
				int res = in.read( buffer.array() );

				if ( res == 0 )
					return;

				rtspMessage.appendToBuffer( new StringBuffer( buffer ) );
				if ( rtspMessage.getBufferSize() >= bufferLen ) {
					// The RTSP message parsing is completed
					state = ReadState.Dispatch;
				}
				break;
		}
	}
}
