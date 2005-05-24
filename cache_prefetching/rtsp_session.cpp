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
 
#include "../config.h"
 
#include <assert.h>
#include <string.h>
#include <netdb.h>
#include <sys/types.h> 
#include <sys/socket.h> 

#include "rtsp_session.h"
#include "../libapp/dbg.h"
#include "../cache_manager/time_range.h"

#define DEFAULT_RTSP_PORT 554

RtspSession::RtspSession(const char* url)
{
	assert( url );
	m_url = strdup( url );
	
	m_port = 0;
	parse_url();
}

RtspSession::~RtspSession()
{

}

bool RtspSession::start(u_int16_t rtp_port, TimeRange* tr)
{
	if ( ! connect() )
		return false;
		
	char msg[4096];
	snprintf(msg, 4096, 
		"SETUP %s RTSP/1.0\r\n"
		"CSeq: 1\r\n"
		"Transport: rtp/avp;unicast;client_port=%u-%u\r\n"
		"User-Agent: RTSP Proxy Version " VERSION " \r\n"
		"\r\n", m_url, rtp_port, rtp_port + 1 );
	
	int res = write( m_sock, msg, strlen(msg) );
	if ( res < strlen(msg) ) 
		return false;
	/// dbg( msg );
	
	if ( !recv_response() )
		return false;
		
	snprintf(msg, 4096, 
		"PLAY %s RTSP/1.0\r\n"
		"CSeq: 2\r\n"
		"Session: %s\r\n"
		"Range: npt=%f-%f\r\n"
		"User-Agent: RTSP Proxy Version " VERSION " \r\n"
		"\r\n", m_url, m_session, tr->start(), tr->end() );
	
	res = write( m_sock, msg, strlen(msg) );
	if ( res < strlen(msg) ) 
		return false;
	dbg( msg );
	
	if ( !recv_response() )
		return false;


	return true;
}

bool RtspSession::end()
{
	char msg[4096];
	snprintf(msg, 4096, 
		"TEARDOWN %s RTSP/1.0\r\n"
		"CSeq: 3\r\n"
		"Session: %s\r\n"
		"User-Agent: RTSP Proxy Version " VERSION " \r\n"
		"\r\n", m_url, m_session );
		
	close( m_sock );
	
	free( m_url );
	free( m_host );
	free( m_session );
	
	return true;
}

bool RtspSession::recv_response()
{
	char msg[4096];
	int res = read( m_sock, msg, 4096 );
	if ( res == -1 ) {
		perror("RtspSession::recv_response()");
		return false;
	}
	
	dbg( msg );
	
	if ( strstr( msg, "RTSP/1.0 200" ) == NULL ) {
		/* Server replyed with an error message.. */
		return false;
	}
	if ( strstr( msg, "Session: " ) != NULL ) {
		/* Server sent a session header..  */
		char *s = strstr( msg, "Session: " ) + 9;
		char *e = strstr( s, "\r\n" );
		m_session = (char *)malloc( e - s );
		memcpy( m_session, s, e - s );
		m_session[ e - s ] = '\0';
	}
	if ( strstr( msg, "Transport: " ) != NULL ) {
		/* Server sent a transport header..  */
		char *s = strstr( msg, "server_port=" ) + 12;
		char *e = strstr( s, "-" );
		char *v = (char *)malloc( e - s );
		memcpy( v, s, e - s );
		v[ e - s ] = '\0';
		m_rtp_port = atoi( v );
		free( v );
	}
	
	return true;
}

void RtspSession::parse_url()
{
	/* URL is in the form of 
	 *         rtsp://host:port/path/to/stream.mp4/streamid=0 
	 */
	char *start = strstr( m_url, "//") + 2;
	char *end;
	char *end1 = strstr( start, ":");
	char *end2 = strstr( start, "/");
	if ( end1 == NULL ) {
		end = end2;
		m_port = DEFAULT_RTSP_PORT;
	} else  end = (end1 < end2) ? end1 : end2;
	
	u_int16_t size = end - start;
	m_host = (char *)malloc( size );
	memcpy( m_host, start, size  );
	m_host[ size ] = '\0';
	
	// dbg("Host name: '%s'\n", m_host );
	
	if ( m_port != 0 )
		/* Port is default */
		return;
		
	start = end + 1;
	end = strstr( start, "/" );
	size = end - start;
	char *s = (char *)malloc( size );
	memcpy( s, start, size  );
	s[ size ] = '\0';
	
	m_port = atoi( s );
	// dbg("Port: %u\n", m_port );
}


bool RtspSession::connect()
{
	struct hostent *host;
	struct in_addr h_addr;
	struct sockaddr_in addr;

	m_sock = socket( AF_INET, SOCK_STREAM, 0 );
	if ( m_sock == -1 ) {
		perror("RtspSession::connect()");
		return false;
	}
	
	host = gethostbyname( m_host );
	if ( host == NULL ) {
		perror("RtspSession::connect()");
		return false;
	}
	h_addr = *( (struct in_addr *) (host->h_addr) );
	
	addr.sin_family = AF_INET;
	addr.sin_addr = h_addr;
	addr.sin_port = htons( m_port );
	
	if ( ::connect( m_sock, (struct sockaddr*)&addr, sizeof(struct sockaddr) ) == -1 ) {
		perror("RtspSession::connect()");
		return false;
	}
	
	return true;
}

