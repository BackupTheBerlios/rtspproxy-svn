/**************************************************************************
 *   Copyright (C) 2003 Matteo Merli <matteo.merli@studenti.unipr.it>
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
 *****************************************************************************/

#ifndef _RTSP_SESSION_H_
#define _RTSP_SESSION_H_

#include <sys/types.h>

class TimeRange;

class RtspSession
{
public:
	RtspSession(const char* url);
	~RtspSession();
	
	bool start(u_int16_t rtp_port, TimeRange* tr);
	
	bool end();
	
	u_int16_t server_rtp_port() {return m_rtp_port;}
	
	const char* server_addr() {return m_host;}
	
	u_int16_t server_port() {return m_port;}
	
protected:
	void parse_url();
	
	bool connect();
	
	bool recv_response();

	/*! URL */
	char *m_url;

	/*! Remote host address. */
	char *m_host;
	
	/*! Remote host port. */
	u_int16_t m_port;
	
	char *m_session;
	
	int m_sock;
	
	u_int16_t m_rtp_port;

};

#endif

