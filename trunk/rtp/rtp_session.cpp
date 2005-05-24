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
 
#include "rtp_session.h"

#include <arpa/inet.h>
#include <math.h>
#include <netinet/in.h>
#include <netdb.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>
#include <time.h>
#include <sys/stat.h>
#include <fcntl.h>

#define MIN_UDP_PORT (uint16_t)6970
#define MAX_UDP_PORT (uint16_t)32000

uint64_t my_random()
{
	int fd = open( "/dev/random", O_RDONLY );
	if ( fd == -1 ) {
		/* System does not have the random device... */
		srandom( time(NULL) );
		return (uint64_t)random();
	}

	uint64_t r;
	read( fd, &r, 8 );
	close( fd );
	return r;
}


#define GETTIMEOFDAY_TO_NTP_OFFSET 2208988800U
/* Convert microseconds to 2^-32's of a second (the lsw of an NTP
 * timestamp).  This uses the factorization
 * 2^32/10^6 = 4096 + 256 - 1825/32 which results in a max conversion
 * error of 3 * 10^-7 and an average error of half that.
 */
ntp_t timeval_to_ntp(struct timeval tv)
{
	ntp_t n;
	uint32_t t = (tv.tv_usec * 1825) >> 5;
	n.frac = ((tv.tv_usec << 12) + (tv.tv_usec << 8) - t);
	n.secs = (uint32_t) tv.tv_sec + GETTIMEOFDAY_TO_NTP_OFFSET;
	return n;
}


/*! Convert a struct timeval to a double */
#define tv2dbl(tv) ((tv).tv_sec + (tv).tv_usec / 1000000.0)

/*! Convert a double to a struct timeval */
static inline struct timeval dbl2tv(double d)
{
	struct timeval tv;
	tv.tv_sec = (long) d;
	tv.tv_usec = (long) ((d - (long) d) * 1000000.0);
	return tv;
}

RtpSession::RtpSession() :
	m_rtp_sock( 0 ),
	m_rtcp_sock( 0 ),
	m_rtp_local_port( 0 ),
	m_ssrc( 0 ),
	m_cname( NULL ),
	m_psent( 0 ),
	m_osent( 0 ),
	m_rtcp_avg_size( 0 ),
	m_rtcp_psent( 0 ),
	m_bandwidth( 64000 ),
	m_preceived( 0 ),
	m_last_timestamp( 0 )
{

}

RtpSession::~RtpSession()
{
	if ( m_rtp_sock )
		::close( m_rtp_sock );
	if ( m_rtcp_sock )
		::close( m_rtcp_sock );

	if ( m_cname )
		free( m_cname );
	
}

void RtpSession::insert_event(event_t *e)
{
	if ( ! m_event_queue ) {
		m_event_queue = e;
		return;
	}
	
	event_t *c = m_event_queue;
	while ( c ) {
		if ( ! c->next )
			break;
		c = (event_t *)c->next;
	}
	c->next = e;
}

bool RtpSession::setup( uint16_t base_port, const char* cname, 
		const char* peer_addr, uint16_t peer_port, 
		uint8_t payload_type, uint32_t rtp_clock )
{
  	if ( rtp_clock == 0 ) {
    		printf("RtpSession: RTPClock is 0.. aborting..\n");
		return false;
  	}

	m_payload_type = payload_type;
	m_rtp_clock = rtp_clock;

	m_ssrc = (uint32_t)my_random();
	m_base_timestamp = (uint32_t)my_random();
	m_base_seq = (uint16_t)my_random();

	m_last_timestamp = m_base_timestamp;
	m_prev_timestamp = m_base_timestamp;
	m_seq = m_base_seq;

	if ( ! create_sockets( base_port ) ) {
		printf("RtpSession: cannot create socket pairs\n");
		return false;
	}
	if ( ! setup_peer( peer_addr, peer_port ) ) {
		printf("RtpSession: cannot setup pear host\n");
		return false;
	}
		
	set_cname( cname );
	
	return true;
}

bool RtpSession::send_rtp_packet(void *buf, uint16_t size, uint32_t ts_inc )
{
	uint32_t timestamp;
	struct timeval start_time_tv, now_tv;
	double play_time, now;
	
	timestamp = m_prev_timestamp + ts_inc;
	m_prev_timestamp = timestamp;
	
	if ( !buf || !size ) {
	  printf("Cannot send packet.\n");
	  return false;
	}
	
	if ( ! m_psent ) {
		/* This is the 1th packet that we send.
		 * Here we should send an RTCP SDES packet with all the 
		 * infos...
		 */
		gettimeofday(&start_time_tv, NULL);
		m_start_time = tv2dbl(start_time_tv);
		
		/*! Build the SDES CNAME packet */
		uint16_t s = 10 + strlen( m_cname );
		/* Align to 32 bit boundary .. */
		s = (uint16_t)ceil( (double)s /4 ) * 4;
		
		m_sdes_pkt = (rtcp_t *)calloc( s , 1 );
		RtcpPacket *p = new RtcpPacket( m_sdes_pkt, s );
		p->set_version( 2 );
		p->set_padding( 0 );
		p->set_count( 1 );
		p->set_type( RTCP_SDES );
		p->set_length( (uint16_t)s/4 - 1 );
		m_sdes_pkt->r.sdes.src = htonl( m_ssrc );
		m_sdes_pkt->r.sdes.item->type = RTCP_SDES_CNAME;
		m_sdes_pkt->r.sdes.item->length = strlen( m_cname );
		memcpy( m_sdes_pkt->r.sdes.item[0].data, m_cname, strlen(m_cname) );
		m_sdes_pkt_size = s;

		/*! Schedule an RTCP for now.. */
		event_t *e = (event_t*)malloc( sizeof( event_t ) );
 		e->type = RTCP_SR;
		e->time = 0.0;
		e->next = NULL;
		insert_event( e );
		send_rtcp();
	}

	/** OK, we build the real RTP packet... */
	RtpPacket *pkt = new RtpPacket( (uint8_t*)buf, size );
	pkt->set_ssrc( m_ssrc );
	pkt->set_payload_type( m_payload_type );
	pkt->set_sequence( m_seq++ );
	pkt->set_timestamp( timestamp );
	delete pkt;

	if ( ! send_pkt( buf, size ) ) {
		/* Problems sending packet.. probably the client closed
		 * the connection..
		 */
		return false;
	}

	/* *************************************************** */
	/*! Here we check for events or things to do... */

	/* Schedule the times to play packets as an absolute offset from
	 * our start time, rather than a relative offset from the initial
	 * packet.  (We're less vulnerable to drifting clocks that way).
	 * Alternative version based on timestamps and RTP clock..
	 */
	play_time = m_start_time + ((double)(timestamp - m_base_timestamp)/ m_rtp_clock);
	// printf("play_time: %f - difference: %f sec.\n", play_time,
	//		((double)(timestamp - m_base_timestamp)/ m_rtp_clock));

	while (gettimeofday(&now_tv, NULL), (now = tv2dbl(now_tv)) < play_time) {

		int event = 0;
		int retval;
		double timeout;
		struct timeval timeout_tv;
		fd_set sockets;

		if (	m_event_queue != NULL && 
			m_event_queue->time < play_time ) {
			event = 1;
			timeout = m_event_queue->time - now;
		} else {
			event = 0;
			timeout = play_time - now;
		}
		if ( timeout < 0 )
			timeout = 0;
		timeout_tv = dbl2tv( timeout );

		FD_ZERO( &sockets );
		FD_SET( m_rtp_sock, &sockets );
		FD_SET( m_rtcp_sock, &sockets );

		int max_fd = ((m_rtp_sock > m_rtcp_sock) ? m_rtp_sock : m_rtcp_sock ) + 1;

		retval = select(max_fd, &sockets, NULL, NULL, &timeout_tv);
		if ( retval < 0 ) {
			perror("select");
			exit(1);
		} else if ( retval > 0 ) { // There are some events...
			if (FD_ISSET(m_rtp_sock, &sockets)) {
				/* There's an RTP packet to be read... 
				 * We should receive, validate and .. trash it..
				 */
				receive_rtp( (void*)m_buf );
			}
			
			if (FD_ISSET(m_rtcp_sock, &sockets)) {
				receive_rtcp();
			}
		}  else { /* retval == 0, select timed out */
			if (event) {
				gettimeofday( &now_tv, NULL );
				now = tv2dbl( now_tv );
				while ( m_event_queue != NULL &&
					m_event_queue->time <= now ) {
					/* There is a pending RTCP packet to send) */
					send_rtcp();
				}
			} else
				break;  /* Time for the next packet */
		}
	}
	/* **************************************************** */
	return true;
}

uint16_t RtpSession::receive_rtp_packet(void *buf )
{
	struct timeval start_time_tv;

	if ( ! m_preceived ) {
		/* This is the 1th packet that we receive-
		 * Here we should send an RTCP SDES packet with all the 
		 * infos...
		 */
		gettimeofday(&start_time_tv, NULL);
		m_start_time = tv2dbl(start_time_tv);
#if 0		
		/*! Build the SDES CNAME packet */
		uint16_t s = 10 + strlen( m_cname );
		/* Align to 32 bit boundary .. */
		s = (uint16_t)ceil( (double)s /4 ) * 4;
		
		m_sdes_pkt = (rtcp_t *)calloc( s , 1 );
		RtcpPacket *p = new RtcpPacket( m_sdes_pkt, s );
		p->set_version( 2 );
		p->set_padding( 0 );
		p->set_count( 1 );
		p->set_type( RTCP_SDES );
		p->set_length( (uint16_t)s/4 - 1 );
		m_sdes_pkt->r.sdes.src = htonl( m_ssrc );
		m_sdes_pkt->r.sdes.item->type = RTCP_SDES_CNAME;
		m_sdes_pkt->r.sdes.item->length = strlen( m_cname );
		memcpy( m_sdes_pkt->r.sdes.item[0].data, m_cname, strlen(m_cname) );
		m_sdes_pkt_size = s;

		/*! Schedule an RTCP RR.. */
		event_t *e = (event_t*)malloc( sizeof( event_t ) );
 		e->type = RTCP_RR;
		e->time = m_start_time + 5.0;
		e->next = NULL;
		insert_event( e );
		send_rtcp();
#endif
	}
	
	int event = 0;
	int retval;
	double timeout, now;
	struct timeval timeout_tv, now_tv;
	fd_set sockets;

	/*! Here we check for events or things to do... */
	while ( 1 ) {

		gettimeofday(&now_tv, NULL);
		now = tv2dbl(now_tv);

		if (    m_event_queue != NULL ) {
			event = 1;
			timeout = m_event_queue->time - now;
		} else {
			event = 0;
			timeout = 0.5; /* Arbitrary value.. to be trimmed.. */
		}
		if ( timeout < 0 )
			timeout = 0;
		timeout_tv = dbl2tv( timeout );

		FD_ZERO( &sockets );
		FD_SET( m_rtp_sock, &sockets );
		FD_SET( m_rtcp_sock, &sockets );

		int max_fd = ((m_rtp_sock > m_rtcp_sock) ? m_rtp_sock : m_rtcp_sock ) + 1;

		retval = select(max_fd, &sockets, NULL, NULL, &timeout_tv);
		if ( retval < 0 ) {
			perror("select");
			return false;
		} else if ( retval > 0 ) { // There are some events...
			if (FD_ISSET(m_rtp_sock, &sockets)) {
				/* There's an RTP packet to be read... 
				 */
				return receive_rtp( buf );
			}
			
			if (FD_ISSET(m_rtcp_sock, &sockets)) {
				receive_rtcp();
			}
		}  else { /* retval == 0, select timed out */
			if (event) {
				gettimeofday( &now_tv, NULL );
				now = tv2dbl( now_tv );
				while ( m_event_queue != NULL &&
					m_event_queue->time <= now ) {
					/* There is a pending RTCP packet to send) */
					send_rtcp();
				}
			} 
		}
	}
}

void RtpSession::close()
{
	printf("RtpSession::close()\n");

	RtcpPacket *p;
	struct timeval tv;
	gettimeofday(&tv, NULL);
	ntp_t ntp = timeval_to_ntp( tv );	
	
	p = new RtcpPacket( RTCP_SR );
	p->set_count( 0 );
	p->set_sr_ssrc( m_ssrc );
	p->set_sr_ntp_sec( ntp.secs );
	p->set_sr_ntp_frac( ntp.frac );
	p->set_sr_rtp_ts( m_last_timestamp );
	p->set_sr_psent( m_psent );
	p->set_sr_osent( m_osent );
	
	send_rtcp_pkt( p , true ); /* Send the BYE packet */
	
	if ( m_rtp_sock )
		::close( m_rtp_sock );
	if ( m_rtcp_sock )
		::close( m_rtcp_sock );
	m_rtp_sock = 0;
	m_rtcp_sock = 0;
}

double RtpSession::compute_rtcp_interval( )
{
	double t; /* interval */

	#define MEMBERS 2

	double rtcp_bw = 0.05 * m_bandwidth;
	rtcp_bw *= RTCP_SENDER_BW_FRACTION;
	t = m_rtcp_avg_size * 8 * MEMBERS / rtcp_bw;
	if (t < RTCP_MIN_TIME) t = RTCP_MIN_TIME;

	/*
	 * To avoid traffic bursts from unintended synchronization with
	 * other sites, we then pick our actual next report interval as a
	 * random number uniformly distributed between 0.5*t and 1.5*t.
	 */
	t = t * (drand48() + 0.5);
	t = t / COMPENSATION;
	// printf("RTCP delay: %f\n", t);
	return t;
}

bool RtpSession::send_rtp_pkt(RtpPacket *pkt)
{
	uint32_t size = pkt->to_buffer( m_buf );
	if ( ! size )
		return false;

	if ( send( m_rtp_sock, m_buf, size, MSG_DONTWAIT ) == -1 ) {
		return false;
	}
	
	++ m_psent;
	m_osent += pkt->get_payload_size();
	m_last_timestamp = pkt->timestamp();
	
	return true;
}

bool RtpSession::send_pkt(void *buf, uint32_t size)
{

	if ( send( m_rtp_sock, buf, size, MSG_DONTWAIT ) == -1 ) {
		return false;
	}
	
	m_osent += size - RTP_HEADER_LEN;
	m_last_timestamp = m_prev_timestamp;
	++ m_psent;
	return true;
}

bool RtpSession::send_rtcp_pkt(RtcpPacket *pkt, bool bye_packet)
{
	uint32_t size = pkt->to_buffer( m_buf );
	if ( !size )
		return false;
	
	/* Attach SDES CNAME */
	memcpy( m_buf + size, m_sdes_pkt, m_sdes_pkt_size );
	size += m_sdes_pkt_size;

	if ( bye_packet ) {
		rtcp_t* bye_pkt = (rtcp_t *)malloc( 8 );
		RtcpPacket *p = new RtcpPacket( bye_pkt, 8 );
		p->set_version( 2 );
		p->set_padding( 0 );
		p->set_count( 1 );
		p->set_type( RTCP_BYE );
		p->set_length( (uint16_t)1 );
		bye_pkt->r.bye.src[0] = htonl( m_ssrc );
		memcpy( m_buf + size, bye_pkt, 8);
		size += 8;
	}

	if ( send( m_rtcp_sock, m_buf, size, MSG_DONTWAIT ) == -1 ) {
		return false;
	}

	++ m_rtcp_psent;
	m_rtcp_avg_size = ( (m_rtcp_avg_size * (m_rtcp_psent-1)) + size )
				/ m_rtcp_psent;
	// printf("m_rtcp_avg_size 1 : %f\n", m_rtcp_avg_size);

	/// process_rtcp_packet( m_buf, size );

	return true;
}

uint16_t RtpSession::receive_rtp(void *buf)
{
	int32_t r;
	r = recv( m_rtp_sock, buf, MAX_UDP_LEN, 0 );
	if ( r == -1 ) {
		perror("receive_rtp");
		this->close();
		return 0;
	}
	return r;
}

bool RtpSession::receive_rtcp()
{
	int32_t r;
	r = recv( m_rtcp_sock, m_buf, MAX_UDP_LEN, 0 );
	if ( r == -1 ) {
		perror("receive_rtcp");
		return false;
	}

	++ m_rtcp_psent;
	m_rtcp_avg_size = ( (m_rtcp_avg_size * (m_rtcp_psent-1)) + r )
				/ m_rtcp_psent;
	// printf("m_rtcp_avg_size 2 : %f\n", m_rtcp_avg_size);

	// process_rtcp_packet( m_buf, r );
	return true;
}

void RtpSession::send_rtcp()
{
	RtcpPacket *p;
	struct timeval tv;
	gettimeofday(&tv, NULL);
	ntp_t ntp = timeval_to_ntp( tv );
	
	uint8_t type = m_event_queue->type;

	switch ( m_event_queue->type ) {

	case RTCP_SR:  /* We want to send an RTCP Sender Report. */
		p = new RtcpPacket( RTCP_SR );
		p->set_count( 0 );
		p->set_sr_ssrc( m_ssrc );
		p->set_sr_ntp_sec( ntp.secs );
		p->set_sr_ntp_frac( ntp.frac );
		p->set_sr_rtp_ts( m_last_timestamp );
		p->set_sr_psent( m_psent );
		p->set_sr_osent( m_osent );
		break;
	
	case RTCP_RR:
		p = new RtcpPacket( RTCP_RR );
		p->set_count( 1 );
	
	default: p = NULL;
	}

	if ( ! p )
		return;

	send_rtcp_pkt( p );

	/* Advance the queue */
	event_t *next = (event_t *)m_event_queue->next;
	free( m_event_queue );
	m_event_queue = next;

	/* Schedule a new RTCP packet */
	event_t *e = (event_t*)malloc( sizeof( event_t ) );
 	e->type = type;
	e->time = tv2dbl( tv ) + compute_rtcp_interval();
	e->next = NULL;
	insert_event( e );
}

bool RtpSession::create_sockets(uint16_t base_port)
{
	struct sockaddr_in si_rtp, si_rtcp;
	int32_t retval;
	uint16_t port;

	while ( 1 ) {
		if ( base_port == 0 ) {
			srandom( time(NULL) );
			port = MIN_UDP_PORT +
				2 * (uint16_t)( (double)random()/2 /0xFFFFFFFF  *
				(MAX_UDP_PORT - MIN_UDP_PORT)  );
		} else port = base_port;

		if ( m_rtp_sock ) {
			::close( m_rtp_sock );
			m_rtp_sock = 0;
		}
		if ( m_rtcp_sock ) {
			::close( m_rtcp_sock );
			m_rtcp_sock = 0;
		}

		/**** RTP socket ****/
		m_rtp_sock = socket( AF_INET, SOCK_DGRAM, 0);
		if ( m_rtp_sock == -1 ) {
			fprintf(stderr, "Failed to create RTP socket...\n");
			return false;
		}

		memset((uint8_t *) &si_rtp, sizeof(struct sockaddr_in), 0);
		si_rtp.sin_family = AF_INET;
		si_rtp.sin_port = htons( port );
		si_rtp.sin_addr.s_addr = htonl( INADDR_ANY );

		retval = bind(m_rtp_sock, (struct sockaddr*)&si_rtp, sizeof(struct sockaddr_in));
		if ( retval == -1 ) {
			if ( base_port ) {
				perror("Bind RTP ");
				return false;
			}
			continue;
		}

		m_rtp_local_port = ntohs( si_rtp.sin_port );
		printf("RTP local port: %u\n", m_rtp_local_port);

		/**** RTCP socket ****/
		m_rtcp_sock = socket( AF_INET, SOCK_DGRAM, IPPROTO_UDP);
		if ( m_rtcp_sock == -1 ) {
			fprintf(stderr, "Failed to create RTCP socket...\n");
			return false;
		}
		memset((uint8_t *) &si_rtcp, sizeof(struct sockaddr_in), 0);
		si_rtcp.sin_family = AF_INET;
		si_rtcp.sin_port = htons( m_rtp_local_port + 1 );
		si_rtcp.sin_addr.s_addr = htonl( INADDR_ANY );

		retval = bind(m_rtcp_sock, (struct sockaddr*)&si_rtcp, sizeof(struct sockaddr_in));
		if ( retval == -1 ) {
			if ( base_port ) {
				perror("Bind RTCP ");
				return false;
			}
			continue;
		}
		printf("RTCP local port: %u\n", ntohs( si_rtcp.sin_port ));

		return true;
	}
}

bool RtpSession::setup_peer(const char* peer_addr, uint16_t peer_port)
{
	struct hostent *host;
	struct in_addr h_addr;
	struct sockaddr_in addr;
	
	if ( peer_addr == NULL && peer_port == 0 ) {
		/* The setup_peer() will be called after.. */
		return true;
	}
	
	host = gethostbyname( peer_addr );
	if ( host == NULL ) {
		perror("RtpSession::setup_peer()");
		return false;
	}
	h_addr = *( (struct in_addr *) (host->h_addr) );
	
	addr.sin_family = AF_INET;
	addr.sin_addr = h_addr;
	addr.sin_port = htons( peer_port );
	
	if ( connect( m_rtp_sock, (struct sockaddr*)&addr, sizeof(struct sockaddr) ) == -1 ) {
		perror("Connect to RTP port");
		return false;
	}
	
	/* We increment the port number for RTCP */
	addr.sin_port = htons( peer_port + 1);
	if ( connect( m_rtcp_sock, (struct sockaddr*)&addr, sizeof(struct sockaddr) ) == -1 ) {
		perror("Connect to RTCP port");
		return false;
	}
	
	return true;
}

void RtpSession::set_ssrc(uint32_t ssrc)
{
	if ( ssrc == 0 )
		return;
	m_ssrc = ssrc;
}

void RtpSession::set_cname(const char* s)
{
	char *hostname = (char *)malloc( 50 );
	if ( gethostname(hostname, 50) == -1 ) {
		strncpy(hostname, "localhost", 50);
	}
	
	if ( m_cname )
		free( m_cname );
	m_cname = (char *)malloc( strlen( s ) + strlen( hostname ) + 2 );
	snprintf(m_cname, strlen( s ) + strlen( hostname ) + 2, "%s@%s", s, hostname);
	
	printf("CNAME: '%s'\n", m_cname);	
	
	free( hostname );
}

uint16_t RtpSession::get_rtp_port()
{
	return m_rtp_local_port;
}

uint16_t RtpSession::get_rtcp_port()
{
	if ( ! m_rtp_local_port )
		return 0;
	else return (m_rtp_local_port + 1);
}







/** LOG **
 *
 * $Log: rtp_session.cpp,v $
 * Revision 1.3  2003/11/17 16:14:12  mat
 * make-up
 *
 *
 */

