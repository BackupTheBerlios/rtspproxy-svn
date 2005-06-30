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

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.protocol.ProtocolDecoder;
import org.apache.mina.protocol.ProtocolDecoderOutput;
import org.apache.mina.protocol.ProtocolSession;
import org.apache.mina.protocol.ProtocolViolationException;

/**
 * 
 */
public class RtspDecoder implements ProtocolDecoder
{

	/** 
	 * State enumerator that indicates the reached state in the RTSP
	 * message decoding process.  
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
		/** Reading command (request or command line) */
		Command,
		/** Reading headers */
		Header,
		/** Reading body (entity) */
		Body,
		/** Fully formed message */
		Dispatch
	}

	static Logger log = Logger.getLogger( RtspDecoder.class );

	/** 
	 * Get a line from a string buffer and delete the line in the buffer.
	 * @param buffer
	 * @return
	 */
	private static String getLine( StringBuffer buffer )
	{
		int idx = buffer.indexOf( "\r\n" );
		if ( idx == -1 ) {
			return null;
		} else {
			// Return the line string (without CRLF)
			String s = buffer.substring( 0, idx );
			buffer.delete( 0, idx + 2 );
			return s;
		}
	}

	/**
	 * Do the parsing on the incoming stream. If the stream does not contain the
	 * entire RTSP message wait for other data to arrive, before dispatching the
	 * message.
	 * 
	 * @see org.apache.mina.protocol.ProtocolDecoder#decode(org.apache.mina.protocol.ProtocolSession,
	 *      org.apache.mina.common.ByteBuffer,
	 *      org.apache.mina.protocol.ProtocolDecoderOutput)
	 */
	public void decode( ProtocolSession session, ByteBuffer buffer,
			ProtocolDecoderOutput out ) throws ProtocolViolationException
	{
		StringBuffer decodeBuf = new StringBuffer();

		do {
			decodeBuf.append( (char) buffer.get() );
		} while ( buffer.hasRemaining() );

		// Retrieve status from session
		ReadState state = (ReadState) session.getAttribute( "state" );
		if ( state == null )
			state = ReadState.Command;
		RtspMessage rtspMessage = (RtspMessage) session.getAttribute( "rtspMessage" );

		while ( true ) {

			if ( state != ReadState.Command && state != ReadState.Header )
				// the "while" loop is only used to read commands and headers
				break;

			String line = getLine( decodeBuf );
			if ( line == null )
				// there's no more data in the buffer
				break;

			if ( line.length() == 0 ) {
				// This is the empty line that marks the end
				// of the headers section
				state = ReadState.Body;
				break;
			}

			switch ( state ) {

				case Command:
					// log.debug( "Command line: " + line );
					if ( line.startsWith( "RTSP" ) ) {
						// this is a RTSP response
						RtspCode code = RtspCode.fromString( line.split( " " )[1] );
						rtspMessage = new RtspResponse();
						( (RtspResponse) ( rtspMessage ) ).setCode( code );
						RtspRequest.Verb verb = (RtspRequest.Verb) session.getAttribute( "lastRequestVerb" );
						( (RtspResponse) ( rtspMessage ) ).setRequestVerb( verb );

					} else {
						// this is a RTSP request
						String verb = line.split( " " )[0];
						URI url;
						try {
							// log.debug( "url line: " + line.split( " " )[1] );
							url = new URI( line.split( " " )[1] );
						} catch ( URISyntaxException e ) {
							log.info( e );
							url = null;
							state = ReadState.Failed;
							break;
						}
						rtspMessage = new RtspRequest();
						( (RtspRequest) rtspMessage ).setVerb( verb );
						( (RtspRequest) rtspMessage ).setUrl( url );
					}
					state = ReadState.Header;
					break;

				case Header:
					// this is an header
					// log.debug( "Line: " + line );

					String[] a = line.split( ": ", 2 );
					if ( a.length == 2 ) {
						String key = a[0];
						String value = a[1];
						rtspMessage.setHeader( key, value );
					} else if ( a.length == 1 ) {
						String key = a[0];
						rtspMessage.setHeader( key, "" );
					} else {
						log.debug( "Malformed header: " + line );
					}
					break;

			}
		}

		if ( state == ReadState.Body ) {
			// Read the message body
			int bufferLen = Integer.valueOf( rtspMessage.getHeader( "Content-Length", "0" ) );
			if ( bufferLen == 0 ) {
				// there's no buffer to be read
				state = ReadState.Dispatch;

			} else {
				// we have a content buffer to read
				int bytesToRead = bufferLen - rtspMessage.getBufferSize();
				
				if ( bytesToRead < decodeBuf.length() ) {
					log.warn( "We are reading more bytes than Content-Length." );
				}

				// read the content buffer
				rtspMessage.appendToBuffer( decodeBuf );
				if ( rtspMessage.getBufferSize() >= bufferLen ) {
					// The RTSP message parsing is completed
					state = ReadState.Dispatch;
				}
			}
		}

		if ( state == ReadState.Dispatch ) {
			// The message is already formed
			// send it
			session.removeAttribute( "state" );
			session.removeAttribute( "rtspMessage" );
			out.write( rtspMessage );
			return;
		}

		// log.debug( "INCOMPLETE MESSAGE \n" + rtspMessage );

		// Save attributes in session
		session.setAttribute( "state", state );
		session.setAttribute( "rtspMessage", rtspMessage );
	}
}
