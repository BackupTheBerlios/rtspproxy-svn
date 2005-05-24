/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _DBG_H
#define _DBG_H

#include <assert.h>
#include "types.h"

void dbg(const char *fmt, ...);
void open_log_file();
void close_log_file();

inline void dbgout(const char *fmt, ...)
{

}

inline void dump_alloc_heaps(void)
{
}

#define assert_or_ret(cond) { assert(cond); if( !(cond) ) return; }
#define assert_or_retv(val,cond) { assert(cond); if( !(cond) ) return (val); }

#endif				//ndef _DBG_H

/** LOG **
 *
 * $Log: dbg.h,v $
 * Revision 1.2  2003/11/17 16:14:02  mat
 * make-up
 *
 *
 */

