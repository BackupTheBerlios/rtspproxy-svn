/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */
 
/* Caching support is 
 * Copyright (C) 2003  Matteo Merli <matteo.merli@studenti.unipr.it>
 * 
 * $Id$
 */

#ifndef _PROXYTRAN_H
#define _PROXYTRAN_H

#include "../librtsp/tran.h"
#include "../librtsp/rtspprot.h"

#include "../cache_manager/cache_segment.h"

class CPassthruProtocol;

class CProxyTransport : public CTransport {
  public:		      
	 CProxyTransport(CacheSegment *cs=0);
	 CProxyTransport(const CProxyTransport &);
	 CProxyTransport & operator=(const CProxyTransport &);
	

	 virtual ~ CProxyTransport(void);
	 
	 bool Init(int nPorts);
	 bool Init(int nChannels, UINT16 chan, CRtspProtocol * pProt);
	 void Close();
	 void SetPeer(const CString & strHost, UINT16 port);
	 void SetPeer(const CInetAddr & host, UINT16 port);

	 void SetSibling(CProxyTransport * pSibling);
	 
	 UINT16 GetBasePort(void);

	 virtual void SendPacket(UINT chan, CPacket * ppkt);
	 
	 void OnPacket(UINT chan, CBuffer * pbuf);

	 void set_cache_segment( CacheSegment *cs );

	 /*! Returns the instance of the cache segment associated with the Transport. */
	 CacheSegment *get_cache_segment();


 protected:
	 /*! Cache segment object */ 
	 CacheSegment *m_cache_segment;
	 
	 u_int16_t id;
	 

	 UINT m_nPorts;
	 UINT16 m_portBase;

	 CProxyTransport *m_pSibling;
	 
 protected:
	 class CPassthruProtocol:public CStreamResponse {
	 public:
		 CPassthruProtocol(CProxyTransport * pOwner);
		 virtual ~ CPassthruProtocol(void);
		 
		 bool Init(UINT16 port);
		 bool Init(UINT16 chan, CRtspProtocol * pProt);
		 
		 void Close();
		 
		 void SetPeer(const CString & strHost, UINT16 port);
		 void SetPeer(const CInetAddr & host, UINT16 port);
		 
		 void SendPacket(CPacket * ppkt);
		
		 virtual void OnConnectDone(int err);
		 virtual void OnReadReady(void);
		 virtual void OnWriteReady(void);
		 virtual void OnExceptReady(void);
		 virtual void OnClosed(void);
		 
	 protected:
		 CProxyTransport * m_pOwner;
		 CSocket *m_pSock;
		 UINT16 m_chan;
	public:
		 bool m_trace_rtcp;
	 };

 protected:
	 CPassthruProtocol m_protData;
	 CPassthruProtocol m_protCtrl;	// RTP only
	 
};

#endif				//ndef _PROXYTRAN_H


