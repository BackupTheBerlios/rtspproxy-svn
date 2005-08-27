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
	rtspVersion.second = 0;
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
	return headers.count();
}

QString RtspMessage::getHeader( const QString& key ) const
{
	if ( ! headers.contains( key ) ) 
		// Header not found
		return QString::null;

	return headers[ key ];
}

void RtspMessage::setHeader( const QString& key, const QString& value )
{
	headers.insert( key, value );
}

void RtspMessage::removeHeader( const QString& key )
{
	headers.remove( key );
}

QString RtspMessage::getHeadersString() const
{
	QString s;
	QTextStream str( &s );
	HeadersDictIterator it( headers );
	while ( it.hasNext() ) {
		it.next();
		str << it.key() << ": " << it.value() << CRLF;
	}
	return s;
}

void RtspMessage::setBuffer( const QByteArray& buffer ) 
{ 
	this->buffer = buffer;
	if ( buffer.size() ) {
		setHeader( "Content-Length", QString::number( buffer.size() ) );
	} else {
		removeHeader( "Content-Length" );
	}
} 

/** 
 * Append the content to the current buffer.
 */
void RtspMessage::addToBuffer( const QByteArray& other )
{
	this->buffer += other;
	setBuffer( this->buffer );
}


