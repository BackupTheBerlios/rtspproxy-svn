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
 
#include <dbg.h>
#include <cstdlib>
#include "cache_play.h"
#include "rtspproxy.h"

#include "transport_rtp.h"
#include "transport_tcp.h"
#include "cache_segment.h"
#include "time_range.h"
#include "prefetching.h"

#ifdef HAVE_CONFIG_H
#include "../config.h"
#endif

CachePlay::CachePlay(CClientCnx *cnx, CacheItem *item) : 
	m_item( item ),
	m_cnx( cnx ),
	m_session_id( NULL ),
	m_pf( NULL )
{
	if ( !item )
		return;
}

CachePlay::~CachePlay()
{
	free( m_client_addr );
	free( m_session_id );
	if ( m_cache_transport )
		delete m_cache_transport;
		
	if ( m_pf )
		m_pf->end();
}

void CachePlay::setup(CRtspRequestMsg *msg)
{
	dbg("CachePlay::SETUP\n");

	CString str_cseq = msg->GetHdr("CSeq");
	CString str_tran = msg->GetHdr("Transport");
	str_tran.ToLower();

	m_transport = parse_transport( str_tran );
	m_client_port = get_client_port( str_tran );

	if ( m_transport == TRANSPORT_UNKNOWN ) {
		m_cnx->sendResponse( 461, /* Unsupported Transport */
				     atoi( msg->GetHdr("CSeq") ));
		return;
	}

	switch ( m_transport ) {

		case TRANSPORT_RTP_UDP :
			dbg("Transport is RTP/UDP...\n");
			m_cache_transport = new TransportRTP();
			m_cache_transport->set_parent( this );
			break;

		case TRANSPORT_RTP_TCP :
			dbg("Transport is RTP/TCP...\n");
			// m_cache_transport == new TransportTCP();
			break;

		default: ;
	}
	
	m_segment = m_item->segment();
	m_cache_transport->set_segment( m_segment );

	u_int32_t tmp_addr = m_cnx->get_socket()->GetPeerAddr().GetHost().s_addr;
	m_client_addr = (char *) malloc( 16 ); // "255.255.255.255\0" .. 16 should be enough..
	snprintf(m_client_addr, 16, "%d.%d.%d.%d",
			(tmp_addr & 0x000000ff),
			(tmp_addr & 0x0000ff00) >> 8,
			(tmp_addr & 0x00ff0000) >> 16,
			(tmp_addr & 0xff000000) >> 24 );

	dbg("Client address is '%s'\n", m_client_addr);
	m_cache_transport->set_client_addr( m_client_addr );
	m_cache_transport->set_client_port( m_client_port );

	if ( m_cache_transport->setup() == false ) {
		dbg("Error in transport setup..\n");
		return;
	}

	if ( IS_UDP( m_transport ) ) {
		/*
		 * Check for the port(s) stated by client and
		 * pick a port (or 2) for the proxy.
		 */
		char str[30];
		uint16_t p = m_cache_transport->get_port();
		snprintf( str, 30, ";server_port=%u-%u", p, p+1);
		m_transport_str = (char *)realloc( m_transport_str,
			strlen( m_transport_str) + strlen(str) );
		strncat( m_transport_str, str, 
				strlen( m_transport_str) + strlen(str) );
	}

	dbg("Proxy chosen transport: %s\n", m_transport_str);

	m_url = msg->GetUrl();

	CRtspResponseMsg *response = new CRtspResponseMsg();
	response->SetStatus( 200 );
	response->SetHdr( "Server", "RTSP Proxy Version " VERSION  );
	response->SetHdr("CSeq", msg->GetHdr("CSeq") );
	response->SetHdr("Transport", m_transport_str);
	if ( strlen( msg->GetHdr("Session") ) <= 1 )
		gen_session_id();
	else m_session_id = strdup( msg->GetHdr("Session") );
	response->SetHdr("Session", m_session_id);
	response->SetHdr("Via", CString( "RTSP/1.0 rtsp_proxy" ) );

	m_cnx->sendResponse( response );
	delete response;
}

void CachePlay::play(CRtspRequestMsg *msg, CachePlay *cp2)
{
	dbg("CachePlay::PLAY\n");

	uint32_t seq = 0;
	uint32_t timestamp = 0;
	uint32_t ssrc = 0;
	uint32_t seq2 = 0;
	uint32_t timestamp2 = 0;
	uint32_t ssrc2 = 0;

	CRtspResponseMsg *response = new CRtspResponseMsg();
	response->SetStatus( 200 );
	response->SetHdr("CSeq", msg->GetHdr("CSeq") );

	if ( IS_RTP( m_transport ) ) {
		/* Defining RTP specific parameter... */
		TransportRTP *rtp = (TransportRTP *)m_cache_transport;
		dbg("My SSRC: %u\n", rtp->session()->get_ssrc() );
		dbg("Initial TimeStamp: %u\n", rtp->session()->get_base_timestamp() );
		dbg("Initial Sequence number: %u\n", rtp->session()->get_base_seq() );
		seq = rtp->session()->get_base_seq();
		timestamp = rtp->session()->get_base_timestamp();
		ssrc = rtp->session()->get_ssrc();

		if ( cp2 != NULL ) {
			TransportRTP *rtp = (TransportRTP *)( cp2->get_transport() );
			seq2 = rtp->session()->get_base_seq();
			timestamp2 = rtp->session()->get_base_timestamp();
			ssrc2 = rtp->session()->get_ssrc();
		}
	}
	CString str;
	str.Append( "url=" );
	str.Append(  m_url );
	char s[50];
	snprintf(s, 50, ";seq=%u;rtptime=%u;ssrc=%u", seq, timestamp, ssrc);
	str.Append( s );
	if ( cp2 != NULL ) {
		str.Append( ", url=" );
		str.Append(  cp2->get_url() );
		char s[50];
		snprintf(s, 50, ";seq=%u;rtptime=%u;ssrc=%u", seq2, timestamp2, ssrc2);
		str.Append( s );
	}
	response->SetHdr( "Server", "RTSP Proxy Version " VERSION  );
	response->SetHdr( "RTP-Info", str );
	response->SetHdr( "Session", msg->GetHdr("Session") );
	response->SetHdr( "Range", m_cache_transport->get_range() );

	m_cnx->sendResponse( response );
	delete response;

	/*! Here we check if the cached segment does contain all 
	 *  the stream or only a piece of it.
	 */
	TimeRange *cache_range = m_segment->time_range();
	TimeRange *play_range = new TimeRange( m_cache_transport->get_range() );
	dbg("cache_range: %f-%f  -  play_range: %f-%f\n", 
		cache_range->start(), cache_range->end(),
		play_range->start(), play_range->end() );
	if ( cache_range->contain( play_range ) ) {
		/* Ok, we already have all the stream in cache.. */
		dbg("NO prefetching is needed..\n");
	} else if ( ! m_segment->parent()->is_locked_write() ) {
#warning Stream prefetching is enabled...
// #if 0
		/* We have to prefetch the end of the stream... */
		m_pf = new Prefetching();
		m_pf->set_stream_url( m_url );
		m_pf->set_segment( m_segment );
		m_pf->set_time_range( cache_range->end(), play_range->end() );
		m_pf->start();
// #endif
	} else {
		dbg("We are already prefetching the stream...\n");
	}
	delete play_range;

	/*! We use another thread to send packets to the client.
	 *  This way we have a more simple control over it.
	 */
	m_cache_transport->start();
}


void CachePlay::teardown(CRtspRequestMsg *msg)
{
	dbg("CachePlay::TEARDOWN\n");
	
	if ( m_pf )
		m_pf->end();

	CRtspResponseMsg *response = new CRtspResponseMsg();
	response->SetStatus( 200 );
	response->SetHdr( "CSeq", msg->GetHdr("CSeq") );
	response->SetHdr( "Server", "RTSP Proxy Version " VERSION  );
	response->SetHdr( "Session", msg->GetHdr("Session") );
	response->SetHdr( "Connection", "Close" );
	m_cnx->sendResponse( response );
	delete response;

	#warning We have to check where (and when) to delete m_cache_transport
	if ( m_cache_transport ) {
		m_cache_transport->close();
		#if 0
		delete m_cache_transport;
		#endif
	}
}

void CachePlay::send_teardown()
{
	CRtspRequestMsg *msg = new CRtspRequestMsg();
	msg->SetVerb( "TEARDOWN" );
	msg->SetUrl( m_url );
	msg->SetHdr( "Server", "RTSP Proxy Version " VERSION  );
	m_cnx->sendRequest( msg );
	delete msg;
}

uint8_t CachePlay::parse_transport(const char* str)
{
	char *str2 = strdup( str );
	char *s;
	int idx, r;

	dbg("Client Protocol List:\n");
	s = strtok(str2, ",");
	while (s != NULL) {
		idx = index(s, ';') - s;
		s[idx] = '\0';
		dbg("\t%s\n", s);
		r = check_transport( s );
		if (r != TRANSPORT_UNKNOWN) {
			s[idx] = ';';
			m_transport_str = strdup( s );
			return r;
		}
		s = strtok(0 , ",");
	}

	return TRANSPORT_UNKNOWN;
}

u_int16_t CachePlay::get_client_port(const char* str)
{
	char *idx, *idx2;
	char str2[20];

	idx = strstr(str, "client_port=");
	if (idx == NULL)
		return 0;

	idx += 12;
	idx2 = index(idx, '-');
	strncpy(str2, idx, (idx2 - idx) );
	str2[ idx2-idx ] = '\0';

	return atoi( str2 );
}

uint8_t CachePlay::check_transport(const char* str)
{
	/*
	 * We have to choice the "better" transport protocol
	 * in the list provided by the client.
	 * Also, we must check that if we have cached RTP packets,
	 * we cannot playback with RDT..
	 */
	if ( IS_RTP( m_item->transport() ) ) {
		if ( ! strcmp(str, "rtp/avp") ) {
			return TRANSPORT_RTP_UDP;
		}
		/* if ( ! strcmp(str, "rtp/avp/tcp") ) {
		 	return TRANSPORT_RTP_TCP;
	} */
		return TRANSPORT_UNKNOWN;
	}

	if ( IS_RDT( m_item->transport() ) ) {
		/*
		 * When we have RDT packet we are only able to send them
		 * trough a TCP connection, since we don't have access
		 * to RDT protocol.
		 */
		// FIXME: Not yet implemented!
		// if ( ! strcmp(str, "x-real-rdt/tcp") )
		//	return TRANSPORT_RDT_TCP;
	}

	return TRANSPORT_UNKNOWN;
}

void CachePlay::gen_session_id()
{
	char tmp[30];
	uint64_t n = 0;

	n = (uint64_t) my_random();

	snprintf( tmp, 30, "%u%u",
		(uint32_t) (n >> 32),
		(uint32_t) (n & 0x00000000ffffffff) );
	m_session_id = strdup( tmp );
}



/** LOG **
 *
 * $Log: cache_play.cpp,v $
 * Revision 1.4  2003/11/17 18:27:00  mat
 * Disabled stream prefetching (temporary)
 *
 * Revision 1.3  2003/11/17 16:13:47  mat
 * make-up
 *
 *
 */

