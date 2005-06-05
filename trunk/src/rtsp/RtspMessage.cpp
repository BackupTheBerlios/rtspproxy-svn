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

#include "RtspMessage.h"

RtspMessage::RtspMessage()
{
	sequenceNumber = 0;
	
	// default is RTSP/1.0
	rtspVersion.first = 1;
	rtspVersione.second = 0;
}

RtspMessage::RtspMessage( const RtspMessage& ) // other )
{
	// TODO 
}

RtspMessage::~RtspMessage()
{
}

void RtspMessage::setRtspVersion( quint8 major, quint8 minor )
{
	rtspVersion.first = major;
	rtspVersion.second = minor;
}

quint32 RtspMessage::getHeadersCount() const
{
	return headerList.count();
}

QString RtspMessage::getHeader( const QString& key ) const
{
	if ( ! headers.contains( key ) ) 
		// Header not found
		return QString::null;

	return headers[ key ];
}

void setHeader( const QSring& key, cont QString& value )
{
	headers.insert( key, value );
}

QString RtspMessage::getHeadersString() const
{
	QString* headers = new QString();
	QTextStream str( headers );
	HeadersDictIterator it( headerList );
	while ( it.hasNext() ) {
		RtspHeader h = it.next();
		str << h.getKey() << ": " << h.getValue() << CRLF;
	}
	return *headers;
}

void RtspMessage::setBuffer( const QByteArray& buffer ) 
{ 
	this->buffer = buffer;
	if ( buffer.size() ) {
		setHeader( "Content-Length", QString::fromInt( buffer.size() ) );
	} else {
		removeHeader( "Content-Length" );
	}
} 


