/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#include "resolver.h"
#include "parser.h"
#include "app.h"

#include "dbg.h"

#include <errno.h>

#ifdef _UNIX
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#endif

// RCODE values
#define RC_OK       0		/* Success */
#define RC_FMT      1		/* Format error - server cannot grok */
#define RC_FAIL     2		/* Server failed */
#define RC_EXIST    3		/* No such host/domain */
#define RC_NOTIMPL  4		/* Not implemented */
#define RC_ACCESS   5		/* Access denied */

// CLASS values
#define CL_IN       1		/* Internet */
#define CL_CS       2
#define CL_CH       3
#define CL_HS       4

// RRTYPE and QTYPE values (QTYPE is a superset of RRTYPE)
#define RR_A         1		/* Address */
#define RR_NS        2
#define RR_MD        3
#define RR_MF        4
#define RR_CNAME     5		/* Canonical name (alias) */
#define RR_SOA       6
#define RR_MB        7
#define RR_MG        8
#define RR_MR        9
#define RR_NULL     10
#define RR_WKS      11
#define RR_PTR      12		/* Pointer */
#define RR_HINFO    13
#define RR_MINFO    14
#define RR_MX       15
#define RR_TXT      16

/*
 * Header flags:
 *   QR = Query/Response? 0=query, 1=response
 *   OPCODE: Opcode:
 *           0 = Forward query
 *           1 = Inverse query
 *           2 = Server Status Request
 *           3..15 reserved
 *   AA:     Authoritative Answer? 0=no, 1=yes
 *   TC:     Truncated? 0=no, 1=yes
 *   RD:     Recursion Desired? 0=no, 1=yes
 *   RA:     Recursion Available? 0=no, 1=yes
 *   Z:      Must be zero (reserved)
 *   RCODE:  Response code:
 *           0 = No Error
 *           1 = Format Error
 *           2 = Server Failure
 *           3 = Name Error (nonexistent host/domain)
 *           4 = Not Implemented (query type not supported)
 *           5 = Refused (access denied)
 *           6..15 reserved
 *
 * Question format:
 *   QNAME:  series of (bytelen,label) terminated by nul octet
 *   QTYPE:  two-octet query type
 *   QCLASS: two-octet domain class (eg. IN)
 *
 * RR (answer, authority, additional) format:
 *   NAME:  series of (bytelen,label) terminated by nul octet
 *   TYPE:  two-octet RR type
 *   CLASS: two-octet domain class (eg. IN)
 *   TTL:   four-octet time-to-live in seconds (0=nocache)
 *   RDLEN: two-octet length of RDATA field
 *   RDATA: description of resource (eg. four-octet addr)
 */

/*
 * Sample query: www.aa.net
 * e6 4f   query_id
 * 01 00   flags: QR=Q, OP=0, AA=0, TC=0, RD=1, RA=0, RCODE=0
 * 00 01   qdcnt: 1
 * 00 00   ancnt: 0
 * 00 00   nscnt: 0
 * 00 00   arcnt: 0
 * qd@0C: 03 77 77 77 02 61 61 03 6e 65 74 00 00 01 00 01
 *   "www.aa.net" A IN
 *
 * Sample response: www.aa.net
 * e6 4f   query_id
 * 85 80   flags: QR=R, OP=0, AA=1, TC=0, RD=1, RA=1, RCODE=0
 * 00 01   qdcnt: 1
 * 00 01   ancnt: 1
 * 00 02   nscnt: 2
 * 00 02   arcnt: 2
 * qd@0C: 03 77 77 77 02 61 61 03 6e 65 74 00 00 01 00 01
 *   "www.aa.net" A IN
 * an@1C: c0 0c 00 01 00 01   00 01 51 80
 *        00 04 cc 9d dc 02
 *   "www.aa.net" A IN, TTL=1day, 204.157.220.2
 * ns@2C: 02 61 61 03 6e 65 74 00 00 02 00 01   00 01 51 80
 *        00 0a 07 68 65 6e 64 72 69 78 c0 2c
 *   "aa.net" NS IN, TTL=1day, "hendrix.aa.net"
 *
 * ns@48: c0 2c 00 02 00 01   00 01 51 80
 *        00 06 03 6e 73 32 c0 2c
 *   "aa.net" NS IN, TTL=1day, "ns2.aa.net"
 * ar@5A: c0 3e 00 01 00 01   00 01 51 80
 *        00 04 cc 9d dc 04
 *   "hendrix.aa.net" A IN, TTL=1day, 204.157.220.4
 * ar@6A: c0 54 00 01  00 01 00 01 51 80 00 04 cf 11 70 04
 *   "ns2.aa.net" A IN, TTL=1day, 207.17.112.4
 */

// inet_aton for in-addr.arpa hack: "4.3.2.1.in-addr.arpa" -> 1.2.3.4
static int inet_aton_rev(const char *cp, struct in_addr *inp)
{
	int ret = 0;
	char host[16];		// aaa.bbb.ccc.ddd
	const char *end = cp;
	while (*end && (isdigit(*end) || '.' == *end))
		end++;
	if (end > cp && end - cp <= 16 && !strcasecmp(end, "in-addr.arpa")) {
		struct in_addr addr;
		end--;
		memcpy(host, cp, end - cp);
		host[end - cp] = '\0';
		ret = inet_aton(host, &addr);
		if (ret) {
			unsigned n;
			BYTE *src =
			    (BYTE *) & addr.s_addr +
			    sizeof(struct in_addr);
			BYTE *dst = (BYTE *) & inp->s_addr;
			for (n = 0; n < sizeof(struct in_addr); n++) {
				src--;
				*dst = *src;
				dst++;
			}
		}
	}
	return ret;
}

// inet_ntoa for in-addr.arpa hack: 1.2.3.4 -> "4.3.2.1.in-addr.arpa"
static char *inet_ntoa_rev(struct in_addr in)
{
	static char host[29];	// aaa.bbb.ccc.ddd.in-addr.arpa\0
	BYTE qa[4];
	memcpy(qa, &in.s_addr, 4);
	sprintf(host, "%u.%u.%u.%u.in-addr.arpa", qa[3], qa[2], qa[1],
		qa[0]);
	return host;
}

/**************************************
 *
 * CResolver
 *
 **************************************/
CResolver *CResolver::m_pResolver = NULL;

CResolver::CResolver(void):
m_sock(this), 
m_timer(this), 
m_usQueryID(0)
{
	ReadConfig();
}

CResolver::~CResolver(void)
{
	// Empty
}

CResolver *CResolver::GetResolver(void)
{
	if (m_pResolver == NULL)
		m_pResolver = new CResolver;

	return m_pResolver;
}

bool CResolver::GetHost(CResolverResponse * pResponse,
			const CString & strHost)
{
	struct hostent *host;
	struct in_addr addr;
	host = gethostbyname( strHost );

	if ( host == NULL ) {
		addr.s_addr = INADDR_NONE;
		pResponse->GetHostDone(ENOENT, strHost, addr);
		return false;
	}

	addr = *( (struct in_addr *) (host->h_addr) );
	pResponse->GetHostDone(0, strHost, addr);
	return true;

/*
	dbg("CResolver::GetHost: query for '%s'\n", (CPCHAR) strHost);
	CHostInfo *pInfo;

	// Determine if this is a number or name
	bool bIsNumeric = true;
	CPCHAR p = strHost;
	while (*p) {
		if (!isdigit(*p) && *p != '.') {
			bIsNumeric = false;
			break;
		}
		p++;
	}
	if (bIsNumeric) {
		int err = 0;
		in_addr addr;
		if (!inet_aton(strHost, &addr)) {
			err = ENOENT;
		}
		pResponse->GetHostDone(err, strHost, addr);
		return true;
	}
	// First search for name as given
	CHostInfo info(strHost);
	pInfo = m_treeHostInfo.Search(info);
	if (pInfo) {
		if (pInfo->m_tExpire > time(NULL)) {
			pResponse->GetHostDone(0, strHost, pInfo->m_addr);
			return true;
		}
		m_treeHostInfo.Delete(info);
	}
	// Not found - if it's unqualified, search the domain list
	if (!strchr(strHost, '.')) {
		CDomainList::Iterator itr(m_listDomains.Begin());
		if (!itr) {
			// Domain list is empty - game over, man
			in_addr addr;
			addr.s_addr = INADDR_NONE;
			pResponse->GetHostDone(ENOENT, strHost, addr);
			return true;
		}
		while (itr) {
			CString strFQDN = strHost;
			strFQDN.Append(".");
			strFQDN.Append(*itr);
			CHostInfo info(strFQDN);
			CHostInfo *pInfo = m_treeHostInfo.Search(info);
			if (pInfo) {
				if (pInfo->m_tExpire > time(NULL)) {
					pResponse->GetHostDone(0, strHost,
							       pInfo->
							       m_addr);
					return true;
				}
				m_treeHostInfo.Delete(info);
			}
			itr++;
		}
	}
	// Looks like we have to send a query
	CHostQuery query(strHost);
	CHostQuery *pQuery = m_treeHostQueries.Insert(query);
	if (!pQuery) {
		pQuery = m_treeHostQueries.Search(query);
		assert(pQuery);

		pQuery->m_tExpire = time(NULL) + 4;
		pQuery->m_tDelta = 4;
		if (!strchr(strHost, '.')) {
			// Unqualified - search the domain list
			CDomainList::Iterator itr(m_listDomains.Begin());
			assert(itr);	// should have caught this already
			pQuery->m_strFQDN = strHost;
			pQuery->m_strFQDN.Append(".");
			pQuery->m_strFQDN.Append(*itr);
			pQuery->m_itrDomain = itr;
		}
		pQuery->m_itrServer = m_listServers.Begin();

		SendQuery(*pQuery->m_itrServer, pQuery->m_strFQDN, RR_A);
	}

	pQuery->AddResponse(pResponse);

	return true;
*/
}

bool CResolver::GetHost(CResolverResponse * pResponse, struct in_addr addr)
{
	dbg("CResolver::GetHost: query for %s", inet_ntoa(addr));

	CAddrInfo info(addr);

	// See if we already have it
	CAddrInfo *pInfo = m_treeAddrInfo.Search(info);
	if (pInfo) {
		if (pInfo->m_tExpire > time(NULL)) {
			pResponse->GetHostDone(0, addr, pInfo->m_strName);
			return true;
		}
		m_treeAddrInfo.Delete(info);
	}
	// Nope, gotta send a query
	CAddrQuery query(addr);
	CAddrQuery *pQuery = m_treeAddrQueries.Insert(query);
	if (!pQuery) {
		pQuery = m_treeAddrQueries.Search(query);
		assert(pQuery);

		pQuery->m_tExpire = time(NULL) + 4;
		pQuery->m_tDelta = 4;
		pQuery->m_itrServer = m_listServers.Begin();
		SendQuery(*pQuery->m_itrServer, inet_ntoa_rev(addr),
			  RR_PTR);
	}

	pQuery->AddResponse(pResponse);

	return true;
}

void CResolver::WalkHostTree(AvlNode < CHostQuery > *pNode)
{
	if (!pNode)
		return;
	WalkHostTree(pNode->Subtree(LEFT));
	WalkHostTree(pNode->Subtree(RIGHT));
	CHostQuery & rquery = pNode->Key();
	if (rquery.m_tExpire < time(NULL)) {
		// Timed out, next nameserver
		rquery.m_itrServer++;
		if (rquery.m_itrServer) {
			rquery.m_tExpire = time(NULL) + 4;
			rquery.m_tDelta = 4;
			SendQuery(*rquery.m_itrServer, rquery.m_strHost,
				  RR_A);
			return;
		}
		// Exhausted server list, so bump the timeout and start over
		rquery.m_tExpire += rquery.m_tDelta;
		rquery.m_tDelta *= 2;
		rquery.m_itrServer = m_listServers.Begin();
		if (rquery.m_tDelta > 30) {
			// Everything timed out - we're all alone, and it's getting dark!
			struct in_addr addr;
			addr.s_addr = INADDR_NONE;
			CResolverResponseList::Iterator itr(rquery.
							    m_listResponses.
							    Begin());
			while (itr) {
				(*itr)->GetHostDone(EAGAIN,
						    rquery.m_strHost,
						    addr);
				itr++;
			}
			m_treeHostQueries.Delete(rquery);
		}
	}
}

void CResolver::WalkAddrTree(AvlNode < CAddrQuery > *pNode)
{
	if (!pNode)
		return;
	WalkAddrTree(pNode->Subtree(LEFT));
	WalkAddrTree(pNode->Subtree(RIGHT));
	CAddrQuery & rquery = pNode->Key();
	if (rquery.m_tExpire < time(NULL)) {
		// Timed out, next nameserver
		rquery.m_itrServer++;
		if (rquery.m_itrServer) {
			rquery.m_tExpire = time(NULL) + 4;
			rquery.m_tDelta = 4;
			SendQuery(*rquery.m_itrServer,
				  inet_ntoa_rev(rquery.m_addr), RR_PTR);
			return;
		}
		// Exhausted server list, so bump the timeout and start over
		rquery.m_tExpire += rquery.m_tDelta;
		rquery.m_tDelta *= 2;
		rquery.m_itrServer = m_listServers.Begin();
		if (rquery.m_tDelta > 30) {
			// Everything timed out - we're all alone, and it's getting dark!
			CString host;
			CResolverResponseList::Iterator itr(rquery.
							    m_listResponses.
							    Begin());
			while (itr) {
				(*itr)->GetHostDone(EAGAIN, rquery.m_addr,
						    host);
				itr++;
			}
			m_treeAddrQueries.Delete(rquery);
		}
	}
}

void CResolver::OnTimer(void)
{
	dbgout("CResolver::OnTimer");

	// iterate through queries, remove stale ones and respond fail
	WalkHostTree(m_treeHostQueries.GetRoot());
	WalkAddrTree(m_treeAddrQueries.GetRoot());

	if (m_treeHostQueries.IsEmpty() && m_treeAddrQueries.IsEmpty()) {
		dbgout("CResolver::OnTimer: no more queries");
		m_sock.Close();
		m_timer.Disable();
	}
}

void CResolver::OnConnectDone(int err)
{
	assert(false);
}

void CResolver::OnReadReady(void)
{
	dbgout("CResolver::OnReadReady");
	CBuffer buf;
	buf.SetSize(513);
	if (!m_sock.Read(&buf)) {
		//XXX: fail all pending queries
		dbgout("CResolver::OnReadReady: socket read failed");
		return;
	}
	if (buf.GetSize() > 512) {
		dbgout
		    ("CResolver::OnReadReady: packet too large, ignoring");
		return;
	}

	CPBYTE p = buf.GetBuffer();
	size_t len = buf.GetSize();
	size_t pos = 0;
	dns_hdr hdr;
	if (len < sizeof(dns_hdr)) {
		dbgout("CResolver::OnReadReady: short packet");
		return;
	}

	ntohs_buf(&hdr.id, p + pos, sizeof(dns_hdr) / sizeof(UINT16));
	pos += sizeof(dns_hdr);

	// QR=1, OPCODE=0, AA=x, TC=0, RD=1, RA=x, Z=0, RCODE=0
	// 10000x01x000xxxx
	if ((hdr.flags & 0xFB70) != 0x8100) {
		dbgout("CResolver::OnReadReady: bad response flags %04X",
		       hdr.flags);
		return;
	}
	if (hdr.qdcnt != 1) {
		dbgout
		    ("CResolver::OnReadReady: bad counts %hu, %hu, %hu, %hu",
		     hdr.qdcnt, hdr.ancnt, hdr.nscnt, hdr.arcnt);
		return;
	}

	time_t tnow;
	dns_qr_hdr qrhdr;
	dns_rr_hdr rrhdr;

	tnow = time(NULL);
	while (hdr.qdcnt) {
		if (!ParseQuestionHeader(&qrhdr, buf, pos)) {
			dbgout("CResolver: bad question");
			return;
		}
		hdr.qdcnt--;
	}

	//XXX: match question to pending query (hostname/serveraddr)

	UINT rc = (hdr.flags & 0xF);
	if (rc != RC_OK) {
		if (qrhdr.qtype == RR_A) {
			// Does not exist - try next domain
			CHostQuery query(qrhdr.strHost);
			CHostQuery *pQuery =
			    m_treeHostQueries.Search(query);
			if (!pQuery) {
				dbgout
				    ("CResolver::OnReadReady: received rc=%u for unexpected host '%s'",
				     rc, (CPCHAR) qrhdr.strHost);
				return;
			}
			// If query was unqualified, bump the domain iterator
			if (pQuery->m_itrDomain)
				pQuery->m_itrDomain++;

			if (!pQuery->m_itrDomain) {
				// Exhausted search list
				struct in_addr addr;
				addr.s_addr = INADDR_NONE;
				CResolverResponseList::
				    Iterator itr(pQuery->m_listResponses.
						 Begin());
				while (itr) {
					(*itr)->GetHostDone(ENOENT,
							    qrhdr.strHost,
							    addr);
					itr++;
				}
				m_treeHostQueries.Delete(query);
			} else {
				pQuery->m_strFQDN = pQuery->m_strHost;
				pQuery->m_strFQDN.Append(".");
				pQuery->m_strFQDN.Append(*pQuery->
							 m_itrDomain);
				pQuery->m_itrDomain++;
				//XXX: reset query's server iterator here?
				SendQuery(*pQuery->m_itrServer,
					  pQuery->m_strFQDN, RR_A);
			}
		} else if (qrhdr.qtype == RR_PTR) {
			struct in_addr addr;
			if (inet_aton_rev(qrhdr.strHost, &addr)) {
				CAddrQuery query(addr);
				CAddrQuery *pQuery =
				    m_treeAddrQueries.Search(query);
				if (!pQuery) {
					dbgout
					    ("CResolver::OnReadReady: received rc=%u for unexpected host '%s'",
					     rc, (CPCHAR) qrhdr.strHost);
					return;
				}

				CString host;
				CResolverResponseList::
				    Iterator itr(pQuery->m_listResponses.
						 Begin());
				while (itr) {
					(*itr)->GetHostDone(ENOENT, addr,
							    host);
					itr++;
				}
				m_treeAddrQueries.Delete(query);
			}
		} else {
			dbgout
			    ("CResolver::OnReadReady: error %u for unexpected qtype %hu",
			     rc, qrhdr.qtype);
			return;
		}

		// Don't bother parsing the answers
		hdr.ancnt = 0;
	}

	while (hdr.ancnt) {
		struct in_addr addr;
		char szHostAnswer[256];

		if (!ParseAnswerHeader(&rrhdr, buf, pos)) {
			dbgout("CResolver: bad answer");
			return;
		}
		switch (rrhdr.rtype) {
		case RR_A:
			if (!rrhdr.rdlen || rrhdr.rdlen % 4)
				return;
			//XXX: keep multiple addrs?
			memcpy(&addr, buf.GetBuffer() + pos, 4);
			dbgout("Got A: %s = %s", (CPCHAR) qrhdr.strHost,
			       inet_ntoa(addr));
			AddHostEntry(tnow + rrhdr.ttl, qrhdr.strHost,
				     addr);
			break;
		case RR_CNAME:
			if (!DecodeName(szHostAnswer, buf, pos))
				return;
			dbgout("Got CNAME: %s = %s",
			       (CPCHAR) qrhdr.strHost, szHostAnswer);
			break;
		case RR_PTR:
			if (!DecodeName(szHostAnswer, buf, pos))
				return;
			if (inet_aton_rev((CPCHAR) qrhdr.strHost, &addr)) {
				dbgout("Got PTR: %s (%s) = %s",
				       (CPCHAR) qrhdr.strHost,
				       inet_ntoa(addr), szHostAnswer);
				AddHostEntry(tnow + rrhdr.ttl,
					     szHostAnswer, addr);
			}
			break;
		default:	// Looks valid but useless
			break;
		}
		hdr.ancnt--;
	}

	// Ignore authority and additional RR's

	if (m_treeHostQueries.IsEmpty() && m_treeAddrQueries.IsEmpty()) {
		dbgout("CResolver::OnReadReady: no more queries");
		m_sock.Close();
		m_timer.Disable();
	}
}

void CResolver::OnWriteReady(void)
{
	assert(false);
}

void CResolver::OnExceptReady(void)
{
	assert(false);
}

void CResolver::OnClosed(void)
{
	dbgout("CResolver::OnClosed");
	// iterate through host queries, respond fail and delete
	// iterate through addr queries, respond fail and delete
}

static BYTE s_byQueryTmpl[] = {
	0x00, 0x00,		// Query ID (fill this in)
	0x01, 0x00,		// QR=0, OPCODE=0, AA=0, TC=0, RD=1, RA=0, Z=0, RCODE=0
	0x00, 0x01,		// QD count
	0x00, 0x00,		// AN count
	0x00, 0x00,		// NS count
	0x00, 0x00		// AR count
};

void CResolver::SendQuery(const CSockAddr & addr, const CString & strHost,
			  UINT16 qtype)
{
	dbgout("CResolver::SendQuery: host %s", (CPCHAR) strHost);
	// Fire up our socket and timer
	if (!m_sock.IsOpen()) {
		if (!m_sock.Bind(CSockAddr::Any())) {
			dbgout("CResolver::SendQuery: cannot bind socket");
			return;
		}
		m_sock.Select(SF_READ);
	}
	if (CTimer::Repeating != m_timer.GetMode()) {
		dbgout("CResolver::SendQuery: setting timer");
		m_timer.SetRepeating(2 * 1000);
	}
	// Create the query buffer
	CBuffer buf;
	buf.SetSize(512);
	PBYTE p = buf.GetBuffer();
	size_t len = buf.GetSize();

	// Encode the header
	m_usQueryID++;
	memcpy(p, s_byQueryTmpl, sizeof(s_byQueryTmpl));
	*(p + 0) = m_usQueryID >> 8;
	*(p + 1) = m_usQueryID & 0xFF;
	p += sizeof(s_byQueryTmpl);
	len -= sizeof(s_byQueryTmpl);

	// Encode the hostname
	if (!EncodeName(strHost, p, len))
		return;
	if (len < 4)
		return;

	// qtype, qclass
	*(p + 0) = qtype >> 8;
	*(p + 1) = qtype & 0xFF;
	*(p + 2) = 0;
	*(p + 3) = 1;
	p += 4;
	len -= 4;

	buf.SetSize(512 - len);
	m_sock.SendTo(addr, buf.GetBuffer(), buf.GetSize());
}

void CResolver::AddHostEntry(time_t tExpire, const CString & strHost,
			     struct in_addr addr)
{
	dbgout("AddHostEntry: host %s = %s", (CPCHAR) strHost,
	       inet_ntoa(addr));
	// Add host to our host tree
	m_treeHostInfo.Insert(CHostInfo(tExpire, strHost, addr));

	// Add addr to our addr tree
	m_treeAddrInfo.Insert(CAddrInfo(tExpire, addr, strHost));

	// See if anyone is waiting for this host
	CHostQuery queryHost(strHost);
	CHostQuery *pHostQuery = m_treeHostQueries.Search(queryHost);
	if (pHostQuery) {
		dbgout("\tfound host query in tree, calling responses");
		CResolverResponseList::Iterator itr(pHostQuery->
						    m_listResponses.
						    Begin());
		while (itr) {
			(*itr)->GetHostDone(0, strHost, addr);
			itr++;
		}
		m_treeHostQueries.Delete(queryHost);
	}
	CPCHAR pdot;
	if ((pdot = strchr(strHost, '.'))) {
		// Perhaps someone is waiting on the unqualified name
		queryHost.m_strHost.Set(strHost, pdot - (CPCHAR) strHost);
		pHostQuery = m_treeHostQueries.Search(queryHost);
		if (pHostQuery) {
			dbgout
			    ("\tfound bare host query in tree, calling responses");
			CResolverResponseList::Iterator itr(pHostQuery->
							    m_listResponses.
							    Begin());
			while (itr) {
				(*itr)->GetHostDone(0, strHost, addr);
				itr++;
			}
			m_treeHostQueries.Delete(queryHost);
		}
	}
	// See if anyone is waiting for this addr
	CAddrQuery queryAddr(addr);
	CAddrQuery *pAddrQuery = m_treeAddrQueries.Search(queryAddr);
	if (pAddrQuery) {
		dbgout("\tfound addr query in tree, calling responses");
		CResolverResponseList::Iterator itr(pAddrQuery->
						    m_listResponses.
						    Begin());
		while (itr) {
			(*itr)->GetHostDone(0, addr, strHost);
			itr++;
		}
		m_treeAddrQueries.Delete(queryAddr);
	}
}

void CResolver::ReadConfig(void)
{
	CConfigParser parser;
	CToken tok;
#ifdef _UNIX
	// Read resolv.conf for domain name(s), nameservers
	parser.Open("/etc/resolv.conf");
	tok = parser.NextToken();
	while (CToken::TOK_EOF != tok.type) {
		if ('#' == tok.val[0]) {
			parser.NextLine();
		} else if (0 == strcmp(tok.val, "nameserver")) {
			tok = parser.NextToken();
			if (CToken::TOK_STRING == tok.type) {
				dbgout("nameserver '%s'",
				       (CPCHAR) tok.val);
				m_listServers.
				    InsertTail(CSockAddr(tok.val, 53));
			}
			parser.NextLine();
		} else if (0 == strcmp(tok.val, "domain")) {
			tok = parser.NextToken();
			if (CToken::TOK_STRING == tok.type) {
				dbgout("domain '%s'", (CPCHAR) tok.val);
			}
			parser.NextLine();
		} else if (0 == strcmp(tok.val, "search")) {
			tok = parser.NextToken();
			while (CToken::TOK_STRING == tok.type) {
				dbgout("search '%s'", (CPCHAR) tok.val);
				m_listDomains.InsertTail(tok.val);
				tok = parser.NextToken();
			}
		} else {
			dbgout("unknown token '%s'", (CPCHAR) tok.val);
			while (CToken::TOK_STRING == tok.type) {
				tok = parser.NextToken();
			}
		}
		tok = parser.NextToken();
	}
	parser.Close();
#endif

#ifdef _WIN32
	char strHolder[255];
	DWORD size = 255;
	HKEY hKey;
	long lRet;

	lRet = RegOpenKey(HKEY_LOCAL_MACHINE,
			  "System\\CurrentControlSet\\Services\\Tcpip\\Parameters",
			  &hKey);
	if (lRet == ERROR_SUCCESS) {
		lRet = RegQueryValueEx(hKey,
				       "NameServer",
				       NULL,
				       NULL, (LPBYTE) strHolder, &size);

		//if can find it, try DhcpNameServer
		if (lRet != ERROR_SUCCESS || strHolder[0] == '\0') {
			size = 255;
			lRet = RegQueryValueEx(hKey,
					       "DhcpNameServer",
					       NULL,
					       NULL,
					       (LPBYTE) strHolder, &size);
		}

		if (lRet == ERROR_SUCCESS && strHolder[0] != '\0') {
			char *pCur = strHolder;
			char *pStart = pCur;
			while (*pCur) {
				if (*pCur == ' ') {
					*pCur = '\0';
					m_listServers.
					    InsertTail(CSockAddr
						       (pStart, 53));
					pStart = pCur + 1;
				}
				pCur++;
			}
			if (*pStart)
				m_listServers.
				    InsertTail(CSockAddr(pStart, 53));
		}
		// for searchlist
		size = 255;
		lRet = RegQueryValueEx(hKey,
				       "SearchList",
				       NULL,
				       NULL, (LPBYTE) strHolder, &size);

		//if can find it, try Domain
		if (lRet != ERROR_SUCCESS || strHolder[0] == '\0') {
			size = 255;
			lRet = RegQueryValueEx(hKey,
					       "DhcpDomain",
					       NULL,
					       NULL,
					       (LPBYTE) strHolder, &size);

			//if can find it, try Domain
			if (lRet != ERROR_SUCCESS || strHolder[0] == '\0') {
				size = 255;
				lRet = RegQueryValueEx(hKey,
						       "DhcpDomain",
						       NULL,
						       NULL,
						       (LPBYTE) strHolder,
						       &size);
			}
		}

		if (lRet == ERROR_SUCCESS && strHolder[0] != '\0') {
			char *pCur = strHolder;
			char *pStart = pCur;
			while (*pCur) {
				if (*pCur == ' ') {
					*pCur = '\0';
					m_listDomains.InsertTail(pStart);
					pStart = pCur + 1;
				}
				pCur++;
			}
			if (*pStart)
				m_listDomains.InsertTail(pStart);
		}

		RegCloseKey(hKey);
	}
#endif

#ifdef _UNIX
	parser.Open("/etc/hosts");
#endif

#ifdef _WIN32
	char strPath[255];
	ExpandEnvironmentStrings("%windir%\\system32\\drivers\\etc\\hosts",
				 strPath, 255);
	parser.Open(strPath);
#endif

	tok = parser.NextToken();
	while (CToken::TOK_EOF != tok.type) {
		struct in_addr addr;

		if ('#' == tok.val[0]) {
			parser.NextLine();
		} else if (0 == inet_aton(tok.val, &addr)) {
			// It's probably an IP6 address
			parser.NextLine();
		} else {
			tok = parser.NextToken();
			while (CToken::TOK_EOL != tok.type) {
				AddHostEntry(MAX_TIME_T, tok.val, addr);
				tok = parser.NextToken();
			}
		}
		tok = parser.NextToken();
	}
	parser.Close();

	// If no nameservers are specified, use localhost
	if (m_listServers.IsEmpty()) {
		m_listServers.InsertTail(CSockAddr("127.0.0.1", 53));
	}
}

bool CResolver::EncodeName(CPCHAR szName, PBYTE & rpbuf, size_t & rlen)
{
	while (*szName && rlen > 0) {
		CPCHAR pLabel = szName;
		BYTE nLabelLen = 0;
		BYTE nOverLen = min(64, rlen - 1);
		while (*szName && '.' != *szName && nLabelLen < nOverLen) {
			nLabelLen++;
			szName++;
		}
		if (nLabelLen == nOverLen)
			return false;
		*rpbuf++ = nLabelLen;
		rlen--;
		memcpy(rpbuf, pLabel, nLabelLen);
		rpbuf += nLabelLen;
		rlen -= nLabelLen;
		if (*szName == '.')
			szName++;
	}
	if (rlen < 1)
		return false;
	*rpbuf++ = 0;
	rlen--;

	return true;
}

bool CResolver::ParseQuestionHeader(dns_qr_hdr * phdr,
				    const CBuffer & rbuf, size_t & rpos)
{
	char szHost[256];

	if (!DecodeName(szHost, rbuf, rpos))
		return false;
	if (rpos + 4 > rbuf.GetSize())
		return false;

	phdr->strHost = szHost;
	ntohs_buf(&phdr->qtype, rbuf.GetBuffer() + rpos, 2);
	rpos += 2 * 2;
	if (phdr->qclass != CL_IN)
		return false;

	return true;
}

bool CResolver::ParseAnswerHeader(dns_rr_hdr * phdr, const CBuffer & rbuf,
				  size_t & rpos)
{
	char szHost[256];
	UINT16 usTmp;
	UINT32 ulTmp;

	if (!DecodeName(szHost, rbuf, rpos))
		return false;
	if (rpos + 10 > rbuf.GetSize())
		return false;

	memcpy(&usTmp, rbuf.GetBuffer() + rpos, 2);
	rpos += 2;
	phdr->rtype = ntohs(usTmp);
	memcpy(&usTmp, rbuf.GetBuffer() + rpos, 2);
	rpos += 2;
	phdr->rclass = ntohs(usTmp);
	memcpy(&ulTmp, rbuf.GetBuffer() + rpos, 4);
	rpos += 4;
	phdr->ttl = ntohl(ulTmp);
	memcpy(&usTmp, rbuf.GetBuffer() + rpos, 2);
	rpos += 2;
	phdr->rdlen = ntohs(usTmp);
	if (phdr->rclass != CL_IN)
		return false;

	return true;
}

bool CResolver::DecodeName(PCHAR pname, const CBuffer & buf, size_t & rpos)
{
	CPBYTE pbuf = buf.GetBuffer();
	size_t buflen = buf.GetSize();
	size_t pos = rpos;
	size_t namelen = 0;
	assert(buflen > 0 && buflen <= 512 && pos < buflen);

	bool bHasPtr = false;
	while (pbuf[pos]) {
		UINT8 len = pbuf[pos];
		if (!(len & 0xC0)) {
			// Label
			pos++;
			if (len >= buflen - pos || len + namelen > 254)
				return false;
			memcpy(pname, pbuf + pos, len);
			pos += len;
			*(pname + len) = '.';
			len++;
			pname += len;
			namelen += len;
			if (!bHasPtr)
				rpos += len;
		} else {
			// Pointer
			if ((len & 0xC0) != 0xC0 || pos > buflen - 2)
				return false;
			pos =
			    (UINT16) (pbuf[pos] & 0x3F) * 256 +
			    (UINT16) (pbuf[pos + 1]);
			if (pos >= buflen - 1)
				return false;
			rpos += 2;
			bHasPtr = true;
		}
	}
	if (!namelen)
		return false;	//XXX: is root domain
	*(pname - 1) = '\0';
	if (!bHasPtr)
		rpos++;
	return true;
}

/** LOG **
 *
 * $Log: resolver.cpp,v $
 * Revision 1.2  2003/11/17 16:14:02  mat
 * make-up
 *
 *
 */

