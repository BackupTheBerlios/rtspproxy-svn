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

#include "../libapp/types.h"
#include "../libapp/str.h"
#include "../libapp/sock.h"

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



