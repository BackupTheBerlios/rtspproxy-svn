/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _PROXYPKT_H
#define _PROXYPKT_H

#include "types.h"
#include "str.h"
#include "sock.h"
#include "pkt.h"

class CProxyPacket:public CPacket {
      private:			// Unimplemented
	CProxyPacket(const CProxyPacket &);
	CProxyPacket & operator=(const CProxyPacket &);

      public:
	 CProxyPacket(void);
	 virtual ~ CProxyPacket(void);

	virtual CBuffer *Get(void);
	virtual bool Set(CBuffer * pbuf);

      protected:
	 CBuffer * m_pbuf;
};

#endif				//ndef _PROXYPKT_H

/** LOG **
 *
 * $Log: proxypkt.h,v $
 * Revision 1.2  2003/11/17 16:14:08  mat
 * make-up
 *
 *
 */

