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

import java.nio.CharBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import rtspproxy.config.Config;

/**
 * Base abstract class for RTSP messages.
 * 
 * @author Matteo Merli
 */
public abstract class RtspMessage
{

	public static final String lastRequestVerbATTR = RtspMessage.class.toString()
			+ "lastRequestVerb";

	/**
	 * RTSP Message Type
	 */
	public enum Type {
		/** Generic message (internal use) */
		TypeNone,
		/** Request message */
		TypeRequest,
		/** Response message */
		TypeResponse
	};

	private int sequenceNumber;

	private Map<String, String> headers;

	private StringBuffer buffer;

	/**
	 * Constructor.
	 */
	public RtspMessage()
	{
		sequenceNumber = 0;
		headers = new LinkedHashMap<String, String>();
		buffer = new StringBuffer();
	}

	/**
	 * @return the RTSP type of the message
	 */
	public Type getType()
	{
		return Type.TypeNone;
	}

	/**
	 * Adds a new header to the RTSP message.
	 * 
	 * @param key
	 *            The name of the header
	 * @param value
	 *            Its value
	 */
	public void setHeader( String key, String value )
	{
		// Handle some bad formatted headers
		if ( key.compareToIgnoreCase( "content-length" ) == 0 ) {
			headers.put( "Content-Length", value );
		} else {
			headers.put( key, value );
		}
	}

	/**
	 * @param key
	 *            Header name
	 * @return the value of the header
	 */
	public String getHeader( String key )
	{
		return headers.get( key );
	}

	/**
	 * @param key
	 *            Header name
	 * @param defaultValue
	 *            the default value
	 * @return the value of the header of <i>defaultValue</i> if header is not
	 *         found
	 */
	public String getHeader( String key, String defaultValue )
	{
		String value = getHeader( key );
		if ( value == null )
			return defaultValue;
		else
			return value;
	}

	/**
	 * Remove an header from the message headers collection
	 * 
	 * @param key
	 *            the name of the header
	 */
	public void removeHeader( String key )
	{
		headers.remove( key );
	}

	/**
	 * Formats all the headers into a string ready to be sent in a RTSP message.
	 * 
	 * <pre>
	 *     Header1: Value1
	 *     Header2: value 2
	 *     ... 
	 * </pre>
	 * 
	 * @return a string containing the serialzed headers
	 */
	public String getHeadersString()
	{
		StringBuilder buf = new StringBuilder();
		for ( String key : headers.keySet() ) {
			buf.append( key ).append( ": " ).append( headers.get( key ) ).append( CRLF );
		}
		return buf.toString();
	}

	/**
	 * get a map of all headers set in the request
	 * @return an unmodifiable map of all header fields in this request. 
	 */
	public Map<String, String> getHeaders() {
		if(this.headers != null)
			return Collections.unmodifiableMap(this.headers);
		else
			return Collections.unmodifiableMap(new HashMap<String, String>());
	}
	
	/**
	 * @return the number of headers owned by the message
	 */
	public int getHeadersCount()
	{
		return headers.size();
	}

	/**
	 * Sets common headers like <code>Server</code> and <code>Via</code>.
	 */
	public void setCommonHeaders()
	{
		String proxy = Config.getProxySignature();
		if ( getHeader( "Server" ) != null )
			setHeader( "Server", proxy );
		
		String via = getHeader("Via");
		StringBuffer newVia = new StringBuffer();
		
		if(via != null) {
			newVia.append(", ");
		}
		newVia.append("RTSP/1.0 ");
		newVia.append(Config.proxyClientInterface.getStringValue());
		if(!Config.proxyServerInterface.getStringValue().equals(Config.proxyClientInterface.getStringValue())) {
			newVia.append(", RTSP/1.0 ");
			newVia.append(Config.proxyServerInterface.getStringValue());
		}
		setHeader("Via", newVia.toString());
	}

	/**
	 * @param buffer
	 *            StringBuffer containing the contents
	 */
	public void setBuffer( StringBuffer buffer )
	{
		this.buffer = buffer;
	}

	/**
	 * @param other
	 *            buffer with content to be appended
	 */
	public void appendToBuffer( StringBuffer other )
	{
		this.buffer.append( other );
	}

	/**
	 * @param other
	 *            buffer with content to be appended
	 */
	public void appendToBuffer( String other )
	{
		this.buffer.append( other );
	}

	/**
	 * @param other
	 *            buffer with content to be appended
	 */
	public void appendToBuffer( CharBuffer other )
	{
		this.buffer.append( other );
	}

	/**
	 * @return the content buffer
	 */
	public StringBuffer getBuffer()
	{
		return buffer;
	}

	/**
	 * @return the size of the content buffer
	 */
	public int getBufferSize()
	{
		return buffer.length();
	}

	// CRLF
	public static final String CRLF = "\r\n";

	/**
	 * get the sequence number. If the sequence has not been set, the value from the 
	 * <b>CSeq</b> header is scaned (if the header is set)
	 * @return Returns the sequenceNumber. Returns 0 if the sequence number has not been set and the
	 * <b>CSeq</b> header is not available.
	 */
	public int getSequenceNumber()
	{
		if(this.sequenceNumber == 0) {
			try {
				if(this.headers.containsKey("CSeq"))
					this.sequenceNumber = Integer.parseInt(this.headers.get("CSeq"));
			} catch(Exception e) {
				
			}
		}
			
		return sequenceNumber;
	}

	/**
	 * Set the sequence number. As a by-product it also sets the CSeq header field to this
	 * value.
	 * 
	 * @param sequenceNumber
	 *            The sequenceNumber to set.
	 */
	public void setSequenceNumber( int sequenceNumber )
	{
		this.sequenceNumber = sequenceNumber;
		this.headers.put("CSeq", String.valueOf(this.sequenceNumber));
	}
}
