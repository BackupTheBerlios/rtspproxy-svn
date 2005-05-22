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

#include "cache_transport.h"
#include "cache_segment.h"
#include <signal.h>

/*! We use a global pointer to CachePlay instances..
 *  so we _have_ to prevent race conditions accessing
 *  this pointer..
 */
static CacheTransport *g_cache_transport;
static pthread_mutex_t g_cache_transport_mutex;

CacheTransport::CacheTransport(int handler)
	: m_bit_rate( 0 ),
	  m_range( NULL ),
	  m_exit( false )
{
	m_handler = handler;
}

CacheTransport::~CacheTransport()
{

}

u_int8_t CacheTransport::setup()
{
	return 0;
}

size_t CacheTransport::send_packet( void *buf, size_t size, u_int8_t pt, u_int32_t timestamp)
{
	return 0;
}

void CacheTransport::close()
{
	if ( ! m_thread )
		return;
	pthread_cancel( m_thread );
}

void CacheTransport::set_segment(CacheSegment* seg) 
{
	m_segment = seg;
	/*! Here we have to decide if we have to use the guessed
	 *  bitrate or the bitrate stated in the SDP description.
	 */
	u_int32_t avg_bit_rate, guess_bit_rate;
	avg_bit_rate = m_segment->getSDPInfo()->avg_bitrate;
	guess_bit_rate = m_segment->get_guessed_bitrate();
	
	dbg("avg_bit_rate: %d  -  guess_bit_rate: %d\n", avg_bit_rate, guess_bit_rate);
	
	if ( !avg_bit_rate ) {
		/* If we don't have a SDP bitrate description,
		 * we will use our estimated bitrate..
		 * better than nothing..
		 */
		m_bit_rate = guess_bit_rate;
	} else if ( (2 * avg_bit_rate) < guess_bit_rate ) {
		/* The avg bitrate could be wrong..
		 * so we'll use guessed value.
		 */
		m_bit_rate = guess_bit_rate;
	} else {
		m_bit_rate = avg_bit_rate;
	}
	
	dbg("m_bit_rate: %d\n", m_bit_rate);
	
	m_range = m_segment->getSDPInfo()->range;
}

char *CacheTransport::get_range()
{
	if ( !m_range )
		return "npt=0-";

	return m_range;
}

void CacheTransport::run(void)
{
	dbg("WARNING: the run() method should be re-implemented!!\n");
}

static void * thread_main(void *p)
{
	CacheTransport *ct = g_cache_transport;
	pthread_mutex_unlock( &g_cache_transport_mutex );

	ct->run();
	pthread_exit(0);
}

void CacheTransport::start()
{
	pthread_mutex_lock( &g_cache_transport_mutex );
	g_cache_transport = this;

	pthread_attr_t attr;
	pthread_attr_init(&attr);
	pthread_create( &m_thread, &attr , &thread_main, NULL);
	pthread_detach( m_thread );
}





/** LOG **
 *
 * $Log: cache_transport.cpp,v $
 * Revision 1.3  2003/11/17 16:13:47  mat
 * make-up
 *
 *
 */

