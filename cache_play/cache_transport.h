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
 
#ifndef CACHE_TRANSPORT_H
#define CACHE_TRANSPORT_H

#include <types.h>
#include <pthread.h>

#include <rtp_session.h>

class CacheSegment;
class CachePlay;

/*!
 * This is the base class that defines the interface for
 * the transports.
 * The effective implementations are done in TransportTCP,
 * TransportRTP, TransportRDT ...
 */
class CacheTransport
{
 public:
	/*! Constructor */
	CacheTransport(int handler=0);

	/*! Destructor  */
	virtual ~CacheTransport();

	/*!  */
	virtual uint8_t setup();

	/*! Close the connection. */
	virtual void close();

	/*! Sends a packet to the client. */
	virtual size_t send_packet(void *buf, size_t size,
		uint8_t pt, uint32_t timestamp);

	virtual uint16_t get_port() { return 0; }

	void set_client_addr(char *addr) {m_client_addr = addr;}

	void set_client_port(uint16_t port) {m_client_port = port;}

	void set_segment(CacheSegment* seg);
	
	char *get_range();
	
	virtual void run();
	
	/*! Starts the execution in a new thread, and calls the run() method.
	 */
	void start();

	void set_parent(CachePlay *p) {m_parent = p;}
	
 protected:
	/*! This the handler of transport..
	 *  (socket descriptor)
	 */
	int m_handler;

	char *m_client_addr;

	uint16_t m_client_port;

	/*! Reference to the segment. */
	CacheSegment *m_segment;

	CachePlay *m_parent;

	uint32_t m_bit_rate;

	char *m_range;

	pthread_t m_thread;

	bool m_exit;
};


#endif

/** LOG **
 *
 * $Log: cache_transport.h,v $
 * Revision 1.3  2003/11/17 16:13:47  mat
 * make-up
 *
 *
 */

