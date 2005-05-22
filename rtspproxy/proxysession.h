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
 
#ifndef PROXYSESSION_H
#define PROXYSESSION_H

#include "proxytran.h"
#include "string.h"
#include "tlist.h"

// the session header my contain sessionID and timeout
class CSessionHdr {
      private:
	CSessionHdr(void);

      public:
	CSessionHdr(const CString & strSession);
	~CSessionHdr(void);

	 CString & GetSessionID(void);
	void SetSessionID(CString & strSessionID);
	 CString & GetSessionHdrString(void);

      private:
	 CString m_strSessionID;
	CString m_strTimeout;
	CString m_strSessionHdr;
};

class CProxyDataTunnel {
      public:
	CProxyDataTunnel(CacheSegment *cs=0);
	~CProxyDataTunnel(void);

	void AddRef(void);
	UINT Release(void);


	bool Init(int nPorts);
	bool Init(CRtspProtocol * pClientProt, CRtspProtocol * pServerProt,
		  int nChannels, UINT16 clientChannel,
		  UINT16 serverChannel);

	void SetupTunnel(void);
	bool IsSetup(void);

	void SetClientAddr(const CInetAddr & addr);
	void SetServerAddr(const CInetAddr & addr);

	UINT16 GetClientPort(void);
	void SetClientPort(UINT16 clientPort);

	UINT16 GetServerPort(void);
	void SetServerPort(UINT16 serverPort);

	UINT16 GetProxyToClientPort(void);
	UINT16 GetProxyToServerPort(void);

	void SetProxyToClientPort(UINT16 proxyToClientPort);
	void SetProxyToServerPort(UINT16 proxyToServerPort);

	void set_cache_segment(CacheSegment *p_cs);
	
	/** This flag is used to resolve conflicts with DSS */
	bool m_used;
	
	CString m_url;

      protected:
	 bool m_bSetup;
	UINT m_refCount;
	bool m_bInterleaved;

	UINT16 m_clientPort;
	UINT16 m_serverPort;
	UINT16 m_proxyToClientPort;
	UINT16 m_proxyToServerPort;

	CInetAddr m_addrClient;
	CInetAddr m_addrServer;

	CProxyTransport m_clientSideTran;
	CProxyTransport m_serverSideTran;

	CacheSegment *m_cache_segment;
};

typedef TDoubleList < CProxyDataTunnel * >CProxyDataTunnelList;

class CRtspProxySession {
 public:
	/*! If is present a cache segment, it will be used. */
	CRtspProxySession(CacheSegment *cs=0);
	~CRtspProxySession(void);

	const CString & GetServerSessionID(void) const;
	const CString & GetClientSessionID(void) const;
	const CString & GetSetupCSeq(void) const;
	const CString & GetHost(void) const;

	void SetSetupCSeq(CString strSetupCSeq);
	void SetSessionID(const CString & strSessionID,
			  const CString & strHost, UINT16 sessionIndex);

	void AddTunnel(CProxyDataTunnel * pTunnel);
	void ReleaseAllTunnels(void);
	CProxyDataTunnel *FindTunnelByClientPort(UINT16 clientPort);
	CProxyDataTunnel *FindTunnelByProxyPort(UINT16 proxyToServerPort, char* url=NULL);

	// void set_cache_segment(CacheSegment *p_cs) {m_cache_segment = p_cs;}

 protected:
	CacheSegment *m_cache_segment;

	CString m_serverSessionID;
	CString m_clientSessionID;
	CString m_strHost;
	CString m_setupCSeq;
	CProxyDataTunnelList m_listTunnel;
};

typedef TDoubleList < CRtspProxySession * >CRtspProxySessionList;


#endif

/** LOG **
 *
 * $Log: proxysession.h,v $
 * Revision 1.3  2003/11/17 16:14:16  mat
 * make-up
 *
 *
 */

