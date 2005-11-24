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

import org.apache.log4j.Logger;

/**
 * Wraps up a RTSP response message.
 */
public class RtspResponse extends RtspMessage
{

	private static Logger log = Logger.getLogger( RtspResponse.class );

	RtspCode code;
	RtspRequest.Verb requestVerb = RtspRequest.Verb.None;

	public RtspResponse()
	{
		super();
		code = RtspCode.OK;
	}

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
	 *  &quot;RTSP/1.0&quot; SP [code] SP [reason] CRLF
	 *  [headers] CRLF
	 *  CRLF
	 *  [buf] 
	 * </pre>
	 */
	public String toString()
	{
		String str = "RTSP/1.0 " + code.value() + " " + code.description() + CRLF;
		str += getHeadersString();

		// Insert a blank line
		str += CRLF;

		if ( getBufferSize() > 0 ) {
			str += getBuffer();

			log.debug( "Buffer Size: " + getBufferSize() );
		}

		return str;
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
		return response;
	}

}
