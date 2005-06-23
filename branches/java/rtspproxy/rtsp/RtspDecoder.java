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
 * @author mat
 * 
 */
public class RtspDecoder implements ProtocolDecoder
{

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

	private ReadState state;
	private RtspMessage rtspMessage;
	private StringBuffer decodeBuf;

	public RtspDecoder()
	{
		state = ReadState.Command;
		rtspMessage = null;
		decodeBuf = new StringBuffer();
	}

	/*
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
		// qDebug() << "BUFFER(" << buffer << ")";
		// log.debug( "decode" );
		do {
			decodeBuf.append( (char) buffer.get() );
		} while ( buffer.hasRemaining() );

		String str = decodeBuf.toString();
		String[] list = str.split( "\r\n" );
		if ( list.length == 0 ) {
			// If we only have an empty line, reports it
			list = new String[] { "" };
		}

		for ( String line : list ) {

			switch ( state ) {

				case Command:
					log.debug( "Command line: " + line );
					if ( line.startsWith( "RTSP" ) ) {
						// this is a RTSP response
						RtspCode code = RtspCode.fromString( line.split( " " )[1] );
						rtspMessage = new RtspResponse();
						( (RtspResponse) ( rtspMessage ) ).setCode( code );
					} else {
						// this is a RTSP request
						String verb = line.split( " " )[0];
						URI url;
						try {
							log.debug( "url line: " + line.split( " " )[1] );
							url = new URI( line.split( " " )[1] );
						} catch ( URISyntaxException e ) {
							log.info( e );
							url = null;
							state = ReadState.Failed;
							return;
						}
						rtspMessage = new RtspRequest();
						( (RtspRequest) rtspMessage ).setVerb( verb );
						( (RtspRequest) rtspMessage ).setUrl( url );
					}
					state = ReadState.Header;
					break;

				case Header:
					// this is an header
					log.debug( "Line: " + line );
					if ( line.length() == 0 ) {
						// This is the empty line that marks the end
						// of the headers section
						int bufferLen = Integer.valueOf( rtspMessage.getHeader( "Content-Length", "0" ) );
						if ( bufferLen == 0 )
							
							// there's no buffer to be read
							state = ReadState.Dispatch;
						else
							state = ReadState.Body;
						
						break;
					}

					String key = line.split( ": " )[0];
					String value = line.split( ": " )[1];
					rtspMessage.setHeader( key, value );
					break;

				case Body:

					int bufferLen = Integer.valueOf( rtspMessage.getHeader( "Content-Length", "0" ) );
					if ( bufferLen == 0 ) {
						// there's no buffer to be read
						state = ReadState.Dispatch;
						break;
					}

					int bytesToRead = bufferLen - rtspMessage.getBufferSize();

					// read the content buffer
					rtspMessage.appendToBuffer( new StringBuffer( line + "\r\n" ) );
					if ( rtspMessage.getBufferSize() >= bufferLen ) {
						// The RTSP message parsing is completed
						state = ReadState.Dispatch;
					}
					break;
			}
		}

		if ( state == ReadState.Dispatch ) {
			// The message is already formed
			// send it
			log.debug( "Message parsing completed." );
			log.debug( rtspMessage.toString() );
			out.write( rtspMessage );
			// dispatchMessage();
		}

		decodeBuf.delete( 0, decodeBuf.length() );
	}
}
