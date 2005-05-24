/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#include <ctype.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "tran.h"

/*
 * RTP/RTCP requires a port pair with the lower port even (divisible by two).
 * "TCP/IP Illustrated Vol 1" indicates that there is no inherent standard for
 * choosing port numbers except those designated by IANA, 1..1023.  Most
 * TCP/IP stacks use 1024..4999 for ephermal ports and 5000..65535 are up for
 * grabs, but this is implementation dependent.
 *
 * RealNetworks code uses the following (from rmartsp/pub/rtsptran.h):
 *   const UINT16 MIN_UDP_PORT = 6970;
 *   const UINT16 MAX_UDP_PORT = 32000;
 *
 * Sounds good to me .. 6970..32000 it is! :-)
 *
 * Also note that different systems have different behavior when a received
 * UDP packet exceeds the buffer size in the call to read().  Therefore, we
 * must consider a full read as a bad packet and attempt to resync.
 */

#define MIN_UDP_PORT 6970	/* should be at least 1024 and even */
#define MAX_UDP_PORT 32000	/* should be even */

/**************************************
 *
 * CTransport
 *
 **************************************/

CTransport::CTransport(void):m_pResponse(NULL),
m_type(ttNONE), m_layer(ltNONE)
{
	// Empty
}

CTransport::CTransport(CTransportResponse * pResponse, Type tt, Layer lt):
m_pResponse(pResponse), m_type(tt), m_layer(lt)
{
	assert(m_pResponse);
}

CTransport::~CTransport(void)
{
	// Empty
}

/** LOG **
 *
 * $Log: tran.cpp,v $
 * Revision 1.2  2003/11/17 16:14:08  mat
 * make-up
 *
 *
 */

