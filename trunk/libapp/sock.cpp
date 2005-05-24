/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifdef _UNIX
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <fcntl.h>
#include <errno.h>
#endif

#include "dbg.h"
#include "sock.h"
#include "thread.h"

#if defined(_UNIX)
#include <sys/ioctl.h>
#define SOCK_LAST_ERROR() errno
#define SOCKET_ERROR -1
#define closesocket close
#define ioctlsocket ioctl
typedef int sioctl_t;
#endif
#if defined(_WIN32)
#define SOCK_LAST_ERROR() ::WSAGetLastError()
typedef unsigned long sioctl_t;
#endif

#if defined(_WIN32) || defined(_SOLARIS)
int inet_aton(const char *cp, struct in_addr *inp)
{
	UINT32 addr = inet_addr(cp);
	if (addr == INADDR_NONE)
		return 0;
	inp->s_addr = addr;
	return 1;
}
#endif

#if defined(_UNIX)
static void socket_nonblock(sockobj_t sock)
{
	int tmp;
	fcntl(sock, F_GETFL, &tmp);
	tmp |= O_NONBLOCK;
	fcntl(sock, F_SETFL, &tmp);
}
#endif
#if defined(_WIN32)
static void socket_nonblock(sockobj_t sock)
{
	sioctl_t tmp = 1;
	ioctlsocket(sock, FIONBIO, &tmp);
}
#endif

static void socket_reuseaddr(sockobj_t sock)
{
	int tmp = 1;
	setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, (const char *) &tmp,
		   sizeof(tmp));
}

/**************************************
 *
 * CInetAddr
 *
 **************************************/

CInetAddr::CInetAddr(void)
{
	m_addr.sin_family = AF_INET;
	m_addr.sin_addr.s_addr = INADDR_NONE;
	m_addr.sin_port = 0;
}

CInetAddr::CInetAddr(const in_addr & host)
{
	m_addr.sin_family = AF_INET;
	m_addr.sin_addr = host;
	m_addr.sin_port = 0;
}

CInetAddr::CInetAddr(CPCHAR szHost)
{
	SetHost(szHost);
}

bool CInetAddr::IsValid(void) const
{
	return (m_addr.sin_addr.s_addr != INADDR_NONE);
}

in_addr CInetAddr::GetHost(void) const
{
	return m_addr.sin_addr;
}

void CInetAddr::SetHost(const in_addr & host)
{
	m_addr.sin_addr = host;
}

void CInetAddr::SetHost(CPCHAR szHost)
{
	m_addr.sin_addr.s_addr = INADDR_NONE;

	Resolve(szHost, &m_addr.sin_addr);
}

CInetAddr CInetAddr::Any(void)
{
	CInetAddr addr;
	addr.m_addr.sin_addr.s_addr = INADDR_ANY;
	return addr;
}

CInetAddr CInetAddr::None(void)
{
	CInetAddr addr;
	addr.m_addr.sin_addr.s_addr = INADDR_NONE;
	return addr;
}

bool CInetAddr::Resolve(CPCHAR szHost, in_addr * phost)
{
	bool bRet = false;
	bool bIsName = false;

	if (NULL == szHost)
		return false;

	// Determine if this is a number or name
	CPCHAR p = szHost;
	while (*p) {
		if ((*p < '0' || *p > '9') && *p != '.') {
			bIsName = true;
			break;
		}
		p++;
	}

	if (bIsName) {
		hostent *phent;
		phent = gethostbyname(szHost);
		if (phent) {
			memcpy(phost, phent->h_addr, sizeof(in_addr));
			bRet = true;
		}
	} else {
		bRet = (0 != inet_aton(szHost, phost));
	}

	return bRet;
}

/**************************************
 *
 * CSockAddr
 *
 **************************************/

CSockAddr::CSockAddr(void):
CInetAddr()
{
	// Empty
}

CSockAddr::CSockAddr(const sockaddr_in & addr):CInetAddr()
{
	SetAddr(addr);
}

CSockAddr::CSockAddr(const in_addr & host, UINT16 port /* = 0 */ ):
CInetAddr(host)
{
	SetAddr(host, port);
}

CSockAddr::CSockAddr(CPCHAR szHost, UINT16 port /* = 0 */ ):
CInetAddr()
{
	SetAddr(szHost, port);
}

sockaddr_in CSockAddr::GetAddr(void) const
{
	return m_addr;
}

void CSockAddr::SetAddr(const sockaddr_in & addr)
{
	m_addr = addr;
}

void CSockAddr::SetAddr(const in_addr & host, UINT16 port)
{
	SetHost(host);
	m_addr.sin_port = htons(port);
}

void CSockAddr::SetAddr(CPCHAR szHost, UINT16 port)
{
	SetHost(szHost);
	m_addr.sin_port = htons(port);
}

UINT16 CSockAddr::GetPort(void) const
{
	return ntohs(m_addr.sin_port);
}

void CSockAddr::SetPort(UINT16 port)
{
	m_addr.sin_port = htons(port);
}

CSockAddr CSockAddr::Any(void)
{
	CSockAddr addr;
	addr.m_addr.sin_addr.s_addr = INADDR_ANY;
	addr.m_addr.sin_port = 0;
	return addr;
}

CSockAddr CSockAddr::None(void)
{
	CSockAddr addr;
	addr.m_addr.sin_addr.s_addr = INADDR_NONE;
	addr.m_addr.sin_port = 0xFFFF;
	return addr;
}

/**************************************
 *
 * CSocket
 *
 **************************************/

CSocket::CSocket(void):
CStream(),
m_sock(INVALID_SOCKET), m_uSelectFlags(SF_NONE), m_err(SOCKERR_NONE)
{
	// Empty
}

CSocket::CSocket(CStreamResponse * pResponse):
CStream(pResponse),
m_sock(INVALID_SOCKET), m_uSelectFlags(SF_NONE), m_err(SOCKERR_NONE)
{
	// Empty
}

CSocket::~CSocket(void)
{
	Close();
}

bool CSocket::IsOpen(void)
{
	return (INVALID_SOCKET != m_sock);
}

void CSocket::Close(void)
{
	if (IsOpen()) {
		Select(SF_NONE);
		closesocket(m_sock);
		m_sock = INVALID_SOCKET;
		if (m_pResponse)
			m_pResponse->OnClosed();
	}
}

size_t CSocket::Read(PVOID pbuf, size_t nLen)
{
	assert_or_retv(SOCKERR_EOF, (pbuf != NULL && IsOpen()));

	m_err = SOCKERR_NONE;
	ssize_t n = recv(m_sock, (char *) pbuf, nLen, 0);
	// For TCP sockets...
	// If recv() returns zero, the remote end closed gracefully
	// If we get EPIPE/WSAECONNRESET, the remote end has closed, but there
	// may be more data left to read
	if (n == 0) {
		n = SOCKERR_EOF;
	} else if (n == SOCKET_ERROR) {
		n = 0;
		m_err = SOCK_LAST_ERROR();
		if (m_err != SOCKERR_WOULDBLOCK) {
			n = SOCKERR_EOF;
		}
	}

	// FIXME
	// #warning tcp_logging!
	// if (log_tcp)
	//	fprintf(stderr, "%s\n", pbuf);

	return n;
}

size_t CSocket::Write(CPVOID pbuf, size_t nLen)
{
	assert_or_retv(0, (pbuf != NULL && IsOpen()));

	m_err = SOCKERR_NONE;
	ssize_t n = send(m_sock, (const char *) pbuf, nLen, 0);
	if (n == SOCKET_ERROR) {
		n = 0;
		m_err = SOCK_LAST_ERROR();
		if (m_err != SOCKERR_WOULDBLOCK) {
			n = SOCKERR_EOF;
		}
	}

	return n;
}

CSockAddr CSocket::GetLocalAddr(void)
{
	CSockAddr addr;
	sockaddr_in sa;
	socklen_t salen;

	salen = sizeof(sa);
	if (getsockname(m_sock, (sockaddr *) & sa, &salen) == 0) {
		addr.SetAddr(sa);
	}

	return addr;
}

CSockAddr CSocket::GetPeerAddr(void)
{
	CSockAddr addr;
	sockaddr_in sa;
	socklen_t salen;

	salen = sizeof(sa);
	if (getpeername(m_sock, (sockaddr *) & sa, &salen) == 0) {
		addr.SetAddr(sa);
	}

	return addr;
}

bool CSocket::Select(UINT32 nWhich)
{
	assert(IsOpen() || SF_NONE == nWhich);
	assert(m_pResponse || SF_NONE == nWhich || SF_ACCEPT == nWhich);

	if (nWhich != m_uSelectFlags) {
		CEventThread *pSelf;
#ifdef NO_RTTI
		pSelf = (CEventThread *) CThread::This();	//XXX: very bad, upgrade compiler
#else
		pSelf = dynamic_cast < CEventThread * >(CThread::This());
#endif
		assert_or_retv(false, pSelf);

		if (SF_NONE == m_uSelectFlags) {
			if (!pSelf->AddStream(this))
				return false;
		}
		m_uSelectFlags = nWhich;
		pSelf->SetStreamSelect(this, nWhich);
		if (SF_NONE == nWhich) {
			pSelf->DelStream(this);
		}
	}
	return true;
}

sockerr_t CSocket::LastError(void)
{
	return m_err;
}

/**************************************
 *
 * CListenSocket
 *
 **************************************/

CListenSocket::CListenSocket(void):
CSocket(), m_pAcceptResponse(NULL)
{
	// Empty
}

CListenSocket::CListenSocket(CListenSocketResponse * pResponse):
CSocket(), m_pAcceptResponse(pResponse)
{
	// Empty
}

CListenSocket::~CListenSocket(void)
{
	// Empty
}

void CListenSocket::SetResponse(CListenSocketResponse * pResponse)
{
	assert(pResponse || !IsOpen());

	m_pAcceptResponse = pResponse;
}

bool CListenSocket::Listen(const CSockAddr & addr)
{
	assert_or_retv(false, (m_pAcceptResponse && !IsOpen()));

	m_err = SOCKERR_NONE;
	m_sock = socket(AF_INET, SOCK_STREAM, IPPROTO_IP);
	if (m_sock == INVALID_SOCKET) {
		m_err = SOCK_LAST_ERROR();
		return false;
	}

	socket_nonblock(m_sock);
	socket_reuseaddr(m_sock);

	sockaddr_in bindaddr = addr.GetAddr();
	if (0 != bind(m_sock, (sockaddr *) & bindaddr, sizeof(bindaddr))) {
		m_err = SOCK_LAST_ERROR();
		closesocket(m_sock);
		m_sock = INVALID_SOCKET;
		return false;
	}
	//XXX: Is there a performance penalty for SOMAXCONN?
	if (0 != listen(m_sock, SOMAXCONN)) {
		m_err = SOCK_LAST_ERROR();
		closesocket(m_sock);
		m_sock = INVALID_SOCKET;
		return false;
	}

	Select(SF_ACCEPT);

	return true;
}

/**************************************
 *
 * CTcpSocket
 *
 **************************************/

CTcpSocket::CTcpSocket(void):
CSocket()
{
	// Empty
	log_tcp = true;
}

CTcpSocket::CTcpSocket(CStreamResponse * pResponse):
CSocket(pResponse)
{
	// Empty
}

CTcpSocket::~CTcpSocket(void)
{
	// Empty
}

bool CTcpSocket::Connect(const CSockAddr & addr)
{
	assert_or_retv(false, (m_pResponse && !IsOpen()));

	m_err = SOCKERR_NONE;
	m_sock = socket(AF_INET, SOCK_STREAM, IPPROTO_IP);
	if (m_sock == INVALID_SOCKET) {
		m_err = SOCK_LAST_ERROR();
		return false;
	}

	socket_nonblock(m_sock);
	socket_reuseaddr(m_sock);

	sockaddr_in cnxaddr = addr.GetAddr();
	if (0 == connect(m_sock, (sockaddr *) & cnxaddr, sizeof(cnxaddr))) {
		m_pResponse->OnConnectDone(m_err);
		return true;
	}

	int cnxerr = SOCK_LAST_ERROR();
	if (cnxerr != SOCKERR_INPROGRESS && cnxerr != SOCKERR_WOULDBLOCK) {
		m_err = cnxerr;
		closesocket(m_sock);
		m_sock = INVALID_SOCKET;
		return false;
	}

	Select(SF_CONNECT);

	return true;
}

/**************************************
 *
 * CUdpSocket
 *
 **************************************/

CUdpSocket::CUdpSocket(void):
CSocket()
{
	// Empty
}

CUdpSocket::CUdpSocket(CStreamResponse * pResponse):
CSocket(pResponse)
{
	// Empty
}

CUdpSocket::~CUdpSocket(void)
{
	// Empty
}

bool CUdpSocket::Bind(const CSockAddr & addr)
{
	assert_or_retv(false, !IsOpen());

	m_err = SOCKERR_NONE;
	m_sock = socket(AF_INET, SOCK_DGRAM, IPPROTO_IP);
	if (m_sock == INVALID_SOCKET) {
		m_err = SOCK_LAST_ERROR();
		return false;
	}

	socket_nonblock(m_sock);

	sockaddr_in bindaddr = addr.GetAddr();
	if (0 != bind(m_sock, (sockaddr *) & bindaddr, sizeof(bindaddr))) {
		m_err = SOCK_LAST_ERROR();
		closesocket(m_sock);
		m_sock = INVALID_SOCKET;
		return false;
	}

	return true;
}

bool CUdpSocket::Connect(const CSockAddr & addr)
{
	assert_or_retv(false, IsOpen());

	m_err = SOCKERR_NONE;
	sockaddr_in cnxaddr = addr.GetAddr();
	if (0 != connect(m_sock, (sockaddr *) & cnxaddr, sizeof(cnxaddr))) {
		m_err = SOCK_LAST_ERROR();
		return false;
	}

	return true;
}

size_t CUdpSocket::RecvFrom(CSockAddr * paddr, PVOID pbuf, size_t nLen)
{
	assert_or_retv(0, (paddr != NULL && pbuf != NULL && IsOpen()));

	m_err = SOCKERR_NONE;
	sockaddr_in recvaddr;
	socklen_t salen = sizeof(recvaddr);
	ssize_t n =
	    recvfrom(m_sock, (char *) pbuf, nLen, 0,
		     (sockaddr *) & recvaddr, &salen);
	if (n > 0) {
		paddr->SetAddr(recvaddr);
	} else if (n == 0) {
		dbgout("*** recvfrom() returned zero ***");
		n = SOCKERR_EOF;
	} else if (n == SOCKET_ERROR) {
		n = 0;
		m_err = SOCK_LAST_ERROR();
		if (m_err != SOCKERR_WOULDBLOCK) {
			n = SOCKERR_EOF;
		}
	}

	return n;
}

size_t CUdpSocket::SendTo(const CSockAddr & addr, CPVOID pbuf, size_t nLen)
{
	assert_or_retv(0, (pbuf != NULL && IsOpen()));

	m_err = SOCKERR_NONE;
	sockaddr_in sendaddr = addr.GetAddr();
	ssize_t n =
	    sendto(m_sock, (const char *) pbuf, nLen, 0,
		   (sockaddr *) & sendaddr, sizeof(sendaddr));
	if (n == SOCKET_ERROR) {
		n = 0;
		m_err = SOCK_LAST_ERROR();
		if (m_err != SOCKERR_WOULDBLOCK) {
			n = SOCKERR_EOF;
		}
	}

	return n;
}

/** LOG **
 *
 * $Log: sock.cpp,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

