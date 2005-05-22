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
 
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <dbg.h>

#include "cache_segment.h"
#include "time_range.h"
#include "rtp_packet.h"
#include "rtspproxy.h"
#include "time_range.h"

/*! Time amount to guess the bandwidth in seconds. */
#define PERIOD 5

/*! Referece to a CacheSegment instance */
static CacheSegment * s_cache_segment;
static pthread_mutex_t s_cache_segment_mutex;

static void * thread_main(void *p);

CacheSegment::CacheSegment(CacheItem *parent) :
	m_packet_list( NULL ),
	m_size( 0 ),
	m_num_packets( 0 ),
	m_parent( parent ),
	m_time_range( NULL ),
	m_guessed_bitrate( 0 ),
	m_first_time( true ),
	m_prev_offset( 0 ),
	m_prev_timestamp( 0 ),
	m_ts_offset( 0 )
{
	m_packet_list = new packet_list_t();
    	m_fd = NULL;

	/* We create a new file for the segment.  */
	snprintf(m_file_name, 100, "%s/%u", global_config.cache_dir, m_parent->id() );
	dbg("\nCacheSegment file: '%s'\n", m_file_name);

	this->open( true );
	
	m_time_range = new TimeRange();
}

CacheSegment::~CacheSegment()
{

	fclose( m_fd );
	m_fd = NULL;

	if ( unlink(m_file_name) < 0 )
		dbg("Failed to unlink '%s'.\n", m_file_name);

	/* The packets are directly stored into the list */
	while ( m_packet_list->GetCount() ) {
		packet_t *p = m_packet_list->RemoveHead();
		delete p;
	}

	if ( m_packet_list )
		delete m_packet_list;
	if ( m_time_range )
		delete m_time_range;

	if ( m_sdp_info.range )
		free( m_sdp_info.range );
	if ( m_sdp_info.length )
		free( m_sdp_info.length );
	if ( m_sdp_info.mimetype )
		free( m_sdp_info.mimetype );
}

void CacheSegment::open(bool truncate)
{
	if ( truncate )
		m_fd = fopen( m_file_name, "w+" );
	else m_fd = fopen( m_file_name, "a" );

	if ( m_fd == NULL ) {
		perror(" Failed to create file ");
		exit(-1);
	}
}

void CacheSegment::add_packet( const void *buf, uint16_t size, 
			bool adjust_ts, bool set_offset )
{
	assert( buf );
	assert( size );
	uint32_t timestamp = 0;
	RtpPacket *rtp;

	if ( m_fd == NULL )
		this->open( false);

	if ( m_first_time ) {
		/* This is the 1th packet */
		pthread_mutex_lock( &s_cache_segment_mutex );
		s_cache_segment = this;

		pthread_attr_t attr;
		pthread_attr_init(&attr);
		pthread_create( &m_thread, &attr , &thread_main, NULL);
		pthread_detach( m_thread );

		m_first_time = false;
	}

	if ( IS_RTP( m_transport_type ) ) {
		rtp = new RtpPacket( (uint8_t*)buf, size );
		timestamp = rtp->timestamp();
		// dbg("payload: %u - ts: %u - size: %u \n",
		//    rtp->payload_type(), timestamp, size);
	} else {
		dbg("Unknown transport type %u... cannot cache packets...\n",
			 m_transport_type);
		return;
	}

    #warning Locking disabilitato
    /// printf("locking\n");
	// /this->lock();

	/* Go to EOF */
	fseek( m_fd, 0L, SEEK_END );

	if ( fwrite( buf, 1, size, m_fd ) < (int32_t)size ) {
		perror("CacheSegment::add_packet() : Error writing to file.");
		return;
	}
	/// this->unlock();
    /// printf("unlocked\n");

	free( (uint8_t*)buf );
	delete rtp;

	packet_t *p = (packet_t *)malloc( sizeof( packet_t ) );

	if ( set_offset )
		m_ts_offset = timestamp - m_prev_timestamp;

	if ( adjust_ts ) {
		p->timestamp = timestamp - m_ts_offset;
	} else p->timestamp = timestamp;

	// dbg("Ts: %u\n", p->timestamp );

	p->size = size;
	p->offset = m_prev_offset;
	m_packet_list->InsertTail( p );

	m_prev_offset += size;
	m_prev_timestamp = p->timestamp;
	m_size += size;
	++m_num_packets;
}

void CacheSegment::close()
{
	dbg("CacheSegment close()\n");
	fclose( m_fd );
	m_fd = NULL;
}

FILE* CacheSegment::get_file_desc()
{
	if ( m_fd )
		return m_fd;

	m_fd = fopen( m_file_name, "r" );
	if ( m_fd == NULL )
		perror("Failed to open file for reading ... \n");

	return m_fd;
}

void CacheSegment::set_transport_type(u_int8_t type)
{
	m_transport_type = type;
	/* For the cached item we only care wheter the packets are
	 * RTP or RDT, and so we ignore whether they are sent in UDP
	 * or TCP.
	 */
	m_parent->set_transport( type & ( TRANSPORT_RTP | TRANSPORT_RDT) );
}

void CacheSegment::setSDPInfo( sdp_info_t* sdp )
{
	m_sdp_info.range = NULL;
	m_sdp_info.length = NULL;
	m_sdp_info.mimetype = NULL;
	m_sdp_info.payload_type = 0;
	m_sdp_info.rtp_clock = 0;
	m_sdp_info.avg_bitrate = 0;
	m_sdp_info.track_id = 0;

	if ( sdp->range != NULL ) m_sdp_info.range = strdup( sdp->range );
	if ( sdp->length != NULL ) m_sdp_info.length = strdup( sdp->length );
	if ( sdp->mimetype != NULL ) m_sdp_info.mimetype = strdup( sdp->mimetype );
	m_sdp_info.payload_type = sdp->payload_type;
	m_sdp_info.rtp_clock = sdp->rtp_clock;
	m_sdp_info.avg_bitrate = sdp->avg_bitrate;
	m_sdp_info.track_id = sdp->track_id;
}

TimeRange *CacheSegment::time_range()
{
	/* We re-compute the time range... */
	double start = TimeRange( m_sdp_info.range ).start();
	double end;
	uint32_t ts_start, ts_end;
	
	/* We compute the time of the last packet we cached
	 * using the last timestamp and the rtp-clock.
	 */ 
	packet_list_t::Iterator it = m_packet_list->Begin();
	ts_start = (*it)->timestamp;
	for ( ; it ; it++ ) 
		ts_end = (*it)->timestamp;
	end = (double)(ts_end - ts_start) / m_sdp_info.rtp_clock;
	
	m_time_range->set_start( start );
	m_time_range->set_end( end );
	return m_time_range;
}

void CacheSegment::update_bandwidth_data()
{
	/* We guess the bitrate of the stream looking at the
	 * number of bytes received in (PERIOD) s.
	 * get_size() returns the number of _bytes_ so long
	 * received, and we want to obtain the (bit / s) value
	 */
	m_guessed_bitrate = get_size() * 8 / PERIOD;
	dbg("########## Guessed Bitrate: %d #############\n", m_guessed_bitrate);

	pthread_exit(0);
}

static void * thread_main(void *p)
{
	CacheSegment *ref = s_cache_segment;
	pthread_mutex_unlock( &s_cache_segment_mutex );

	/* Wait for PERIOD seconds using select
	 * to achieve a good precision..
	 */
	struct timeval timeout;
	timeout.tv_sec = PERIOD;
	timeout.tv_usec = 0;
	select( 0, NULL, NULL, NULL, &timeout );

	ref->update_bandwidth_data();

	pthread_exit(0);
}



/** LOG **
 *
 * $Log: cache_segment.cpp,v $
 * Revision 1.3  2003/11/17 16:13:45  mat
 * make-up
 *
 *
 */

