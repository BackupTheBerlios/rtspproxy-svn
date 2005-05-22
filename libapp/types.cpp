/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#include "types.h"

//TODO: assembly-optimize these, for example use multiple registers
//      and convert blocks to avoid stalling the cpu pipeline

void htons_buf(UINT16 * pnbuf, const void *phbuf, UINT cnt)
{
	register UINT16 tmp;
	while (cnt) {
		memcpy(&tmp, phbuf, 2);
		*pnbuf = htons(tmp);
		phbuf = (char *) phbuf + 2;
		pnbuf++;
		cnt--;
	}
}

void ntohs_buf(UINT16 * phbuf, const void *pnbuf, UINT cnt)
{
	register UINT16 tmp;
	while (cnt) {
		memcpy(&tmp, pnbuf, 2);
		*phbuf = ntohs(tmp);
		pnbuf = (char *) pnbuf + 2;
		phbuf++;
		cnt--;
	}
}

void htonl_buf(UINT32 * pnbuf, const void *phbuf, UINT cnt)
{
	register UINT32 tmp;
	while (cnt) {
		memcpy(&tmp, phbuf, 4);
		*pnbuf = htonl(tmp);
		phbuf = (char *) phbuf + 4;
		pnbuf++;
		cnt--;
	}
}

void ntohl_buf(UINT32 * phbuf, const void *pnbuf, UINT cnt)
{
	register UINT32 tmp;
	while (cnt) {
		memcpy(&tmp, pnbuf, 4);
		*phbuf = ntohl(tmp);
		pnbuf = (char *) pnbuf + 4;
		phbuf++;
		cnt--;
	}
}

/** LOG **
 *
 * $Log: types.cpp,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

