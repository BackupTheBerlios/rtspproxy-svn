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
 
#ifndef CACHE_PLAY_H
#define CACHE_PLAY_H

#include "rtspmsg.h"
#include "cache_items.h"
#include "cache_segment.h"
#include "cache_transport.h"

#include <pthread.h>

class CClientCnx;
class Prefetching;

class CachePlay
{
 public:
	/*!
	 * Constructor
	 * @param item Reference to a cached resource.
	 */
	CachePlay(CClientCnx *cnx, CacheItem *item);

	/*! Destructor  */
	~CachePlay();

	/*!
	 * Handle the SETUP request.
	 */
	void setup(CRtspRequestMsg *msg);

	/*!
	 * Handle the PLAY request.
	 */
	void play(CRtspRequestMsg *msg, CachePlay *cp2=NULL);

	/*!
	 * Handle the TEARDOWN request.
	 */
     	void teardown(CRtspRequestMsg *msg);

	/*! Sends a TEARDOWN msg to the client. */
	void CachePlay::send_teardown();

	/*! */
	CacheSegment* get_segment() {return m_segment;}

	CacheTransport* get_transport() {return m_cache_transport;}

	CString get_url() {return m_url;}

 private:
	/*!
	 * Generate a 8 bytes random id to be used as RTSP session id.
	 * 8 bytes is the lenght hinted in RTSP Rfc.
	 */
	void gen_session_id();

	/*! Extract the transport type from the Transport header sent
	 *  by the client.
	 */
	uint8_t parse_transport(const char* str);

	/*! Extract the client port(s) from the transport header. */
	uint16_t get_client_port(const char* str);

	uint8_t check_transport(const char* str);

	/*! Reference to the cached item. */
	CacheItem *m_item;

	/*! URL received in SETUP command. */
	CString m_url;

	/*! */
	CClientCnx *m_cnx;

	uint8_t m_transport;

	char * m_transport_str;

	char * m_session_id;

	/*! */
	CacheTransport * m_cache_transport;

	/*! Client Address */
	char *m_client_addr;

	/*! Client Port */
	uint16_t m_client_port;

	/*! File stream associated with the segment when reading. */
	FILE *m_file_read;

	/*! Pointer to current packet when reading. */
	packet_list_t::Iterator m_read_ptr;

	pthread_t m_thread;

	CacheSegment *m_segment;
	
	Prefetching *m_pf;
};

typedef TDoubleList< CachePlay* > cache_play_list_t;


#endif

/** LOG **
 *
 * $Log: cache_play.h,v $
 * Revision 1.3  2003/11/17 16:13:47  mat
 * make-up
 *
 *
 */

