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

#ifndef _RTP_H_
#define _RTP_H_

#ifdef _BSD
#include <machine/endian.h>
#else
#include <endian.h>
#endif
#include <sys/types.h>

typedef unsigned char u_int8_t;
typedef unsigned short u_int16_t;
typedef unsigned int u_int32_t;
typedef unsigned long long u_int64_t;
typedef u_int8_t  uint8_t;
typedef u_int16_t uint16_t;
typedef u_int32_t uint32_t;
typedef u_int64_t uint64_t;

#define RTP_VERSION 2

#define RTP_SEQ_MOD (1<<16)

/* Maximun text length for SDES*/
#define RTP_MAX_SDES 255

typedef enum {
	RTCP_SR   = 200,
	RTCP_RR   = 201,
	RTCP_SDES = 202,
	RTCP_BYE  = 203,
	RTCP_APP  = 204,
	
	RTCP_NULL = 0 /* Used for internal reasons.. */
} rtcp_type_t;

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
	unsigned int version:2;  /* protocol version       */
	unsigned int p:1;        /* padding flag           */
	unsigned int x:1;        /* header extension flag  */
	unsigned int cc:4;       /* CSRC count             */
	unsigned int m:1;        /* marker bit             */
	unsigned int pt:7;       /* payload type           */
	unsigned int seq:16;     /* sequence number        */
	uint32_t ts;             /* timestamp              */
	uint32_t ssrc;           /* synchronization source */
	uint32_t csrc[1];	 /* optional CSRC list     */

#elif BYTE_ORDER == LITTLE_ENDIAN
	unsigned int cc:4;       /* CSRC count             */
	unsigned int x:1;        /* header extension flag  */
	unsigned int p:1;        /* padding flag           */
	unsigned int version:2;  /* protocol version       */
	unsigned int pt:7;       /* payload type           */
	unsigned int m:1;        /* marker bit             */
	unsigned int seq:16;     /* sequence number        */
	uint32_t ts;             /* timestamp              */
	uint32_t ssrc;           /* synchronization source */
	uint32_t csrc[1];        /* optional CSRC list     */

#else
#error "Byte order is not big endian nor little endian..."
#endif
} rtp_header_t;

/**
 * RTCP common header word
 */
typedef struct {
#if BYTE_ORDER == BIG_ENDIAN
	unsigned int version:2;   /* protocol version                */
	unsigned int p:1;         /* padding flag                    */
	unsigned int count:5;     /* varies by packet type           */
	unsigned int pt:8;        /* RTCP packet type                */
	uint16_t length;          /* pkt len in words, w/o this word */

#else /* LITTLE ENDIAN */
	unsigned int count:5;     /* varies by packet type           */
	unsigned int p:1;         /* padding flag                    */
	unsigned int version:2;   /* protocol version                */
	unsigned int pt:8;        /* RTCP packet type                */
	uint16_t length;          /* pkt len in words, w/o this word */
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
	uint32_t ssrc;             /* data source being reported      */
	unsigned int fraction:8;   /* fraction lost since last SR/RR  */
	
	int lost:24;               /* cumul. no. pkts lost (signed!)  */
	uint32_t last_seq;         /* extended last seq. no. received */
	uint32_t jitter;           /* interarrival jitter             */
	uint32_t lsr;              /* last SR packet from this source */
	uint32_t dlsr;             /* delay since last SR packet      */
} rtcp_rr_t;

/**
 * SDES item
 */
typedef struct {
	uint8_t type;              /* type of item (rtcp_sdes_type_t) */
	uint8_t length;            /* length of item (in octets)      */
	char data[1];              /* text, not null-terminated       */
} rtcp_sdes_item_t;

/**
 * One RTCP packet
 */
typedef struct {
	rtcp_common_t common;     /* common header */
	union {
		/*! sender report (SR) */
		struct {
			uint32_t ssrc;     /* sender generating this report */
			uint32_t ntp_sec;  /* NTP timestamp */
			uint32_t ntp_frac;
			uint32_t rtp_ts;   /* RTP timestamp */
			uint32_t psent;    /* packets sent */
			uint32_t osent;    /* octets sent */
			rtcp_rr_t rr[1];   /* variable-length list */
		} sr;

		/*! reception report (RR) */
		struct {
			uint32_t ssrc;   /* receiver generating this report */
			rtcp_rr_t rr[1]; /* variable-length list */
		} rr;

		/*! source description (SDES) */
		struct rtcp_sdes {
			uint32_t src;      /* first SSRC/CSRC */
			rtcp_sdes_item_t item[1]; /* list of SDES items */
		} sdes;

		/*! BYE */
		struct {
			uint32_t src[1];   /* list of sources */
			/* can't express trailing text for reason */
		} bye;
	} r;
} rtcp_t;

typedef struct rtcp_sdes rtcp_sdes_t;

/**
 * Per-source state information
 */
typedef struct {
	uint16_t max_seq;        /* highest seq. number seen            */
	uint32_t cycles;         /* shifted count of seq. number cycles */
	uint32_t base_seq;       /* base seq number                     */
	uint32_t bad_seq;        /* last 'bad' seq number + 1           */
	uint32_t probation;      /* sequ. packets till source is valid  */
	uint32_t received;       /* packets received                    */
	uint32_t expected_prior; /* packet expected at last interval    */
	uint32_t received_prior; /* packet received at last interval    */
	uint32_t transit;        /* relative trans time for prev pkt    */
	uint32_t jitter;         /* estimated jitter                    */
	/* ... */
} source_t; 

#endif /* _RTP_H_ */


