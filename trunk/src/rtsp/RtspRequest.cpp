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

#include "RtspRequest.h"

// Verb string (in the same order as Verb enum constants)
static const char* s_verbs[] = {
	"-NONE", 
	"ANNOUNCE",
	"DESCRIBE",
	"GET_PARAMETER",
	"OPTIONS",
	"PAUSE", 
	"PLAY", 
	"RECORD", 
	"REDIRECT",
	"SET_PARAMETER",
	"TEARDOWN",
	"LAST",
	NULL
};


RtspRequest::RtspRequest()
	: RtspMessage() 
{
	verb = VerbNone;
}

RtspRequest::~RtspRequest()
{
}

QString RtspRequest::getVerbString() const
{
	return *(new QString( s_verbs[ verb ] ) );
}

void RtspRequest::setVerb( const QString& verb )
{
	int i = VerbNone;
	while ( 1 ) {
		char* v = (char*)s_verbs[ i ];
		if ( verb == v ) {
			this->verb = (Verb)i;
			return;
		}
		++i;
		if ( i > VerbLast )
			break;
	}
	
	qWarning() << "Unknown verb:" << verb;
	this->verb = VerbNone;
	return;
}

QString RtspRequest::toString() const
{
	// <verb> SP <url> SP "RTSP/1.0" CRLF
	// <headers> CRLF <buffer>
	QString *req = new QString();
	QTextStream str( req );
	str << verb << " " << url.toString() << " " 
		<< "RTSP/" << rtspVersion.first << "." << rtspVersion.second << CRLF;
	str << getHeadersString();
	
	// Insert a blank line
	str << CRLF;
	
	if ( buffer.size() ) {
		str << buffer;
	}
	
	return *req;
}


