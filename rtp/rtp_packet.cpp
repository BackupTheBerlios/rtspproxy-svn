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
 
#include <sys/types.h>

#include "rtp_packet.h"

#include <assert.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>

RtpPacket::RtpPacket(void * buf, size_t size) :
	m_delete( false )
{
	assert( buf );
	assert( size );
	
	m_header = (rtp_header_t *)buf;
	m_size = size;

	uint32_t head_size = RTP_HEADER_LEN + 2 + //// XXX NOT SURE
		m_header->cc * sizeof( uint32_t ) +
		0;
		//m_header->x * 8 ;

	m_payload = (void*)( m_header + head_size );

	m_payload_size = m_size - head_size;
	if ( m_header->x )
		printf("Warning.. Extension flag is set...\n");
	if ( m_header->p )
		printf("Warning.. Padding bit is set...\n");

	///printf("size: %u - head_size: %u\n", size, head_size );
		
}

RtpPacket::RtpPacket() :
	m_payload( NULL ),
	m_payload_size( 0 ),
	m_delete( true )
{
	m_header = (rtp_header_t *)calloc( 1, RTP_HEADER_LEN );
	
	m_header->version = 2;
	m_header->p = 0;
	m_header->x = 0;
	m_header->cc = 0;
	
	m_size = RTP_HEADER_LEN;
}

RtpPacket::~RtpPacket()
{
	if ( m_delete )
		free( m_header );
}

void RtpPacket::set_payload(void *buf, uint32_t size)
{
	m_payload = buf;
	m_payload_size = size;
	m_size += size;
}

void* RtpPacket::get_payload()
{
	return m_payload;
}

uint32_t RtpPacket::get_payload_size()
{
	return m_payload_size;
}


uint32_t RtpPacket::to_buffer(void *buf )
{
	memcpy( buf, m_header, RTP_HEADER_LEN );
	memcpy( (uint8_t*)buf + RTP_HEADER_LEN, m_payload, m_payload_size );
	/*
	FILE *f = fopen("/tmp/rtspproxy/prova", "a+");
	fwrite(buf, m_size, 1, f);
	fclose( f );
	*/

	/*
	RtpPacket *rtp = new RtpPacket( (uint8_t*)buf, (RTP_HEADER_LEN + m_payload_size) );
	printf("seq = %u - timestamp: %u - marker: %u - size: %u - psize: %u \n",
		rtp->sequence(), rtp->timestamp(), rtp->marker(),
		rtp->get_size(), rtp->get_payload_size() );
	delete rtp;
	*/
	return ( m_size );
}






/** LOG **
 *
 * $Log: rtp_packet.cpp,v $
 * Revision 1.3  2003/11/17 16:14:12  mat
 * make-up
 *
 *
 */

