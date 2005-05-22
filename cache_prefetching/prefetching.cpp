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

#include <string.h>
#include <assert.h>

#include "prefetching.h"
#include "dbg.h"
#include "rtsp_session.h"
#include "cache_items.h"

static Prefetching *g_prefetching;
static pthread_mutex_t g_prefetching_mutex;

Prefetching::Prefetching() :
	m_time_range( NULL ),
	m_segment( NULL ),
	m_stream_url( NULL )
{
}

Prefetching::~Prefetching()
{
}

void Prefetching::set_stream_url(const char* url)
{
	assert( url );
	m_stream_url = strdup( url );
}

void Prefetching::run()
{
	assert( m_segment );
	assert( m_time_range );
	assert( m_stream_url );
	
	m_segment->parent()->lock_write();
	dbg("Starting prefetching...\n\n");
	
	m_rtsp = new RtspSession( m_stream_url );
	m_rtp = new RtpSession();
	m_rtp->setup( 0, "rtsp_proxy", NULL, 0, 
		m_segment->getSDPInfo()->payload_type, 
		m_segment->getSDPInfo()->rtp_clock );
	m_rtp->set_bandwidth( m_segment->getSDPInfo()->avg_bitrate );
	
	m_rtsp->start( m_rtp->get_rtp_port(), m_time_range );
	m_rtp->setup_peer( m_rtsp->server_addr(), m_rtsp->server_rtp_port() );
	
	/* Receive RTP packets.. */
	uint8_t buf[ MAX_UDP_LEN ];
	uint8_t *b;
	uint32_t size;
	bool first_time = true;
		
	while ( 1 ) {
		size = m_rtp->receive_rtp_packet( buf );
		if ( ! size )
			break;
		/* Write packet to disk */
		b = (uint8_t*)calloc( size, 1 );
		memcpy( b, buf, size );
		/* Add packet to the given segment and adjust the 
		 * timestamp to be continous with other packets. 
		 */
		m_segment->add_packet( b, size, true, first_time );
		first_time = false;
	}
}

void Prefetching::end()
{
	pthread_cancel( m_thread );

	m_rtsp->end();
	
	m_segment->parent()->unlock_write();
	
	delete m_rtsp;
	delete m_time_range;
	delete m_rtp;
	free( m_stream_url );
	delete this;
}

static void * thread_main(void *p)
{
	Prefetching *pf = g_prefetching;
	pthread_mutex_unlock( &g_prefetching_mutex );

	pf->run();
	pthread_exit(0);
}

void Prefetching::start()
{
	pthread_mutex_lock( &g_prefetching_mutex );
	g_prefetching = this;

	pthread_attr_t attr;
	pthread_attr_init(&attr);
	pthread_create( &m_thread, &attr , &thread_main, NULL);
	pthread_detach( m_thread );
}

/** LOG **
 *
 * $Log: prefetching.cpp,v $
 * Revision 1.3  2003/11/17 16:13:51  mat
 * make-up
 *
 *
 */

