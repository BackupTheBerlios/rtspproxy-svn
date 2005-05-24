/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _UTIL_H
#define _UTIL_H

CString UrlEncode(CPCHAR sz);
CString UrlDecode(CPCHAR sz);
CString Base64Encode(CPCHAR sz);
CString Base64Decode(CPCHAR sz);

// Fairly raw MD5 code .. I need to clean it up when I'm bored ;)

typedef struct {
	UINT32 state[4];	/* state (ABCD) */
	UINT32 count[2];	/* number of bits, modulo 2^64 (lsb first) */
	unsigned char buffer[64];	/* input buffer */
} MD5_CTX;

void MD5Init(MD5_CTX *);
void MD5Update(MD5_CTX *, unsigned char *, unsigned int);
void MD5Final(unsigned char[16], MD5_CTX *);

#endif				// ndef _UTIL_H

/** LOG **
 *
 * $Log: util.h,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

