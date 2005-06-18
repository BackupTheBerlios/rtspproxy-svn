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
 * RTSP Status codes.
 */
public enum RtspCode {

	Continue						( 100 ),
	
	OK							( 200 ),
	Created						( 201 ),
	LowOnStorageSpace			( 250, "Low on Storage Space" ),

	MultipleChoices				( 300, "Multiple Choices" ),
	MovedPermanently				( 301, "Multiple Choices" ),
	MovedTemporarily				( 302, "Moved Temporarily" ),
	SeeOther						( 303, "See Other" ),
	NotModified					( 304, "Not Modified" ),
	UseProxy						( 305, "Not Modified" ),

	BadRequest					( 400, "Bad Request" ),
	Unauthorized					( 401 ),
	PaymentRequired				( 402, "Payment Required" ),
	Forbidden					( 403 ),
	NotFound						( 404, "Not Found" ),
	MethodNotAllowed				( 405, "Method Not Allowed" ),
	NotAcceptable				( 406, "Not Acceptable" ),
	ProxyAuthenticationRequired	( 407, "Proxy Authentication Required" ),
	RequestTimeOut				( 408, "Request Time-out" ),
	Gone							( 410 ),
	LengthRequired				( 411, "Length Required" ),
	PreconditionFailed			( 412, "Precondition Failed"),
	RequestEntityTooLarge			( 413, "Request Entity Too Large" ),
	RequestUriTooLarge			( 414, "Request-URI Too Large" ),
	UnsupportedMediaType			( 415, "Unsupported Media Type" ),
	ParameterNotUnderstood		( 451, "Parameter Not Understood" ),
	ConferenceNotFound			( 452, "Conference Not Found" ),
	NotEnoughBandwidth			( 453, "Not Enough Bandwidth" ),
	SessionNotFound				( 454, "Session Not Found" ),
	MethodNotValidInThisState		( 455, "Method Not Valid in This State" ),
	HeaderFieldNotValidForResource( 456, "Header Field Not Valid for Resource" ),
	InvalidRange					( 457, "Invalid Range" ),
	ParameterIsReadOnly			( 458, "Parameter Is Read-Only" ),
	AggregateOperationNotAllowed	( 459, "Aggregate operation not allowed" ),
	OnlyAggregateOperationAllowed	( 460, "Only aggregate operation allowed" ),
	UnsupportedTransport			( 461, "Unsupported transport" ),
	DestinationUnreachable 		( 464, "Destination unreachable" ),

	InternalServerError			( 500, "Internal Server Error" ),
	NotImplemented				( 501, "Not Implemented" ),
	BadGateway					( 502, "Bad Gateway" ),
	ServiceUnavailable			( 503, "Service Unavailable" ),
	GatewayTimeOut				( 504, "Gateway Time-out" ),
	RtspVersionNotSupported		( 505, "RTSP Version not supported" ),
	OptionNotSupported			( 551, "Option not supported" );

	private final int code;
	private final String description;
	
	private RtspCode( int code, String description ) 
	{
		this.code = code;
		this.description = description;
	}
	
	private RtspCode( int code )
	{
		this.code = code;
		this.description = null;
	}
	
	public String toString()
	{
		if ( description != null )
			return description;
		else 
			return name();
	}
}
