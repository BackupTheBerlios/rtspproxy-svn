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

#ifndef TRANSPORT_TCP_H
#define TRANSPORT_TCP_H

#include <sys/types.h>

#include "cache_transport.h"

/*!
 * This is the implementation of the class that play back
 * the packets stored in cache, using teh RTP/TCP protocol.
 */
class TransportTCP : public CacheTransport
{
 public: 
	/*! Constructor */
	TransportTCP();

	/*! Destructor  */
	~TransportTCP();

	u_int8_t setup();
	
	void close();

	/*! Sends a packet to the client. */
	size_t send_packet(void *buf, size_t size);
};


#endif


