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

#ifndef _RTSP_REQUEST_H_
#define _RTSP_REQUEST_H_

#include "RtspMessage.h"

#include <QtCore>

class RtspRequest : public RtspMessage
{
public:
	enum Verb {
		VerbNone,
		VerbAnnounce,
		VerbDescribe,
		VerbGetParam,
		VerbOptions,
		VerbPause,
		VerbPlay,
		VerbRecord,
		VerbRedirect,
		VerbSetup,
		VerbSetParam,
		VerbTeardown,
		VerbLast
	};
	
	RtspRequest();
	~RtspRequest();	
	
	MessageType getType() const { return TypeRequest; }
	
	Verb getVerb() const { return verb; }
	QString getVerbString() const;
	
	void setVerb( Verb verb ) { this->verb = verb; }
	void setVerb( const QString& verb );
	
	void setUrl( const QUrl& url ) { this->url = url; }
	QUrl getUrl() const { return url; }
	
	QByteArray toString() const;
	
private:
	Verb verb;
	QUrl url;
};

#endif //_RTSP_REQUEST_H_
