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

import java.util.Properties;

/**
 * @author mat
 * 
 */
public abstract class RtspMessage
{

	// Enum Message Type
	/**
	 * 
	 */
	public enum Type {
		TypeNone, TypeRequest, TypeResponse
	};

	private int sequenceNumber;
	private Properties headers;
	private StringBuffer buffer;

	/**
	 * Constructor.
	 */
	public RtspMessage()
	{
		sequenceNumber = 0;
		headers = new Properties();
		buffer = new StringBuffer();
	}

	/**
	 * Adds a new header to the RTSP message.
	 * 
	 * @param key
	 *        The name of the header
	 * @param value
	 *        Its value
	 */
	public void setHeader( String key, String value )
	{
		// Handle some nasty formatted headers
		if ( key == "Content-length" )
			key = "Content-Length";
		headers.setProperty( key, value );
	}

	public String getHeader( String key )
	{
		return headers.getProperty( key );
	}

	public void removeHeader( String key )
	{
		headers.remove( key );
	}

	/**
	 * Formats all the headers into a string ready to be sent in a RTSP message.
	 * 
	 * <pre>
	 * Header1: Value1
	 * Header2: value 2
	 * ... 
	 * </pre>
	 * 
	 * @return a string containing the serialzed headers
	 */
	public String getHeadersString()
	{
		String str = "";
		for ( Object key : headers.keySet() ) {
			String value = headers.getProperty( (String)key );
			str += key + ": " + value + CRLF;
		}
		return str;
	}

	public int getHeadersCount()
	{
		return headers.size();
	}

	public void setBuffer( StringBuffer buffer )
	{
		this.buffer = buffer;
	}

	public void appendToBuffer( StringBuffer other )
	{
		this.buffer.append( other );
	}

	public StringBuffer getBuffer()
	{
		return buffer;
	}

	public int getBufferSize()
	{
		return buffer.capacity();
	}

	// CRLF
	public static final String CRLF = "\r\n";
}