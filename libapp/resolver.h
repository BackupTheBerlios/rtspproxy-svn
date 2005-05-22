/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _RESOLVER_H
#define _RESOLVER_H

#include "types.h"
#include "ttree.h"
#include "str.h"
#include "sock.h"
#include "timer.h"

#include <string.h>

/*
 * Locations found in the WinNT registry:
 *  \\HKLM\System\CurrentControlSet\Services\Tcpip\Parameters\
 *          DhcpDomain
 *          DhcpNameServer
 *          Domain
 *          Nameserver
 *          Hostname
 *          SearchList
 *  (Win2000: also in Interfaces\*\ for each interface)
 */

class CResolverResponse {
      public:
	virtual void GetHostDone(int err, const CString & strQuery,
				 in_addr addrResult) = 0;
	virtual void GetHostDone(int err, in_addr addrQuery,
				 const CString & strResult) = 0;
};

class CResolver:public CTimerResponse, public CStreamResponse {
 private:			// Unimplemented
	CResolver(const CResolver &);
	 CResolver & operator=(const CResolver &);

 public:
	 CResolver(void);
	 virtual ~ CResolver(void);

	static CResolver *GetResolver(void);

	bool GetHost(CResolverResponse * pResponse,
		     const CString & strHost);
	bool GetHost(CResolverResponse * pResponse, struct in_addr addr);

 protected:
	virtual void OnTimer(void);
	virtual void OnConnectDone(int err);
	virtual void OnReadReady(void);
	virtual void OnWriteReady(void);
	virtual void OnExceptReady(void);
	virtual void OnClosed(void);

	struct dns_hdr {
		UINT16 id;
		UINT16 flags;	// QR:1, OPCODE:4, AA:1, TC:1, RD:1, RA:1, Z:3, RCODE:4
		UINT16 qdcnt;
		UINT16 ancnt;
		UINT16 nscnt;
		UINT16 arcnt;
	};
	struct dns_qr_hdr {
		CString strHost;
		UINT16 qtype;
		UINT16 qclass;
	};
	struct dns_rr_hdr {
		UINT16 rtype;
		UINT16 rclass;
		UINT32 ttl;
		UINT16 rdlen;
	};

	void SendQuery(const CSockAddr & addr, const CString & strHost,
		       UINT16 qtype);

	void AddHostEntry(time_t tExpire, const CString & strHost,
			  struct in_addr addr);
	void ReadConfig(void);

	bool EncodeName(CPCHAR szName, PBYTE & rpbuf, size_t & rlen);

	bool ParseQuestionHeader(dns_qr_hdr * phdr, const CBuffer & rbuf,
				 size_t & rpos);
	bool ParseAnswerHeader(dns_rr_hdr * phdr, const CBuffer & rbuf,
			       size_t & rpos);
	bool DecodeName(PCHAR pname, const CBuffer & rbuf, size_t & rpos);

 protected:
	typedef TSingleList < CResolverResponse * >CResolverResponseList;
	typedef TSingleList < CSockAddr > CNameserverList;
	typedef TSingleList < CString > CDomainList;

	class CHostInfo {
	public:
		CHostInfo(const CString & strName):m_tExpire(0),
		    m_strName(strName) {
			m_addr.s_addr = INADDR_NONE;
		} CHostInfo(time_t tExpire, const CString & strName,
			    struct in_addr addr):m_tExpire(tExpire),
		    m_strName(strName), m_addr(addr) {
		} CHostInfo & operator=(const CHostInfo & other) {
			m_tExpire = other.m_tExpire;
			m_strName = other.m_strName;
			m_addr = other.m_addr;
			return *this;
		}
		int operator==(const CHostInfo & other) const {
			return (strcasecmp(m_strName, other.m_strName) ==
				0);
		} int operator<(const CHostInfo & other) const {
			return (strcasecmp(m_strName, other.m_strName) <
				0);
		} int operator>(const CHostInfo & other) const {
			return (strcasecmp(m_strName, other.m_strName) >
				0);
		} time_t m_tExpire;
		CString m_strName;
		struct in_addr m_addr;
	};
	typedef AvlTree < CHostInfo > CHostInfoTree;

	class CAddrInfo {
	      public:

		CAddrInfo(struct in_addr addr):m_tExpire(0), m_addr(addr) {
		} CAddrInfo(time_t tExpire, struct in_addr addr,
			    const CString & strName):m_tExpire(tExpire),
		    m_addr(addr), m_strName(strName) {
		}

		CAddrInfo & operator=(const CAddrInfo & other) {
			m_tExpire = other.m_tExpire;
			m_addr = other.m_addr;
			m_strName = other.m_strName;
			return *this;
		}
		int operator==(const CAddrInfo & other) const {
			return (m_addr.s_addr == other.m_addr.s_addr);
		} int operator<(const CAddrInfo & other) const {
			return (m_addr.s_addr < other.m_addr.s_addr);
		} int operator>(const CAddrInfo & other) const {
			return (m_addr.s_addr > other.m_addr.s_addr);
		} time_t m_tExpire;
		struct in_addr m_addr;
		CString m_strName;
	};
	typedef AvlTree < CAddrInfo > CAddrInfoTree;

	class CHostQuery {
	      public:
		CHostQuery(const CString & strHost):m_tExpire(0),
		    m_tDelta(4), m_strHost(strHost), m_strFQDN(strHost) {
		} void AddResponse(CResolverResponse * pResponse) {
			m_listResponses.InsertTail(pResponse);
		}

		//XXX: this is soooooo lame
		CHostQuery(const CHostQuery & other) {
			m_tExpire = other.m_tExpire;
			m_tDelta = other.m_tDelta;
			m_strHost = other.m_strHost;
			m_strFQDN = other.m_strFQDN;
			m_itrDomain = other.m_itrDomain;
			m_itrServer = other.m_itrServer;
			CResolverResponseList::ConstIterator itr(other.
								 m_listResponses.
								 Begin());
			while (itr) {
				m_listResponses.InsertTail(*itr);
				itr++;
			}
		}
		CHostQuery & operator=(const CHostQuery & other) {
			while (!m_listResponses.IsEmpty())
				m_listResponses.RemoveHead();
			m_tExpire = other.m_tExpire;
			m_tDelta = other.m_tDelta;
			m_strHost = other.m_strHost;
			m_strFQDN = other.m_strFQDN;
			m_itrDomain = other.m_itrDomain;
			m_itrServer = other.m_itrServer;
			CResolverResponseList::ConstIterator itr(other.
								 m_listResponses.
								 Begin());
			while (itr) {
				m_listResponses.InsertTail(*itr);
				itr++;
			}
			return *this;
		}
		int operator==(const CHostQuery & other) const {
			return (strcasecmp(m_strHost, other.m_strHost) ==
				0);
		} int operator<(const CHostQuery & other) const {
			return (strcasecmp(m_strHost, other.m_strHost) <
				0);
		} int operator>(const CHostQuery & other) const {
			return (strcasecmp(m_strHost, other.m_strHost) >
				0);
		} time_t m_tExpire;
		time_t m_tDelta;
		CString m_strHost;
		CString m_strFQDN;
		CDomainList::Iterator m_itrDomain;
		CNameserverList::Iterator m_itrServer;
		CResolverResponseList m_listResponses;
	};
	typedef AvlTree < CHostQuery > CHostQueryTree;

	class CAddrQuery {
	      public:
		CAddrQuery(struct in_addr addr):m_tExpire(0), m_tDelta(4),
		    m_addr(addr) {
		} void AddResponse(CResolverResponse * pResponse) {
			m_listResponses.InsertTail(pResponse);
		}

		//XXX: this is soooooo lame
		CAddrQuery(const CAddrQuery & other) {
			m_tExpire = other.m_tExpire;
			m_tDelta = other.m_tDelta;
			m_addr = other.m_addr;
			m_itrServer = other.m_itrServer;
			CResolverResponseList::ConstIterator itr(other.
								 m_listResponses.
								 Begin());
			while (itr) {
				m_listResponses.InsertTail(*itr);
				itr++;
			}
			m_addr = other.m_addr;
		}
		CAddrQuery & operator=(const CAddrQuery & other) {
			while (!m_listResponses.IsEmpty())
				m_listResponses.RemoveHead();
			m_tExpire = other.m_tExpire;
			m_tDelta = other.m_tDelta;
			m_addr = other.m_addr;
			m_itrServer = other.m_itrServer;
			CResolverResponseList::ConstIterator itr(other.
								 m_listResponses.
								 Begin());
			while (itr) {
				m_listResponses.InsertTail(*itr);
				itr++;
			}
			return *this;
		}
		int operator==(const CAddrQuery & other) const {
			return (m_addr.s_addr == other.m_addr.s_addr);
		} int operator<(const CAddrQuery & other) const {
			return (m_addr.s_addr < other.m_addr.s_addr);
		} int operator>(const CAddrQuery & other) const {
			return (m_addr.s_addr > other.m_addr.s_addr);
		} time_t m_tExpire;
		time_t m_tDelta;
		struct in_addr m_addr;
		CNameserverList::Iterator m_itrServer;
		CResolverResponseList m_listResponses;
	};
	typedef AvlTree < CAddrQuery > CAddrQueryTree;

	void WalkHostTree(AvlNode < CHostQuery > *pNode);
	void WalkAddrTree(AvlNode < CAddrQuery > *pNode);

      protected:
	CUdpSocket m_sock;
	CTimer m_timer;
	UINT16 m_usQueryID;
	CNameserverList m_listServers;
	CDomainList m_listDomains;

	CHostInfoTree m_treeHostInfo;
	CAddrInfoTree m_treeAddrInfo;

	CHostQueryTree m_treeHostQueries;
	CAddrQueryTree m_treeAddrQueries;

	static CResolver *m_pResolver;
};

#endif				//ndef _RESOLVER_H

/** LOG **
 *
 * $Log: resolver.h,v $
 * Revision 1.2  2003/11/17 16:14:02  mat
 * make-up
 *
 *
 */

