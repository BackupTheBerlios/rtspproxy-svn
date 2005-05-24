/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _TRAN_H
#define _TRAN_H

#include "../libapp/types.h"
#include "pkt.h"

// Response for data transports
class CTransportResponse {
      public:
	virtual void OnPacket(CPacket * ppkt) = 0;
};

// Virtual base class for all data transports
class CTransport {
      private:			// Unimplemented
	CTransport(const CTransport &);
	 CTransport & operator=(const CTransport &);

      public:
	enum Type {
		ttNONE = 0,
		ttNULL = 1,
		ttRDT = 2,
		ttRTP = 4,
		ttPNG = 8,
		ttALL = 15
	};
	enum Layer {
		ltNONE = 0,
		ltNULL = 1,
		ltMCAST = 2,
		ltUDP = 4,
		ltTCP = 8,
		ltALL = 15
	};

      protected:
	 CTransport(void);
	 CTransport(CTransportResponse * pResponse, Type tt, Layer lt);

      public:
	 virtual ~ CTransport(void);

	virtual void SendPacket(UINT chan, CPacket * ppkt) = 0;

      protected:
	 CTransportResponse * m_pResponse;
	Type m_type;
	Layer m_layer;
};

#endif				//ndef _TRAN_H

/** LOG **
 *
 * $Log: tran.h,v $
 * Revision 1.2  2003/11/17 16:14:08  mat
 * make-up
 *
 *
 */

