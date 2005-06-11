/**************************************************************************
 *   Copyright (C) 2005 Matteo Merli <matteo.merli@gmail.com>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *   $Id$
 * 
 *   $URL$
 * 
 *****************************************************************************/

#include "RtspResponse.h"

struct StatusMapEntry {
	qint8 code;
	const char* name;
};

extern struct StatusMapEntry s_mapStatus[];

RtspResponse::RtspResponse()
	: RtspMessage() 
{
	code = 0;
}

RtspResponse::~RtspResponse()
{
}

QString RtspResponse::getCodeString() const
{
	int i = 0;
	while ( 1 ) {
		struct StatusMapEntry *entry = &s_mapStatus[ i++ ];
		if ( entry->code == code ) 
			return entry->name;
		if ( entry->code == 0 )	
			// end of the list
			break;
	}
	qWarning() << "Invalid RTSP status code:" << code;
	return QString("");
}

QByteArray RtspResponse::toString() const
{
	// "RTSP/1.0" SP <code> SP <reason> CRLF
	// <headers> CRLF
	// <buf> 
	QByteArray req;
	QTextStream str( &req );
	str << "RTSP/" << rtspVersion.first << "." << rtspVersion.second << " " 
		<< code << " " << getCodeString() << CRLF;
	str << getHeadersString();
	
	// Insert a blank line
	str << CRLF;
	
	if ( buffer.size() ) {
		str << buffer;
	}
	
	return req;
}

struct StatusMapEntry s_mapStatus[] = {
	{100, "Continue"},

	{200, "OK"},
	{201, "Created"},
	{250, "Low on Storage Space"},

	{300, "Multiple Choices"},
	{301, "Moved Permanently"},
	{302, "Moved Temporarily"},
	{303, "See Other"},
	{304, "Not Modified"},
	{305, "Use Proxy"},

	{400, "Bad Request"},
	{401, "Unauthorized"},
	{402, "Payment Required"},
	{403, "Forbidden"},
	{404, "Not Found"},
	{405, "Method Not Allowed"},
	{406, "Not Acceptable"},
	{407, "Proxy Authentication Required"},
	{408, "Request Time-out"},
	{410, "Gone"},
	{411, "Length Required"},
	{412, "Precondition Failed"},
	{413, "Request Entity Too Large"},
	{414, "Request-URI Too Large"},
	{415, "Unsupported Media Type"},
	{451, "Parameter Not Understood"},
	{452, "Conference Not Found"},
	{453, "Not Enough Bandwidth"},
	{454, "Session Not Found"},
	{455, "Method Not Valid in This State"},
	{456, "Header Field Not Valid for Resource"},
	{457, "Invalid Range"},
	{458, "Parameter Is Read-Only"},
	{459, "Aggregate operation not allowed"},
	{460, "Only aggregate operation allowed"},
	{461, "Unsupported transport"},
	{462, "Destination unreachable"},

	{500, "Internal Server Error"},
	{501, "Not Implemented"},
	{502, "Bad Gateway"},
	{503, "Service Unavailable"},
	{504, "Gateway Time-out"},
	{505, "RTSP Version not supported"},
	{551, "Option not supported"},
	{0, NULL}
};

