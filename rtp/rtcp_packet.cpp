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
 
#include "rtcp_packet.h"

#include <assert.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <math.h>

void process_rtcp_packet(void *buf, uint32_t size)
{
	RtcpCompound compound( buf, size );
	RtcpPacket rtcp;

	int j = 1;
	while ( compound.get_next_packet( rtcp ) ) {
		printf("\n@@ %u @@\n", j++);

		switch ( rtcp.type() ) {

		case RTCP_SR:  /* This is an SR packet */

		printf("***************************************\n");
		printf("* SENDER REPORT\n*\n");
		printf("* SSRC: %u\n", rtcp.sr_ssrc() );
		printf("* ntp: %u.%u s\n", rtcp.sr_ntp_sec(), rtcp.sr_ntp_frac() );
		printf("* RTP timestamp: %u\n", rtcp.sr_rtp_ts() );
		printf("* Packets sent: %u\tBytes sent: %u\n", rtcp.sr_psent(), rtcp.sr_osent() );

		for (uint j=0; j < rtcp.count(); j++ ){
			rtcp_rr_t* rr = rtcp.sr_rr( j );
			if ( !rr )
				break;
			ReceiverReport b( rr );
			printf("*\n* Fraction lost: %u\tLost packets: %u\n",
				b.fraction(), b.lost() );
			printf("* Last seq.: %u\tJitter: %u\n", b.last_seq(), b.jitter() );
			printf("* Last SR: %u\tDelay Last SR: %f s\n", b.lsr(), 
				(double)b.dlsr() / 65536 );

		}
		printf("***************************************\n");

		break;

		case RTCP_RR: /* This is an RR packet */

		printf("=======================================\n");
		printf("| RECEIVER REPORT\n|\n");
		printf("| SSRC: %u\tLength: %u\n", rtcp.rr_ssrc(), rtcp.length() );
		for ( j=0; j < rtcp.count() ; j++ ){
			rtcp_rr_t* rr = rtcp.rr_rr( j );
			if ( !rr )
				break;
			ReceiverReport b( rr );
			printf("|\n| Fraction lost: %u\tLost packets: %u\n",
				b.fraction(), b.lost() );
			printf("| Last seq.: %u\tJitter: %u\n", b.last_seq(), b.jitter() );
			printf("| Last SR: %u\tDelay Last SR: %f s\n", b.lsr(),
				(double)b.dlsr() / 65536 );
		}
		printf("=======================================\n");
		break;

		case RTCP_SDES:

		printf("=== SDES ===\n");
		printf("Pkt length: %u\n", rtcp.length() );
		for ( j=0; j < rtcp.count(); j++ ){
			rtcp_sdes_item_t* item;
			item = rtcp.sdes( j );
			if ( !item )
				break;
			SDESItem i( item );
			printf("Type : %s\n", i.type_str() );
			printf("Length : %u\n", i.length() );
			printf("Value: %s\n", i.value() );
		}
		printf("===========\n");
		break;

		case RTCP_BYE:

		printf("==== BYE ====\n");
		break;

		case RTCP_APP:

		printf("==== Application-defined ====\n");
		break;

		default: ;
		}

	}
}

/**********************************************************************/

RtcpCompound::RtcpCompound(void * buf, size_t size)
{
	assert( buf );
	assert( size );

	m_compound = buf;
	m_size = size;
	m_offset = 0;
}

RtcpCompound::~RtcpCompound()
{
}

bool RtcpCompound::get_next_packet(RtcpPacket &packet)
{
	if ( m_offset >= m_size )
		return false;

	RtcpPacket tmp = RtcpPacket( (uint8_t*)m_compound + m_offset, m_size );
	uint32_t len = tmp.length() +1 ;
	packet = RtcpPacket( (uint8_t*)m_compound + m_offset, len * 4);
	m_offset += len * 4;
	return true;
}

/**********************************************************************/

ReceiverReport::ReceiverReport(rtcp_rr_t *rr)
{
	m_rr = rr;
}


ReceiverReport::~ReceiverReport()
{
}

/**********************************************************************/

SDESItem::SDESItem(rtcp_sdes_item_t *sdes)
{
	m_sdes = sdes;
	m_str = NULL;
}

SDESItem::~SDESItem()
{
	if ( m_str )
		free( m_str );
}

const char* SDESItem::type_str()
{
	switch ( m_sdes->type ) {

	case RTCP_SDES_END: return "END";
	case RTCP_SDES_CNAME: return "CNAME";
	case RTCP_SDES_NAME: return "NAME";
	case RTCP_SDES_EMAIL: return "EMAIL";
	case RTCP_SDES_PHONE: return "PHONE";
	case RTCP_SDES_LOC: return "LOC";
	case RTCP_SDES_TOOL: return "TOOL";
	case RTCP_SDES_NOTE: return "NOTE";
	case RTCP_SDES_PRIV: return "PRIV";
	default:
		return "Error...";
	}
}

char* SDESItem::value()
{
	if ( m_str )
		free( m_str );

	/* Not sure what to do here...
	 * RFC says that length it should refer to complexive
	 * item length.. but servers and players seems to use it
	 * like the length of the string...
	 */
	uint8_t len = length()/* - 2*/;
	m_str = (char *)malloc( len + 1 );
	memcpy( m_str, m_sdes->data, len );
	m_str[ len ] = '\0';
	return m_str;
}

void SDESItem::set_value(const char* str)
{
	uint8_t len = strlen( str );
	m_sdes->length = len;
	m_str = (char *)malloc( len );
	strncpy( m_str, str, len );
}

/**********************************************************************/
/**********************************************************************/

RtcpPacket::RtcpPacket(void * buf, size_t size)
{
	assert( buf );
	assert( size );
	
	m_packet = (rtcp_t *)buf;
	m_size = size;
	m_delete = false;
}

RtcpPacket::RtcpPacket(rtcp_type_t type) : 
	m_sdes( NULL )
{
	if ( ! type ) {
		m_delete = false;
		return;
	}
	
	m_delete = true;
	uint16_t size = 4; /* common header */
	
	switch ( type ) {
	case RTCP_SR:   size += 6 * 4; break;
	case RTCP_RR:   size += 7 * 4; break;
	case RTCP_SDES: size += 4 + 2; break;
	case RTCP_BYE:  
	case RTCP_APP:  break;
	default: ;
	}
	
	m_packet = (rtcp_t *)malloc( size );
	m_size = size;
	set_version( 2 );
	set_type( type );
	set_padding( false );
	set_length( (uint16_t)ceil( (double)size/4 ) - 1 );
}

RtcpPacket::~RtcpPacket()
{
	if ( m_delete )
		free( m_packet );
}

rtcp_rr_t* RtcpPacket::sr_rr(uint8_t idx)
{
	if ( idx < count() ) 
		return &(m_packet->r.sr.rr[ idx ]);
		
	return NULL;
}

rtcp_rr_t* RtcpPacket::rr_rr(uint8_t idx)
{
	if ( idx < count() )
		return &(m_packet->r.rr.rr[ idx ]);

	return NULL;
}

rtcp_sdes_item_t* RtcpPacket::sdes(uint8_t idx)
{
	if ( idx < count() )
		return &(m_packet->r.sdes.item[ idx ]);

	return NULL;
}

void RtcpPacket::set_sdes(rtcp_sdes_type_t type, const char* value)
{
	rtcp_sdes_item_t* sdes = (rtcp_sdes_item_t*)(m_packet + m_size);
	memcpy( (uint8_t*)m_packet + m_size, sdes, 2 + strlen(value) );
	m_size += 2 + strlen(value);
}

uint32_t RtcpPacket::to_buffer(void *buf )
{
	memcpy( (uint8_t *)buf, m_packet, m_size );
	return m_size;
}


