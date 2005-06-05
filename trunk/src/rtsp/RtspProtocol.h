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
 
#ifndef _RTSP_PROTOCOL_H_
#define _RTSP_PROTOCOL_H_

#include <QtCore>

#define DEFAULT_RTSP_PORT 554

class RtspRequest;
class RtspResponse;
 
class RtspProtocol
{
public:
	RtspProtocol();
	virtual ~RtspProtocol();
	
	void sendRequest( RtspRequest* request );
	
private:
	enum ReadState {
		stFail,		// Unrecoverable error occurred
		stSync,		// Trying to resync
		stReady,	// Waiting for a command
		stPkt,		// Reading interleaved packet
		stCmd,		// Reading command (request or response line)
		stHdr,		// Reading headers
		stBody,		// Reading body (entity)
		stDispatch,	// Fully formed message
		stREADSTATE_LAST
	};
	
	quint32 getNextCSeq() { return ++cseqToSend; }

	quint32 cseqToSend;
	quint32 cseqReceived;
	ReadState internalReadState;
}; 
 
#endif // _RTSP_PROTOCOL_H_
 