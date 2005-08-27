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

#ifndef _RTSP_RESPONSE_H_
#define _RTSP_RESPONSE_H_

#include "RtspMessage.h"

#include <QtCore>

class RtspResponse : public RtspMessage
{
public:	
	RtspResponse();
	~RtspResponse();	
	
	MessageType getType() const { return TypeResponse; }
	
	quint8 getCode() const { return code; }
	QString getCodeString() const; 
	void setCode( quint8 code ) { this->code = code; }
	
	QByteArray toString() const;
	
private:
	quint8 code;
};

#endif //_RTSP_RESPONSE_H_
