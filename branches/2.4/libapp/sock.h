/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _SOCK_H
#define _SOCK_H

#include "types.h"
#include "str.h"
#include "stream.h"

#ifdef _UNIX
typedef int sockobj_t;
typedef int sockerr_t;
#define INVALID_SOCKET -1
#define SF_NONE     0
#define SF_ACCEPT   (XPOLLACC|POLLIN)
#define SF_CONNECT  (XPOLLCNX|POLLOUT)
#define SF_READ     POLLIN
#define SF_WRITE    POLLOUT
#define SF_EXCEPT   POLLPRI
#define SF_ALL      (XPOLLACC|XPOLLCNX|POLLIN|POLLOUT|POLLPRI)
#define SOCKERR_NONE        0
#define SOCKERR_WOULDBLOCK  EAGAIN
#define SOCKERR_INPROGRESS  EINPROGRESS
#define SOCKERR_CONNRESET   EPIPE
#define SOCKERR_EOF         0x7FFFFFFF

#if (defined(_BSD) && (_BSD < 40)) || (defined(_SOLARIS) && (_SOLARIS < 57))
typedef int socklen_t;
#endif
#endif
#ifdef _WIN32
int inet_aton(const char *cp, struct in_addr *inp);
typedef SOCKET sockobj_t;
typedef int sockerr_t;
    // INVALID_SOCKET defined in winsock.h
typedef int socklen_t;
typedef int ssize_t;
#define SF_NONE     0
#define SF_ACCEPT   FD_ACCEPT
#define SF_CONNECT  FD_CONNECT
#define SF_READ     FD_READ
#define SF_WRITE    FD_WRITE
#define SF_EXCEPT   FD_OOB
#define SF_ALL      (FD_ACCEPT|FD_CONNECT|FD_READ|FD_WRITE|FD_OOB)
#define SOCKERR_NONE        0
#define SOCKERR_WOULDBLOCK  WSAEWOULDBLOCK
#define SOCKERR_INPROGRESS  WSAEINPROGRESS
#define SOCKERR_CONNRESET   WSAECONNRESET
#define SOCKERR_EOF         0x7FFFFFFF
#endif

#define INVALID_PORT 0xffff
#define MAX_UDP_LEN 8192

//TODO: add IPv6 support, look into multicast

class CInetAddr {
      public:
	CInetAddr(void);
	 CInetAddr(const in_addr & host);
	 CInetAddr(CPCHAR szHost);

	bool IsValid(void) const;
	in_addr GetHost(void) const;
	void SetHost(const in_addr & host);
	void SetHost(CPCHAR szHost);

	static CInetAddr Any(void);
	static CInetAddr None(void);

	static bool Resolve(CPCHAR szHost, in_addr * paddr);

	inline operator  in_addr(void) const {
		return m_addr.sin_addr;
      } protected:
	 sockaddr_in m_addr;
};

class CSockAddr:public CInetAddr {
      public:
	CSockAddr(void);
	CSockAddr(const sockaddr_in & addr);
	 CSockAddr(const in_addr & host, UINT16 port = 0);
	 CSockAddr(CPCHAR szHost, UINT16 port = 0);

	sockaddr_in GetAddr(void) const;
	void SetAddr(const sockaddr_in & addr);
	void SetAddr(const in_addr & host, UINT16 port);
	void SetAddr(CPCHAR szHost, UINT16 port);

	UINT16 GetPort(void) const;
	void SetPort(UINT16 port);

	static CSockAddr Any(void);
	static CSockAddr None(void);

	inline operator  sockaddr_in(void) const {
		return m_addr;
}};

class CTcpSocket;
class CListenSocketResponse {
      public:
	virtual void OnConnection(CTcpSocket * psock) = 0;
	virtual void OnClosed(void) = 0;
};

class CSocket:public CStream {
	friend class CEventThread;

      private:			// Unimplemented
	 CSocket(const CSocket &);
	 CSocket & operator=(const CSocket &);

      public:
	 CSocket(void);
	 CSocket(CStreamResponse * pResponse);
	 virtual ~ CSocket(void);

	virtual bool IsOpen(void);
	virtual void Close(void);
	virtual size_t Read(PVOID pbuf, size_t nLen);
	virtual size_t Write(CPVOID pbuf, size_t nLen);

	inline bool Read(CBuffer * pbuf) {
		return CStream::Read(pbuf);
	} CSockAddr GetLocalAddr(void);
	CSockAddr GetPeerAddr(void);

	bool Select(UINT32 nWhich);
	sockerr_t LastError(void);

      protected:
	sockobj_t GetHandle(void) {
		return m_sock;
	}

      protected:
	sockobj_t m_sock;
	UINT32 m_uSelectFlags;
	sockerr_t m_err;

	// FIXME
	bool log_tcp;
};

class CListenSocket:public CSocket {
	friend class CEventThread;

      private:			// Unimplemented
	 CListenSocket(const CListenSocket &);
	 CListenSocket & operator=(const CListenSocket &);

      public:
	 CListenSocket(void);
	 CListenSocket(CListenSocketResponse * pResponse);
	 virtual ~ CListenSocket(void);

	void SetResponse(CListenSocketResponse * pResponse);
	bool Listen(const CSockAddr & addr);

      protected:
	 CListenSocketResponse * m_pAcceptResponse;
};

class CTcpSocket:public CSocket {
      private:			// Unimplemented
	CTcpSocket(const CTcpSocket &);
	 CTcpSocket & operator=(const CTcpSocket &);

      public:
	 CTcpSocket(void);
	 CTcpSocket(CStreamResponse * pResponse);
	 virtual ~ CTcpSocket(void);

	bool Connect(const CSockAddr & addr);
};

class CUdpSocket:public CSocket {
      private:			// Unimplemented
	CUdpSocket(const CUdpSocket &);
	 CUdpSocket & operator=(const CUdpSocket &);

      public:
	 CUdpSocket(void);
	 CUdpSocket(CStreamResponse * pResponse);
	 virtual ~ CUdpSocket(void);

	bool Bind(const CSockAddr & addr);
	bool Connect(const CSockAddr & addr);

	size_t RecvFrom(CSockAddr * paddr, PVOID pbuf, size_t nLen);
	size_t SendTo(const CSockAddr & addr, CPVOID pbuf, size_t nLen);

      protected:
};

#endif				//ndef _SOCK_H

/** LOG **
 *
 * $Log: sock.h,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

