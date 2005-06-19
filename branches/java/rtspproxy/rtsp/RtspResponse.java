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

/**
 * Wraps up a RTSP response message. 
 */
public class RtspResponse extends RtspMessage
{

	RtspCode code;

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

	/**
	 * Serialize the RTSP response to a string.
	 * 
	 * <pre>
	 * "RTSP/1.0" SP [code] SP [reason] CRLF
	 * [headers] CRLF
	 * CRLF
	 * [buf] 
	 * </pre>
	 */
	public String toString()
	{
		String str = "RTSP/1.0 ";
		str += code.value() + " " + code.description() + CRLF;
		str += getHeadersString();

		// Insert a blank line
		str += CRLF;
		
		if ( getBufferSize() > 0 )
			str += getBuffer();

		return str;
	}

}
