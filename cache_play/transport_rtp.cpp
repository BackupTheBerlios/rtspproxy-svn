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
 
#include <unistd.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <dbg.h>

#include "tlist.h"
#include "transport_rtp.h"
#include "cache_segment.h"
#include "config_parser.h"
#include "cache_play.h"
#include "rtp_session.h"

#ifdef HAVE_CONFIG_H
#include "../config.h"
#endif

/*! Convert a struct timeval to a double */
#define tv2dbl(tv) ((tv).tv_sec + (tv).tv_usec / 1000000.0)

TransportRTP::TransportRTP() :
	m_local_port( 0 ),
	m_prev_timestamp( 0 )
{
	m_session = new RtpSession();
}

TransportRTP::~TransportRTP()
{
	delete m_session;
}

uint8_t TransportRTP::setup()
{
	int r = m_session->setup( 
		0, "rtsp_proxy", m_client_addr, m_client_port,
		m_segment->getSDPInfo()->payload_type,
		m_segment->getSDPInfo()->rtp_clock );

	if ( ! r ) {
    		dbg("TransportRTP: Cannot initiate the RTP session..\n" \
        	"client_addr='%s' - client_port='%u'\n" \
       		     "payload_type='%u' - rtp_clock='%u'\n",
			m_client_addr, m_client_port, m_segment->getSDPInfo()->payload_type,
			m_segment->getSDPInfo()->rtp_clock
            );
		return false;
    }

	m_session->set_bandwidth( m_segment->getSDPInfo()->avg_bitrate );
		
	dbg("SSRC: %u - TS: %u - Seq: %u\n", 
		m_session->get_ssrc(), m_session->get_base_timestamp(), 
		m_session->get_base_seq() );
	dbg("\n\n");

	dbg("The RTP session was successfully created...\n");

	return true;
}

void TransportRTP::run(void)
{
	uint32_t timestamp, timestamp_offset;
	int32_t result;
	uint32_t size;
	uint8_t * buf;
	uint32_t offset;
	
	dbg("--> Starting playback from cached stream <--\n");
	
	m_segment->parent()->lock_read();
	
	char *file_name = (char *)m_segment->get_file_name();
	FILE* file = fopen( file_name, "r" );
	if ( file == NULL ) {
		perror("Failed to open file for reading ... \n");
		close();
		exit(1);
	}
	
	packet_list_t::Iterator read_ptr = m_segment->get_packet_list()->Begin();

	m_prev_timestamp = (*read_ptr)->timestamp;

	while ( 1 ) {
		if ( !read_ptr || m_exit ) {
			fclose( file );
			m_session->close();
			m_parent->send_teardown();
			m_segment->parent()->unlock_read();
			return;
		}

		size = (*read_ptr)->size;
		timestamp = (*read_ptr)->timestamp;

		if ( size < 0 || size > MAX_UDP_LEN ) {
			dbg("Warning.. size of packet is unreliable..\n");
			++ read_ptr;
			continue;
		}

		buf = (uint8_t*)calloc( 1, size);

#warning Altro lock() disabilitato
		// m_segment->lock();
		offset = fseek( file, (*read_ptr)->offset, SEEK_SET );
		if ( offset == -1 ) {
			/// m_segment->unlock();
			perror("TransportRTP::run() fseek");
			fclose( file );
			m_session->close();
			m_parent->send_teardown();
			m_segment->parent()->unlock_read();
			return;
		}
		
		result = fread( buf, size, 1, file );
		// m_segment->unlock();
		
		if ( result == -1 ) {
			dbg("TransportRTP::run() : Error reading from file.\n");
			fclose( file );
			m_session->close();
			m_parent->send_teardown();
			m_segment->parent()->unlock_read();
			return;
		}
		else if ( result == 0 ) {
			dbg("TransportRTP::run() : End Of File.\n");
			fclose( file );
			m_session->close();
			m_parent->send_teardown();
			m_segment->parent()->unlock_read();
			return;
		}

		/* We care about the difference.. */
		timestamp_offset = timestamp - m_prev_timestamp;
		m_prev_timestamp = timestamp;

		result = m_session->send_rtp_packet( buf, size, timestamp_offset );
		if ( result == 0 ) {
			dbg("--- send_rtp_packet: error ---\n");
			fclose( file );
			m_session->close();
			m_segment->parent()->unlock_read();
			return;
		}
		
		delete( buf );
		
		++ read_ptr;
	}
}

void TransportRTP::close()
{
	dbg("TransportRTP::close()\n");

	/* This way we tell to the thread that is time to leave... */
	m_exit = true;
	CacheTransport::close();
}







/** LOG **
 *
 * $Log: transport_rtp.cpp,v $
 * Revision 1.3  2003/11/17 16:13:47  mat
 * make-up
 *
 *
 */

