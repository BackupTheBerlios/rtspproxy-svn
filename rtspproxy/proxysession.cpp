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


#include "proxysession.h"
#include "rtspproxy.h"
#include "dbg.h"

extern config_t global_config;

/**************************************
 *
 * CSessionHdr class
 *
 **************************************/
CSessionHdr::CSessionHdr(const CString & strSession)
{
	CPCHAR pSess = strSession;
	size_t nSessLen = strlen(pSess);
	CPCHAR pSemi = strchr(pSess, ';');
	if (pSemi != NULL) {
		m_strTimeout = pSemi + 1;
		nSessLen = pSemi - pSess;
	}

	m_strSessionID.Set(strSession, nSessLen);
}

CSessionHdr::~CSessionHdr(void)
{
	//empty
}

CString & CSessionHdr::GetSessionID(void)
{
	return m_strSessionID;
}

void CSessionHdr::SetSessionID(CString & strSessionID)
{
	m_strSessionID = strSessionID;
}

CString & CSessionHdr::GetSessionHdrString(void)
{
	if (m_strTimeout.IsEmpty()) {
		return m_strSessionID;
	}

	m_strSessionHdr.Append(m_strSessionID);
	m_strSessionHdr.Append(";");
	m_strSessionHdr.Append(m_strTimeout);

	return m_strSessionHdr;
}


/**************************************
 *
 * CProxyDataTunnel class
 *
 **************************************/

CProxyDataTunnel::CProxyDataTunnel(CacheSegment *cs):
	m_bSetup(false),
        m_refCount(0),
        m_bInterleaved(false),
        m_clientPort(0),
        m_serverPort(0),
        m_proxyToClientPort(0),
        m_proxyToServerPort(0),
        m_cache_segment(cs)
{
	if ( cs ) {
		m_clientSideTran.set_cache_segment( cs );
		// dbg("CProxyDataTunnel - Using cache\n");
	} else ;//dbg("CProxyDataTunnel - NOT Using cache\n");
	
	m_used = false;
}

CProxyDataTunnel::~CProxyDataTunnel(void)
{
	//empty;
}

void CProxyDataTunnel::AddRef(void)
{
	m_refCount++;
}

UINT CProxyDataTunnel::Release(void)
{
	return --m_refCount;
}

bool CProxyDataTunnel::IsSetup(void)
{
	return m_bSetup;
}

bool CProxyDataTunnel::Init(int nPorts)
{
	if ( ! m_clientSideTran.Init(nPorts) )
		return false;
	if ( ! m_serverSideTran.Init(nPorts) ) {
		m_clientSideTran.Close();
		return false;
	}

	if ( global_config.cache_enable && m_cache_segment )
		m_clientSideTran.set_cache_segment( m_cache_segment );

	m_proxyToClientPort = m_clientSideTran.GetBasePort();
	m_proxyToServerPort = m_serverSideTran.GetBasePort();

	return true;
}

bool CProxyDataTunnel::Init(CRtspProtocol * pClientProt,
			    CRtspProtocol * pServerProt, int nChannels,
			    UINT16 clientChannel, UINT16 serverChannel)
{
	m_bInterleaved = true;
	m_clientPort = m_proxyToClientPort = clientChannel;
	m_serverPort = m_proxyToServerPort = serverChannel;

	if (!m_clientSideTran.Init(nChannels, clientChannel, pClientProt))
		return false;

	if (!m_serverSideTran.Init(nChannels, serverChannel, pServerProt))
		return false;

	if (/*global_config.cache_enable && */m_cache_segment)
		m_clientSideTran.set_cache_segment(m_cache_segment);

	return true;
}

UINT16 CProxyDataTunnel::GetClientPort(void)
{
	return m_clientPort;
}

void CProxyDataTunnel::SetClientAddr(const CInetAddr & addrClient)
{
	m_addrClient = addrClient;
}

void CProxyDataTunnel::SetServerAddr(const CInetAddr & addrServer)
{
	m_addrServer = addrServer;
}

void CProxyDataTunnel::SetupTunnel(void)
{
	if (!m_bInterleaved) {
		m_clientSideTran.SetPeer(m_addrClient, m_clientPort);
		m_serverSideTran.SetPeer(m_addrServer, m_serverPort);
	}
	m_clientSideTran.SetSibling(&m_serverSideTran);
	m_serverSideTran.SetSibling(&m_clientSideTran);

	m_bSetup = true;
}

void CProxyDataTunnel::SetClientPort(UINT16 clientPort)
{
	m_clientPort = clientPort;
}

UINT16 CProxyDataTunnel::GetServerPort(void)
{
	return m_serverPort;
}

void CProxyDataTunnel::SetServerPort(UINT16 serverPort)
{
	m_serverPort = serverPort;
}

UINT16 CProxyDataTunnel::GetProxyToClientPort(void)
{
	return m_proxyToClientPort;
}

void CProxyDataTunnel::SetProxyToClientPort(UINT16 proxyToClientPort)
{
	m_proxyToClientPort = proxyToClientPort;
}

UINT16 CProxyDataTunnel::GetProxyToServerPort(void)
{
	return m_proxyToServerPort;
}

void CProxyDataTunnel::SetProxyToServerPort(UINT16 proxyToServerPort)
{
	m_proxyToServerPort = proxyToServerPort;
}

void CProxyDataTunnel::set_cache_segment(CacheSegment *p_cs)
{
	assert( p_cs );
	m_cache_segment = p_cs;
	// dbg("CProxyDataTunnel setting cache segment : %x\n", p_cs);
	m_clientSideTran.set_cache_segment( m_cache_segment );
}

/**************************************
 *
 * CRtspProxySession class
 *
 **************************************/

CRtspProxySession::CRtspProxySession(CacheSegment *cs)
{
	m_cache_segment = cs;
	// if (m_cache_segment)
	// 	OutputDebugInfo("CRtspProxySession - Using cache");
	// else OutputDebugInfo("CRtspProxySession - NOT Using cache");
}

CRtspProxySession::~CRtspProxySession(void)
{
	// Empty
}

const CString & CRtspProxySession::GetServerSessionID(void) const
{
	return m_serverSessionID;
}

const CString & CRtspProxySession::GetClientSessionID(void) const
{
	return m_clientSessionID;
}

const CString & CRtspProxySession::GetSetupCSeq(void) const
{
	return m_setupCSeq;
}

const CString & CRtspProxySession::GetHost(void) const
{
	return m_strHost;
}

void CRtspProxySession::SetSetupCSeq(CString strSetupCSeq)
{
	m_setupCSeq = strSetupCSeq;
}

void CRtspProxySession::SetSessionID(const CString & strSessionID,
				     const CString & strHost,
				     UINT16 sessionIndex)
{
	m_serverSessionID = strSessionID;
	m_strHost = strHost;
	m_clientSessionID = strSessionID;

	char szSessionIndex[20];
	sprintf(szSessionIndex, "%u", sessionIndex);
	m_clientSessionID.Append(szSessionIndex);
}

void CRtspProxySession::AddTunnel(CProxyDataTunnel * pTunnel)
{
	if (global_config.cache_enable && m_cache_segment) {
 		assert(m_cache_segment);
		pTunnel->set_cache_segment( m_cache_segment );
	}
	dbg("Adding Tunnel - port %u\n\n", pTunnel->GetProxyToServerPort());
	m_listTunnel.InsertTail(pTunnel);
}

void CRtspProxySession::ReleaseAllTunnels(void)
{
	CProxyDataTunnel *pTunnel;
	while (!m_listTunnel.IsEmpty()) {
		pTunnel = m_listTunnel.RemoveHead();
		if (pTunnel->Release() == 0) {
			delete pTunnel;
		}
	}
}

CProxyDataTunnel *CRtspProxySession::
FindTunnelByClientPort(UINT16 clientPort)
{
	CProxyDataTunnel *pTunnel;
	CProxyDataTunnelList::Iterator itr(m_listTunnel.Begin());
	while (itr) {
		pTunnel = *itr;
		if (pTunnel->GetClientPort() == clientPort) {
			return pTunnel;
		}
		itr++;
	}
	return NULL;
}

CProxyDataTunnel *CRtspProxySession::
FindTunnelByProxyPort(UINT16 proxyToServerPort, char* url)
{
	CProxyDataTunnel *pTunnel;
	CProxyDataTunnelList::Iterator itr(m_listTunnel.Begin());
	
	int count = m_listTunnel.GetCount();
	if ( count == 1 ) {
	  	(*itr)->m_used = true;
		return (*itr);
	}

	while (itr) {
		pTunnel = *itr;
		if ( pTunnel->GetProxyToServerPort() == proxyToServerPort ) {
		  	printf("Found tunnel!\n");
			pTunnel->m_used = true;
			return pTunnel;
		}
		itr++;
	}
	
	if ( url ) {
		itr = m_listTunnel.Begin();
		while ( itr ) {
			pTunnel = (*itr);
			if ( ! strcmp( pTunnel->m_url, url )  ) {
				return pTunnel;
			}
			++itr;
		}
	}
	
	itr = m_listTunnel.Begin();
	while ( itr ) {
		pTunnel = (*itr);
		if ( ! (pTunnel->m_used) ) {
			pTunnel->m_used = true;
			return pTunnel;
		}
		++itr;
	}
	
	dbg(" No Tunnel Found for port: %u\n\n", proxyToServerPort );
	return NULL;
}

/** LOG **
 *
 * $Log: proxysession.cpp,v $
 * Revision 1.3  2003/11/17 16:14:16  mat
 * make-up
 *
 *
 */

