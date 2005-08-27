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
 
#ifndef _RTP_SESSION_H_
#define _RTP_SESSION_H_

#include <sys/time.h>

#include "rtp.h"
#include "rtp_packet.h"
#include "rtcp_packet.h"

/*
 * Minimum average time between RTCP packets from this site (in
 * seconds).  This time prevents the reports from `clumping' when
 * sessions are small and the law of large numbers isn't helping
 * to smooth out the traffic.  It also keeps the report interval
 * from becoming ridiculously small during transient outages like
 * a network partition.
 */
#define RTCP_MIN_TIME (double)5.0

/*
 * Fraction of the RTCP bandwidth to be shared among active
 * senders.  (This fraction was chosen so that in a typical
 * session with one or two active senders, the computed report
 * time would be roughly equal to the minimum report time so that
 * we don't unnecessarily slow down receiver reports.)  The
 * receiver fraction must be 1 - the sender fraction.
 */
#define RTCP_SENDER_BW_FRACTION (double)0.25
#define RTCP_RCVR_BW_FRACTION (double)(1-RTCP_SENDER_BW_FRACTION)

/* To compensate for "timer reconsideration" converging to a
 * value below the intended average.
 */
#define COMPENSATION (double)(2.71828-1.5)

#define MAX_UDP_LEN 8192

/*! My own version of random() that uses the linux random
 *  device..
 */
uint64_t my_random();

/*! Event queue list..*/
typedef struct {
	/*! Scheduled time.. */
	double time;

	/*! RTCP packet type. */
	uint8_t type;

	/*! Reference to next event in queue. */
	void *next;
} event_t;

/*! NTP time structure. */
typedef struct {
	uint32_t secs;
	uint32_t frac;
} ntp_t;

/*! This classe create and manage a complete RTP session.
 */
class RtpSession
{
 public:
	/*! Constructor. */
	RtpSession();

	/*! Destructor. */
	~RtpSession();
	
	/*! Sets up the parameter of the RTP session.
	 *  @param base_port RTP port to bind(). RTCP port will be
	 *                   base_port+1. If base_port==0 the port will
	 *                   be dynamically chosed.
	 *  @param cname     This is the first part of the canonical name.
	 *  @param peer_addr Address of the other end-point of the communication.
	 *  @param peer_port Port of the other end-point of the communication.
	 *  @param payload_type RTP payload type for this RTP session.
	 *  @param rtp_clock RTP clock is a mesure to convert from RTP timestamp
	 *                   to normal time. Specify the amount of RTP timestamp
	 *                   increment for each second.
	 */
	bool setup( uint16_t base_port, const char* cname, 
		const char* client_addr, uint16_t client_port,
		uint8_t payload_type, uint32_t rtp_clock );

	/*! Sets the approssimate bandwidth (in bits per second ) for the stream. */
	void set_bandwidth(uint32_t b) {m_bandwidth = b;}

	/*! Closes the session sending a BYE RTCP packet and freeing resources.. */
	void close();
	
	/*! This method sends RTP packet blocking the thread until the moment
	 *  to send another packet.
	 *  @param buf Pointer to the buffer that contains the payload.
	 *  @param size Size of the payload.
	 *  @param ts_inc Incrementation of RTP timestamp since last packet.
	 */
	bool send_rtp_packet(void *buf, uint16_t size, uint32_t ts_inc );
	
	uint16_t receive_rtp_packet(void *buf);
	
	/*! Sets the SSRC for the source.
	 *  A random SSRC will be automatically generated, this
	 *  function let you change it.
	 */
	void set_ssrc(uint32_t ssrc);
	
	/*! Sets the CNAME SDES item for the session.
	 */
	void set_cname(const char* s);

	/*! Returns the local SSRC. */
	uint32_t get_ssrc() {return m_ssrc;}
	
	/*! Returns the canonical name. */
	const char* get_cname() {return (const char *) m_cname;}
		
	/*! Returns the RTP local port used
	 *  or 0 if it's not set.
	 */
	uint16_t get_rtp_port();
	
	/*! Returns the RTCP local port used
	 *  or 0 if it's not set.
	 */
	uint16_t get_rtcp_port();
	
	/*! Return the ranmly chosen initial timestamp. */ 
	uint32_t get_base_timestamp() {return m_base_timestamp;}
	
	/*! Return the ranmly chosen initial sequence number. */ 
	uint32_t get_base_seq() {return m_base_seq;}
	
	/*! */
	bool setup_peer(const char* peer_addr, uint16_t peer_port);

protected:

	double compute_rtcp_interval();

	/*! Send an RTP packet to the peer host. */
	bool send_rtp_pkt(RtpPacket *pkt);
	
	/*! Sends a generic packet.. */
	bool send_pkt(void *buf, uint32_t size);
	
	/*! Send an RTCP packet to the peer host. 
	 *  @param pkt RTCP packet to be sent
	 *  @param bye_packet If true, the function will actually send 
	 *         3 packet: pkt, a CNAME and a BYE.
	 */
	bool send_rtcp_pkt(RtcpPacket *pkt, bool bye_packet=false);

	/*! Creates and binds the UDP sockets. */
	bool create_sockets(uint16_t base_port=0);
		
	void insert_event(event_t*);
	
	uint16_t receive_rtp(void *buf);
	
	bool receive_rtcp();
	
	void send_rtcp();

private:
	/*! Socket used for data packets. */
	int32_t m_rtp_sock;

	/*! Sockets used for control packets. */
	int32_t m_rtcp_sock;

	/*! Local RTP port. */
	uint16_t m_rtp_local_port;

	/*********************************************/
	
	/*! SSRC for this source. */
	uint32_t m_ssrc;
	
	/*! Canonical name for the source. 
	 *  It will be in the form of 'name@host.com'
	 */
	char *m_cname;
	
	/*! Initial timestamp. */
	uint32_t m_base_timestamp;
	
	/*! Previous timestamp. */
	uint32_t m_prev_timestamp;
	
	/*! Initial sequence number. */
	uint16_t m_base_seq;
	
	/*! Current sequence number. */
	uint16_t m_seq;
	
	/*! Payload type. */ 
	uint8_t m_payload_type;
	
	/*! RTP clock. */ 
	uint32_t m_rtp_clock;

	/*! Scratch buffer. */
	uint8_t m_buf[ MAX_UDP_LEN ];
	
	/*! Sdes packet to be attached to every RTCP packet.. */
	rtcp_t* m_sdes_pkt;
	
	uint16_t m_sdes_pkt_size;
	
	/*! Event queue. */
	event_t *m_event_queue;
	
	/********************************************************* 
	*         STATISTICS 
	*********************************************************/
	
	/*! Time when we sent the first packet.. */
	double m_start_time;
	
	/*! Packets sent. */
	uint32_t m_psent;
	
	/*! Octects sent. */
	uint32_t m_osent;

	/*! Average size of RTCP packets. */
	double m_rtcp_avg_size;

	/*! Number of RTCP packet sent. */
	uint32_t m_rtcp_psent;

	uint32_t m_bandwidth;
	
	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
	 *               RECEIVER STATISTICS               *
	 *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/
	 
	/*! Number of received RTP packets. */
	uint32_t m_preceived;
	
	/*! Last timestamp. */
	uint32_t m_last_timestamp;
	 

};




#endif




/** LOG **
 *
 * $Log: rtp_session.h,v $
 * Revision 1.3  2003/11/17 16:14:12  mat
 * make-up
 *
 *
 */

