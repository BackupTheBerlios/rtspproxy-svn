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
#include <unistd.h>
#include "../libapp/dbg.h"

#include "transport_tcp.h"

TransportTCP::TransportTCP()
{

}

TransportTCP::~TransportTCP()
{

}

u_int8_t TransportTCP::setup()
{
	return 0;
}

size_t TransportTCP::send_packet(void *buf, size_t size)
{
	if ( !buf ) 
		return 0;
	
	size_t sz;
	sz = write( m_handler, buf, size);
	
	if (sz < 0) {
		dbg("Error writing to socket..\n");
		return 0;
	}

	return sz;
}


void TransportTCP::close()
{
}


