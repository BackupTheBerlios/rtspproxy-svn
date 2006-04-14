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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.mina.common.ByteBuffer;

/**
 * Wraps up a RTSP response message.
 */
public class RtspResponse extends RtspMessage
{

	private static Logger log = LoggerFactory.getLogger( RtspResponse.class );

	RtspCode code;
	RtspRequest.Verb requestVerb = RtspRequest.Verb.None;

	public RtspResponse()
	{
		super();
		code = RtspCode.OK;
	}

	@Override
    public Type getType()
	{
		return Type.TypeResponse;
	}

	public RtspCode getCode()
	{
		return code;
	}

	public void setCode( RtspCode code )
	{
		this.code = code;
	}

	public void setRequestVerb( RtspRequest.Verb requestVerb )
	{
		this.requestVerb = requestVerb;
	}

	public RtspRequest.Verb getRequestVerb()
	{
		return requestVerb;
	}

	/**
	 * Serialize the RTSP response to a string.
	 * 
	 * <pre>
	 *    &quot;RTSP/1.0&quot; SP [code] SP [reason] CRLF
	 *    [headers] CRLF
	 *    CRLF
	 *    [buf] 
	 * </pre>
	 */
	@Override
    public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "RTSP/1.0 " ).append( code.value() ).append( ' ' );
		sb.append( code.description() ).append( CRLF );
		sb.append( getHeadersString() );

		// Insert a blank line
		sb.append( CRLF );

		if ( getBufferSize() > 0 ) {
			sb.append( getBuffer() );

			log.debug( "Buffer Size: {}", getBufferSize() );
		}

		return sb.toString();
	}

	/**
	 * serialize the RTSP response message into a byte buffer.
	 */
	public ByteBuffer toByteBuffer() throws Exception
	{
		try {
			String msg = this.toString();
			ByteBuffer buffer = ByteBuffer.wrap( msg.getBytes( "UTF-8" ) );

			return buffer;
		} catch ( Exception e ) {
			log.error( "failed to serialize message to byte buffer", e );

			throw e;
		}
	}

	/**
	 * Construct a new RtspResponse error message.
	 * 
	 * @param errorCode
	 *        the RTSP error code to be sent
	 * @return a RTSP response message
	 */
	public static RtspResponse errorResponse( RtspCode errorCode )
	{
		RtspResponse response = new RtspResponse();
		response.setCode( errorCode );
		response.setCommonHeaders();
		return response;
	}

}
