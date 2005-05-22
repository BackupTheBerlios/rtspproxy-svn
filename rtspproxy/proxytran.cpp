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

#include "types.h"
#include "dbg.h"

#include "proxytran.h"
#include "proxypkt.h"
#include "rtspproxy.h"
#include "config_parser.h"

#include <assert.h>

// The UDP port range that we will use
#define MIN_UDP_PORT  6970
#define MAX_UDP_PORT 32000

extern config_t global_config;

CProxyTransport::CProxyTransport(CacheSegment *cs):
	CTransport(),
	m_cache_segment( cs ),
        m_nPorts(0),
        m_portBase(0),
        m_pSibling(NULL),
	m_protData(this),
        m_protCtrl(this)
{
	if ( m_cache_segment ) {
		m_cache_segment->parent()->lock_write();
	}
	m_protCtrl.m_trace_rtcp = true;

}

CProxyTransport::~CProxyTransport(void)
{
	/* We free the item so that it can be remove when necessary */
	if ( m_cache_segment && m_cache_segment->parent()->is_locked_write() )
		m_cache_segment->parent()->unlock_write();
}

CacheSegment *CProxyTransport::get_cache_segment()
{
	return m_cache_segment;
}

/*
 * Look through the transport spec (supplied by the client) and bind to the
 * appropriate local port(s), if any.
 */
bool CProxyTransport::Init(int nPorts)
{
	assert(nPorts <= 2);

	if (!nPorts)
		return true;

	bool bRet = false;
	static UINT16 port = MIN_UDP_PORT;
	UINT16 wrap = port;
	do {
		if (m_protData.Init(port)) {
			if (nPorts == 1) {
				bRet = true;
				break;
			}

			if (m_protCtrl.Init(port + 1)) {
				bRet = true;
				break;
			} else {
				m_protData.Close();
			}
		}
		port += 2;
		if (port >= MAX_UDP_PORT)
			port = MIN_UDP_PORT;
	}
	while (port != wrap);

	if (bRet) {
		m_nPorts = nPorts;
		m_portBase = port;
	}

	return bRet;
}

bool CProxyTransport::Init(int nChannels, UINT16 chan,
			   CRtspProtocol * pProt)
{
	assert(nChannels <= 2);

	if (!nChannels)
		return true;

	if (!m_protData.Init(chan, pProt)) {
		return false;
	}

	if (nChannels == 2) {
		if (!m_protCtrl.Init(chan + 1, pProt)) {
			return false;
		}
	}

	m_nPorts = nChannels;
	m_portBase = chan;
	return true;
}

void CProxyTransport::Close(void)
{
	m_protData.Close();

	if (m_nPorts == 2) {
		m_protCtrl.Close();
	}
}

void CProxyTransport::SetPeer(const CString & strHost, UINT16 port)
{
	m_protData.SetPeer(strHost, port);
	if (m_nPorts > 1)
		m_protCtrl.SetPeer(strHost, port + 1);
}

void CProxyTransport::SetPeer(const CInetAddr & host, UINT16 port)
{
	m_protData.SetPeer(host, port);
	if (m_nPorts > 1)
		m_protCtrl.SetPeer(host, port + 1);
}

UINT16 CProxyTransport::GetBasePort(void)
{
	return m_portBase;
}

void CProxyTransport::SendPacket(UINT chan, CPacket * ppkt)
{
	//assert( chan >= m_portBase && chan - m_portBase < m_nPorts );
	assert(ppkt);

	if (chan == 0) {
		m_protData.SendPacket( ppkt );
	} else {
		m_protCtrl.SendPacket( ppkt );
	}
}

void CProxyTransport::SetSibling(CProxyTransport * pSibling)
{
	m_pSibling = pSibling;
}

inline bool is_odd(uint32_t n) {
	return ( n & 01L );
}

void CProxyTransport::OnPacket(UINT chan, CBuffer * pbuf)
{
	CProxyPacket pkt;
	pkt.Set(pbuf);
	m_pSibling->SendPacket( is_odd( chan ), &pkt);
}

void CProxyTransport::set_cache_segment( CacheSegment *cs )
{
	assert( cs );
	m_cache_segment = cs;
	// dbg("CProxyTransport: setting cache segment : 0x%x\n", cs);
	if ( ! m_cache_segment->parent()->is_locked_write() )
		m_cache_segment->parent()->lock_write();
}


/**************************************
 *
 * Passthru protocol class
 *
 **************************************/

CProxyTransport::CPassthruProtocol::CPassthruProtocol(CProxyTransport * pOwner):
	m_pOwner( pOwner ),
        m_pSock( NULL ),
        m_chan( 0 ),
	m_trace_rtcp( false )
{
	// Empty
}

CProxyTransport::CPassthruProtocol::~CPassthruProtocol(void)
{
	Close();
}

bool CProxyTransport::CPassthruProtocol::Init(UINT16 port)
{
	// dbg("CProxyTransport::CPassthruProtocol::Init()\n");
	m_pSock = new CUdpSocket(this);
	m_chan = port;

	CSockAddr addr(CInetAddr::Any(), port);
	if (((CUdpSocket *) m_pSock)->Bind(addr)) {
		return true;
	}

	delete m_pSock;
	m_pSock = NULL;
	return false;
}

bool CProxyTransport::CPassthruProtocol::Init(UINT16 chan,
					      CRtspProtocol * pProt)
{
	m_pSock = new CRtspInterleavedSocket(this);
	m_chan = chan;
	return ((CRtspInterleavedSocket *) m_pSock)->Connect(chan, pProt);
}

void CProxyTransport::CPassthruProtocol::Close()
{
	if (m_pSock) {
		m_pSock->Close();
		delete m_pSock;
		m_pSock = NULL;
	}
}

void CProxyTransport::CPassthruProtocol::SetPeer(const CString & strHost,
						 UINT16 port)
{
	((CUdpSocket *) m_pSock)->Connect(CSockAddr(strHost, port));
	m_pSock->Select(SF_READ);
}

void CProxyTransport::CPassthruProtocol::SetPeer(const CInetAddr & host,
						 UINT16 port)
{
	((CUdpSocket *) m_pSock)->Connect(CSockAddr(host, port));
	m_pSock->Select(SF_READ);
}

extern "C" void process_rtcp_packet( void *, uint32_t);

void CProxyTransport::CPassthruProtocol::SendPacket(CPacket * ppkt)
{
	CProxyPacket *pproxypkt;
	pproxypkt = dynamic_cast < CProxyPacket * >(ppkt);
	assert_or_ret( pproxypkt );

	CBuffer *pbuf = pproxypkt->Get();
	assert(pbuf);
	void *buf = pbuf->GetBuffer();
	uint32_t size = pbuf->GetSize();

	assert( m_pSock );
	m_pSock->Write( buf, size );

	if ( ! global_config.cache_enable )
		return;

	// Here we save the packet to disk-cache (if cache is enabled)
	if ( m_trace_rtcp == true ) {
		// This packet is RTCP...
		/// process_rtcp_packet( pbuf->GetBuffer(), pbuf->GetSize() );
		return;
	}

	CacheSegment *cs = m_pOwner->get_cache_segment();
	if ( cs != NULL )
		m_pOwner->get_cache_segment()->add_packet( buf, size );
}

void CProxyTransport::CPassthruProtocol::OnConnectDone(int err)
{
	assert(false);
}

void CProxyTransport::CPassthruProtocol::OnReadReady(void)
{
	CBuffer *buf = new CBuffer();
	buf->SetSize(MAX_UDP_LEN + 1);
	size_t len = 0;

	assert( m_pOwner );

	len = m_pSock->Read(buf->GetBuffer(), MAX_UDP_LEN + 1);
	if ( len ) {
		if (len <= MAX_UDP_LEN + 1) {
			buf->SetSize( len );
			m_pOwner->OnPacket(m_chan, buf);
		} else {
			// dbg("CProxyTransport::CPassthruProtocol::OnReadReady: "
			//       "UDP packet too large\n");
		}
	}
}

void CProxyTransport::CPassthruProtocol::OnWriteReady(void)
{
	assert(false);
}

void CProxyTransport::CPassthruProtocol::OnExceptReady(void)
{
	assert(false);
}

void CProxyTransport::CPassthruProtocol::OnClosed(void)
{
	dbg( "Connection closed.\n" );
}


/** LOG **
 *
 * $Log: proxytran.cpp,v $
 * Revision 1.3  2003/11/17 16:14:16  mat
 * make-up
 *
 *
 */

