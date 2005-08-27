/**************************************************************************
 *   Copyright (C) 2005 Matteo Merli <matteo.merli@gmail.com>
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
 * 
 *   $URL$
 * 
 *****************************************************************************/

#ifndef _RTP_H_
#define _RTP_H_

#include <machine/endian.h>
#include <sys/types.h>

/** RTP version implemented */
#define RTP_VERSION 2

#define RTP_SEQ_MOD (1<<16)

/** Maximun text length for SDES*/
#define RTP_MAX_SDES 255

/** RTCP packet types */
typedef enum {
	RTCP_SR   = 200,
	RTCP_RR   = 201,
	RTCP_SDES = 202,
	RTCP_BYE  = 203,
	RTCP_APP  = 204,
	
	RTCP_NULL = 0 /* Used for internal reasons.. */
} rtcp_type_t;


/**
 * RTCP SDES packet types
 */
typedef enum {
	RTCP_SDES_END   = 0,
	RTCP_SDES_CNAME = 1,
	RTCP_SDES_NAME  = 2,
	RTCP_SDES_EMAIL = 3,
	RTCP_SDES_PHONE = 4,
	RTCP_SDES_LOC   = 5,
	RTCP_SDES_TOOL  = 6,
	RTCP_SDES_NOTE  = 7,
	RTCP_SDES_PRIV  = 8
} rtcp_sdes_type_t;


/**
 * RTP data header
 */
typedef struct {
#if BYTE_ORDER == BIG_ENDIAN
	u_int32_t version:2;  /*! protocol version       */
	u_int32_t p:1;        /*! padding flag           */
	u_int32_t x:1;        /*! header extension flag  */
	u_int32_t cc:4;       /*! CSRC count             */
	u_int32_t m:1;        /*! marker bit             */
	u_int32_t pt:7;       /*! payload type           */
	u_int32_t seq:16;     /*! sequence number        */
	u_int32_t ts;            /*! timestamp              */
	u_int32_t ssrc;          /*! synchronization source */
	u_int32_t csrc[1];	    /*! optional CSRC list     */

#elif BYTE_ORDER == LITTLE_ENDIAN
	u_int32_t cc:4;       /*! CSRC count             */
	u_int32_t x:1;        /*! header extension flag  */
	u_int32_t p:1;        /*! padding flag           */
	u_int32_t version:2;  /*! protocol version       */
	u_int32_t pt:7;       /*! payload type           */
	u_int32_t m:1;        /*! marker bit             */
	u_int32_t seq:16;     /*! sequence number        */
	u_int32_t ts;            /*! timestamp              */
	u_int32_t ssrc;          /*! synchronization source */
	u_int32_t csrc[1];       /*! optional CSRC list     */

#else
#error "Byte order is not big endian nor little endian..."
#endif
} rtp_header_t;

/**
 * RTCP common header word
 */
typedef struct {
#if BYTE_ORDER == BIG_ENDIAN
	u_int32_t version:2;   /* protocol version                */
	u_int32_t p:1;         /* padding flag                    */
	u_int32_t count:5;     /* varies by packet type           */
	u_int32_t pt:8;        /* RTCP packet type                */
	u_int16_t length;      /* pkt len in words, w/o this word */

#else /* LITTLE ENDIAN */
	u_int32_t count:5;     /* varies by packet type           */
	u_int32_t p:1;         /* padding flag                    */
	u_int32_t version:2;   /* protocol version                */
	u_int32_t pt:8;        /* RTCP packet type                */
	u_int16_t length;          /* pkt len in words, w/o this word */
#endif
} rtcp_common_t;

/**
 * Big-endian mask for version, padding bit and packet type pair
 */
#define RTCP_VALID_MASK (0xc000 | 0x2000 | 0xfe)
#define RTCP_VALID_VALUE ((RTP_VERSION << 14) | RTCP_SR)

/**
 * Reception report block
 */
typedef struct {
	u_int32_t ssrc;         /* data source being reported      */
	u_int32_t fraction:8;   /* fraction lost since last SR/RR  */
	
	int32_t lost:24;        /* cumul. no. pkts lost (signed!)  */
	u_int32_t last_seq;     /* extended last seq. no. received */
	u_int32_t jitter;       /* interarrival jitter             */
	u_int32_t lsr;          /* last SR packet from this source */
	u_int32_t dlsr;         /* delay since last SR packet      */
} rtcp_rr_t;

/**
 * SDES item
 */
typedef struct {
	u_int8_t type;              /* type of item (rtcp_sdes_type_t) */
	u_int8_t length;            /* length of item (in octets)      */
	u_int8_t data[1];           /* text, not null-terminated       */
} rtcp_sdes_item_t;

/**
 * One RTCP packet
 */
typedef struct {
	rtcp_common_t common;     /* common header */
	union {
		/*! sender report (SR) */
		struct {
			u_int32_t ssrc;     /* sender generating this report */
			u_int32_t ntp_sec;  /* NTP timestamp */
			u_int32_t ntp_frac;
			u_int32_t rtp_ts;   /* RTP timestamp */
			u_int32_t psent;    /* packets sent */
			u_int32_t osent;    /* octets sent */
			rtcp_rr_t rr[1];   /* variable-length list */
		} sr;

		/*! reception report (RR) */
		struct {
			uint32_t ssrc;   /* receiver generating this report */
			rtcp_rr_t rr[1]; /* variable-length list */
		} rr;

		/*! source description (SDES) */
		struct rtcp_sdes {
			u_int32_t src;      /* first SSRC/CSRC */
			rtcp_sdes_item_t item[1]; /* list of SDES items */
		} sdes;

		/*! BYE */
		struct {
			u_int32_t src[1];   /* list of sources */
			/* can't express trailing text for reason */
		} bye;
	} r;
} rtcp_t;

typedef struct rtcp_sdes rtcp_sdes_t;

/**
 * Per-source state information
 */
typedef struct {
	u_int16_t max_seq;        /* highest seq. number seen            */
	u_int32_t cycles;         /* shifted count of seq. number cycles */
	u_int32_t base_seq;       /* base seq number                     */
	u_int32_t bad_seq;        /* last 'bad' seq number + 1           */
	u_int32_t probation;      /* sequ. packets till source is valid  */
	u_int32_t received;       /* packets received                    */
	u_int32_t expected_prior; /* packet expected at last interval    */
	u_int32_t received_prior; /* packet received at last interval    */
	u_int32_t transit;        /* relative trans time for prev pkt    */
	u_int32_t jitter;         /* estimated jitter                    */
	/* ... */
} source_t; 

#endif /* _RTP_H_ */


