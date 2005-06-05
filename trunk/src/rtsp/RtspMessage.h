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
 
#ifndef _RTSP_MESSAGE_H_
#define _RTSP_MESSAGE_H_

#include "RtspHeader.h"

#include <QtCore>

// A pair of 8-bits integers
typedef QPair<quint8,quint8> IntPair;

typedef QMap<QString,QString> HeadersDict;
typedef QMapIterator<QString,QString> HeadersDictIterator;

#define CRLF "\r\n"

class RtspMessage
{
public:

	enum MessageType {
		TypeNone,
		TypeRequest,
		TypeResponse,
		TypeLast
	};

	RtspMessage();
	RtspMessage( const RtspMessage& other );
	
	virtual ~RtspMessage();
	
	IntPair getRtspVersion() const { return rtspVersion; }
	void setRtspVersion( quint8 major, quint8 minor );
	
	quint32 getHeadersCount() const;
	
	QString getHeader( const QString& key ) const;
	
	QString getHeadersString() const;
	
	void setHeader( const QSring& key, cont QString& value );
	
	virtual MessageType getType() const { return TypeNone; }
	
	QByteArray getBuffer() { return buffer; }
	void setBuffer( const QByteArray& buffer );
	
	virtual QString toString() const = 0;
	
protected:
	IntPair rtspVersion;
	quint32 sequenceNumber;
	HeadersDict headers;
	QByteArray buffer;
};

#endif // _RTSP_MESSAGE_H_
