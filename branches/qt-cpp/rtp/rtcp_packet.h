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
 
#ifndef _RTCP_PACKET_H_
#define _RTCP_PACKET_H_

#include "rtp.h"
#include <netinet/in.h>

extern "C" void process_rtcp_packet(void *buf, uint32_t size);

class RtcpPacket;

/*! Process an entire UDP packet and
 *  splits it up in all the RTCP packets contained.
 */
class RtcpCompound
{
public:
	RtcpCompound(void * buf, size_t size);
	~RtcpCompound();

	bool get_next_packet(RtcpPacket &packet);

protected:
	void *m_compound;
	uint32_t m_size;
	uint32_t m_offset;
};

/*! Process a receiver report block. */
class ReceiverReport
{
public:
	/*! Constructor.
	 *  @param rr Pointer to the receiver report.
	 */
	ReceiverReport(rtcp_rr_t *rr);

	/*! Destructor. */
	~ReceiverReport();

	/*! Data source being reported. */
	uint32_t ssrc() {return ntohl(m_rr->ssrc);}

	/*! Fraction lost since last SR/RR. */
	uint8_t fraction() {return m_rr->fraction;}

	/*! Cumul. no. pkts lost (signed!).  */
	int32_t lost() {return m_rr->lost;}

	/*! Extended last seq. no. received. */
	uint32_t last_seq() {return ntohl(m_rr->last_seq);}

	/*! Interarrival jitter. */
	uint32_t jitter() {return ntohl(m_rr->jitter);}

	/*! Last SR packet from this source. */
	uint32_t lsr() {return ntohl(m_rr->lsr);}

	/*! Delay since last SR packet. */
	uint32_t dlsr() {return ntohl(m_rr->dlsr);}

protected:
	/*! Pointer to the receiver report. */
	rtcp_rr_t *m_rr;
};


/*! Process a SDES item. */
class SDESItem
{
public:
	/*! Constructor.
	 *  @param sdes Pointer to the SDES item.
	 */
	SDESItem(rtcp_sdes_item_t *sdes);

	/*! Destructor. */
	~SDESItem();

	/*! Returns the numeric type. */
	uint8_t type() {return m_sdes->type;}

	/*! Returns a translated string for the SDES type. */
	const char* type_str();

	/*! Returns the length of the SDES item. */
	uint8_t length() {return m_sdes->length;}

	/*! Return the value string. */
	char* value();
	
	/*! Sets the numeric type. */
	void set_type(uint8_t t) {m_sdes->type = t;}
	
	/*! Sets the length. */
	void set_length(uint8_t l) {m_sdes->length = l;}

	/*! Sets the value string. */
	void set_value(const char* str);

protected:
	/*! Pointer to the sdes item. */
	rtcp_sdes_item_t *m_sdes;

	/*! Value string. */
	char* m_str;
};



/*! This class is used to process incomings RTCP packets and
 *  to create new packets.
 */
class RtcpPacket
{
public:
	/*! Create a new RtcpPacket using an existing
	 *  RTCP packet.
	 *  @param	buf	pointer to the start of the packet
	 *  @param	size	size of the packet
	 */ 
	RtcpPacket(void * buf, size_t size);
	
	/*! Create an empty RtpPacket.
	 *  @param type RTCP packet type.
	 */
	RtcpPacket(rtcp_type_t type=RTCP_NULL);
	
	/*! Destructor */
	~RtcpPacket();
	
	/*! RTP Version. It _MUST_ be 2. */
	uint8_t version() {return m_packet->common.version;}
	
	/*! If the padding bit is set, this individual RTCP packet contains
	 *  some additional padding octets at the end which are not part of
	 *  the control information but are included in the length field.  The
	 *  last octet of the padding is a count of how many padding octets
	 *  should be ignored, including itself (it will be a multiple of
	 *  four).  Padding may be needed by some encryption algorithms with
	 *  fixed block sizes.  In a compound RTCP packet, padding is only
	 *  required on one individual packet because the compound packet is
	 *  encrypted as a whole for the method in Section 9.1. */
	bool padding() {return m_packet->common.p;}
	
	/*! .... */
	uint8_t count() {return m_packet->common.count;}
	
	/*! RTCP packet type. */
	uint8_t type() {return m_packet->common.pt;}
	
	/*! The length of this RTCP packet in 32-bit words minus one,
	 *  including the header and any padding.  (The offset of one makes
	 *  zero a valid length and avoids a possible infinite loop in
	 *  scanning a compound RTCP packet, while counting 32-bit words
	 *  avoids a validity check for a multiple of 4.) */
	uint16_t length() {return ntohs( m_packet->common.length);}
	
	/****************************************/
	void set_version(uint8_t v) {m_packet->common.version = v;}
	void set_padding(bool p) {m_packet->common.p = p;}
	void set_count(uint8_t cc) {m_packet->common.count = cc;}
	void set_type(uint8_t type) {m_packet->common.pt = type;}
	void set_length(uint16_t l) {m_packet->common.length = htons(l);}
	
	/*******  SENDER REPORT  *********/ 
	
	/*! Sender generating this report. */
	uint32_t sr_ssrc() {return ntohl( m_packet->r.sr.ssrc);}
	
	/*! NTP timestamp. */
	uint32_t sr_ntp_sec() {return ntohl( m_packet->r.sr.ntp_sec);}
	uint32_t sr_ntp_frac() {return ntohl( m_packet->r.sr.ntp_frac);}
	
	/*! RTP timestamp. */
	uint32_t sr_rtp_ts() {return ntohl( m_packet->r.sr.rtp_ts);}
	
	/*! Packets sent. */
	uint32_t sr_psent() {return ntohl( m_packet->r.sr.psent);}
	
	/*! Octets sent. */
	uint32_t sr_osent() {return ntohl( m_packet->r.sr.osent);}
	
	/****************************************/
	void set_sr_ssrc(uint32_t ssrc) {m_packet->r.sr.ssrc = htonl(ssrc);}
	void set_sr_ntp_sec(uint32_t ntp_sec) {m_packet->r.sr.ntp_sec = htonl(ntp_sec);}
	void set_sr_ntp_frac(uint32_t ntp_frac) {m_packet->r.sr.ntp_frac = htonl(ntp_frac);}
	void set_sr_rtp_ts(uint32_t ts) {m_packet->r.sr.rtp_ts = htonl(ts);}
	void set_sr_psent(uint32_t psent) {m_packet->r.sr.psent = htonl(psent);}
	void set_sr_osent(uint32_t osent) {m_packet->r.sr.osent = htonl(osent);}
	
	/*! Returns the receiver report with index idx. */
	rtcp_rr_t* sr_rr(uint8_t idx);
		
	/*************************************************/
	/*! Sender generating this report. */
	uint32_t rr_ssrc() {return ntohl( m_packet->r.rr.ssrc);}
	
	/*! Returns the receiver report with index idx. */
	rtcp_rr_t* rr_rr(uint8_t idx);

	/*! Returns the SDES item with index idx. */
	rtcp_sdes_item_t* sdes(uint8_t idx);
	void set_sdes_ssrc(uint32_t ssrc) {m_packet->r.sdes.src = htonl(ssrc);}
	
	void set_sdes(rtcp_sdes_type_t type, const char* value);

	/*! Write the entire RTCP packet in a buffer (already allocated)
	 *  and returns the size of the packet.
	 */
	uint32_t to_buffer(void *buf );
	
	/*! Returns a reference to the packet buffer.. */
	rtcp_t * packet() {return m_packet;}
	
protected:
	/*! Pointer to the RTCP packet. */
	rtcp_t *m_packet;
	
	/*! */
	rtcp_sdes_item_t* m_sdes;
	
	/*! Packet size. */
	uint32_t m_size;
	
	/*! */ 
	bool m_delete;

};




#endif


/** LOG **
 *
 * $Log: rtcp_packet.h,v $
 * Revision 1.3  2003/11/17 16:14:12  mat
 * make-up
 *
 *
 */

