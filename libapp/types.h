/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _TYPES_H
#define _TYPES_H

#include <sys/types.h>

#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>
#include <string.h>
#include <errno.h>

#ifdef _WIN32
#include <winsock2.h>
#include <windows.h>
#include <time.h>
#include <sys/stat.h>
#define strcasecmp _stricmp
#define strncasecmp _strnicmp
#endif

#ifdef _UNIX
#ifdef SOLARIS_GCC_HACK
#include "/usr/include/sys/types.h"
#else
#include <sys/types.h>
#endif
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/time.h>
#include <sys/stat.h>
#endif

#ifndef NULL
#define NULL 0
#endif
#ifndef min
#define min(a,b) ( ((a)<(b)) ? (a) : (b) )
#endif
#ifndef max
#define max(a,b) ( ((a)>(b)) ? (a) : (b) )
#endif

typedef unsigned int UINT;

typedef void *PVOID;
typedef const void *CPVOID;

typedef char *PCHAR;
typedef const char *CPCHAR;

typedef unsigned char BYTE;
typedef unsigned char *PBYTE;
typedef const unsigned char *CPBYTE;

typedef signed char INT8;
typedef unsigned char UINT8;

typedef signed short INT16;
// typedef unsigned short WORD;
typedef unsigned short UINT16;

typedef signed int INT32;
typedef unsigned int UINT32;

// time_t is signed
#define MAX_TIME_T 0x7FFFFFFF

#ifndef HIWORD
#define HIWORD(dw) ((dw)>>16)
#endif
#ifndef LOWORD
#define LOWORD(dw) ((dw)&0xffff)
#endif
#ifndef MAKEDWORD
#define MAKEDWORD(w1,w2) (((w1)<<16)|(w2))
#endif

#ifndef INADDR_NONE
#define INADDR_NONE ((UINT32)0xFFFFFFFF)
#endif
#ifndef INADDR_ANY
#define INADDR_ANY  ((UINT32)0x00000000)
#endif

#if defined(_WIN32) || defined(_SOLARIS)
int inet_aton(const char *, struct in_addr *);
#endif

// Routines to copy and byteswap blocks of memory
// Note: cnt is always the ordinal number of values, NOT bytes
void htons_buf(UINT16 * pnbuf, const void *phbuf, UINT cnt);
void ntohs_buf(UINT16 * phbuf, const void *pnbuf, UINT cnt);
void htonl_buf(UINT32 * pnbuf, const void *phbuf, UINT cnt);
void ntohl_buf(UINT32 * phbuf, const void *pnbuf, UINT cnt);

#endif				//ndef _TYPES_H


