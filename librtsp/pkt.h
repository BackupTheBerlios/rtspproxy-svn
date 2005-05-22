/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _PKT_H
#define _PKT_H

#include "types.h"
#include "str.h"
#include "sock.h"

// Base class for all packets
class CPacket {
      private:			// Unimplemented
	CPacket(const CPacket &);
	 CPacket & operator=(const CPacket &);

      public:
	 CPacket(void);
	 virtual ~ CPacket(void);

	virtual CBuffer *Get(void) = 0;
	virtual bool Set(CBuffer * pbuf) = 0;
};

#endif				//ndef _PKT_H

/** LOG **
 *
 * $Log: pkt.h,v $
 * Revision 1.2  2003/11/17 16:14:08  mat
 * make-up
 *
 *
 */

