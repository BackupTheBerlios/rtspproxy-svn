/*************************************************************************************************************
 * * This program is free software; you can redistribute it and/or modify * it under the terms of the GNU
 * General Public License as published by * the Free Software Foundation; either version 2 of the License, or *
 * (at your option) any later version. * * Copyright (C) 2005 - Matteo Merli - matteo.merli@gmail.com * *
 ************************************************************************************************************/

/*
 * $Id$ 
 * $URL$
 */

package rtspproxy.rtsp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import rtspproxy.config.Config;
import rtspproxy.lib.Exceptions;

/**
 * 
 */
public class RtspDecoder implements ProtocolDecoder
{

	private static final String readStateATTR = RtspDecoder.class.toString()
			+ "readState";
	private static final String rtspMessageATTR = RtspDecoder.class.toString()
			+ "rtspMessage";

	/**
	 * State enumerator that indicates the reached state in the RTSP message
	 * decoding process.
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

	private static Logger log = LoggerFactory.getLogger( RtspDecoder.class );

	private static final Pattern rtspRequestPattern = Pattern.compile( "([A-Z_]+) ([^ ]+) RTSP/1.0" );
	private static final Pattern rtspResponsePattern = Pattern.compile( "RTSP/1.0 ([0-9]+) .+" );
	private static final Pattern rtspHeaderPattern = Pattern.compile( "([a-zA-Z\\-]+[0-9]?):\\s?(.*)" );
	private static final Pattern spaceRtspHeaderPattern = Pattern.compile( "([a-zA-Z\\-]+[0-9]?)\\s?:\\s?(.*)" );

	private static final Charset asciiCharset = Charset.forName( "US-ASCII" );

	/**
	 * Do the parsing on the incoming stream. If the stream does not contain the
	 * entire RTSP message wait for other data to arrive, before dispatching the
	 * message.
	 * 
	 * @see org.apache.mina.protocol.ProtocolDecoder#decode(org.apache.mina.protocol.IoSession,
	 *      org.apache.mina.common.ByteBuffer,
	 *      org.apache.mina.protocol.ProtocolDecoderOutput)
	 */
	public void decode( IoSession session, ByteBuffer buffer, ProtocolDecoderOutput out )
			throws ProtocolDecoderException
	{
		BufferedReader reader = null;

		reader = new BufferedReader(new InputStreamReader( buffer.asInputStream(),
				asciiCharset ), 2048 );

		// Retrieve status from session
		ReadState state = (ReadState) session.getAttribute( readStateATTR );
		RtspMessage rtspMessage = (RtspMessage) session.getAttribute( rtspMessageATTR );
		log.debug("entered RTSP decode, state=" + state + ", rtsp message in session=" + rtspMessageATTR);
		
		try {

			while ( true ) {
				/*
				if ( state != ReadState.Command && state != ReadState.Header )
					// the "while" loop is only used to read commands and
					// headers
					break;
					*/

				reader.mark(2048);
				String line = reader.readLine();
				if ( line == null ) {
					// there's no more data in the buffer
					log.debug("seen end-of-message, leaving loop");
					break;					
				}
				
				if ( state == null ) {
					log.debug("switching null-state to Command");
					state = ReadState.Command;
				}

				if ( line.length() == 0 ) {
					// This is the empty line that marks the end
					// of the headers section
					if(rtspMessage != null) {
						log.debug("seen emtpy line, switching to Body");
						state = ReadState.Body;
						reader.mark(64);
					} else {
						log.debug("seen emtpy line, switching to Sync");
						state = ReadState.Sync;						
					}
					// break;
				}

				switch (state) {
				case Sync:
					log.debug("found empty line between command, switching to Command");
					state = ReadState.Command;
					break;
				case Command:
					log.debug("Command line: " + line);
					if (line.startsWith("RTSP")) {
						// this is a RTSP response
						Matcher m = rtspResponsePattern.matcher(line);
						if (!m.matches())
							throw new ProtocolDecoderException(
									"Malformed response line: " + line);

						RtspCode code = RtspCode.fromString(m.group(1));
						rtspMessage = new RtspResponse();
						((RtspResponse) (rtspMessage)).setCode(code);
						RtspRequest.Verb verb = (RtspRequest.Verb) session
								.getAttribute(RtspMessage.lastRequestVerbATTR);
						((RtspResponse) (rtspMessage)).setRequestVerb(verb);

					} else {
						// this is a RTSP request
						Matcher m = rtspRequestPattern.matcher(line);
						if (!m.matches())
							throw new ProtocolDecoderException(
									"Malformed request line: " + line);

						String verb = m.group(1);
						String strUrl = m.group(2);
						URL url = null;
						if (!strUrl.equalsIgnoreCase("*")) {
							try {
								url = new URL(strUrl);
							} catch (MalformedURLException e) {
								log.info("malformed URL: " + url, e);
								url = null;
								session.setAttribute(readStateATTR,
										ReadState.Failed);
								throw new ProtocolDecoderException(
										"Invalid URL");
							}
						}
						rtspMessage = new RtspRequest();
						((RtspRequest) rtspMessage).setVerb(verb);

						if (((RtspRequest) rtspMessage).getVerb() == RtspRequest.Verb.None) {
							session.setAttribute(readStateATTR,
									ReadState.Failed);
							throw new ProtocolDecoderException(
									"Invalid method: " + verb);
						}

						((RtspRequest) rtspMessage).setUrl(url);
					}
					state = ReadState.Header;
					log.debug("switching from Command to Header, message="
							+ rtspMessage);
					break;

				case Header:
					// this is an header
					log.debug("Header line: " + line);
					Matcher m = rtspHeaderPattern.matcher(line);

					if (!m.matches()) {
						if(Config.proxyRtspAllowBrokenHeaders.getValue()) {
							Matcher m2 = spaceRtspHeaderPattern.matcher(line);
							
							if(!m2.matches()) {
								throw new ProtocolDecoderException(
										"RTSP header not valid, line=" + line);								
							} else
								rtspMessage.setHeader(m2.group(1), m2.group(2));
						} else {
							throw new ProtocolDecoderException(
							"RTSP header not valid, line=" + line);
						}
					} else
						rtspMessage.setHeader(m.group(1), m.group(2));
					
					break;
				case Body:
					int bufferLen = Integer.parseInt(rtspMessage.getHeader(
							"Content-Length", "0"));

					if (bufferLen == 0) {
						log.debug("no message body found, switching to Dispatch");
						// there's no buffer to be read
						state = ReadState.Dispatch;

					} else {
						// we have a content buffer to read
						int bytesToRead = bufferLen
								- rtspMessage.getBufferSize();

						// if ( bytesToRead < reader. decodeBuf.length() ) {
						// log.warn( "We are reading more bytes than
						// Content-Length." );
						// }

						// read the content buffer
						CharBuffer bufferContent = CharBuffer
								.allocate(bytesToRead);
						reader.reset();
						reader.read(bufferContent);
						bufferContent.flip();
						rtspMessage.appendToBuffer(bufferContent);
						
						// this is an ugly hack to avoid content underruns produced by bogus servers
						if( rtspMessage.getBufferSize() == (bufferLen - 2))
							rtspMessage.appendToBuffer("\r\n");
						if( rtspMessage.getBufferSize() == (bufferLen - 1))
							rtspMessage.appendToBuffer("\n");
						
						// terminate message here
						if (rtspMessage.getBufferSize() >= bufferLen) {
							// The RTSP message parsing is completed
							state = ReadState.Dispatch;
						}
					}
					break;
				}
				if ( state == ReadState.Dispatch ) {
					log.debug("sending decoded RTSP message");
					// The message is already formed
					// send it
					session.removeAttribute( readStateATTR );
					session.removeAttribute( rtspMessageATTR );
					out.write( rtspMessage );
					
					state = null;
					rtspMessage = null;
				}

			}
		} catch ( IOException e ) {
			/*
			 * error on input stream should not happen since the input stream is
			 * coming from a bytebuffer.
			 */
			Exceptions.logStackTrace( e );
			return;
		} catch( Throwable t) {
			Exceptions.logStackTrace( t );
			return;
		} finally {
			try {
				reader.close();
			} catch ( Exception e ) {
			}
		}

		// log.debug( "INCOMPLETE MESSAGE \n" + rtspMessage );

		// Save attributes in session
		log.debug("leaving decode loop, state=" + state + ", message in session=" + rtspMessage);
		session.setAttribute( readStateATTR, state );
		session.setAttribute( rtspMessageATTR, rtspMessage );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.mina.filter.codec.ProtocolDecoder#dispose(org.apache.mina.common.IoSession)
	 */
	public void dispose( IoSession session ) throws Exception
	{
		// Do nothing
	}
}
