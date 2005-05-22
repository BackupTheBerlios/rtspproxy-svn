/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#include "dbg.h"
#include "rtspmsg.h"

#include <string.h>		/* memset */

/**************************************
 *
 * CRtspHdr
 *
 **************************************/

CRtspHdr::CRtspHdr(const CString & strKey):
m_strKey(strKey)
{
	// Empty
}

CRtspHdr::CRtspHdr(const CString & strKey,
		   const CString & strVal):m_strKey(strKey),
m_strVal(strVal)
{
	// Empty
}

const CString & CRtspHdr::GetKey(void) const
{
	return m_strKey;
}

const CString & CRtspHdr::GetVal(void) const
{
	return m_strVal;
}

void CRtspHdr::SetVal(const CString & strVal)
{
	m_strVal = strVal;
}

/**************************************
 *
 * CRtspMsg
 *
 **************************************/

CRtspMsg::CRtspMsg(void):m_nRtspVer(0),
m_nSeq(0), m_nBufLen(0), m_pbuf(NULL)
{
	// Empty
}

CRtspMsg::CRtspMsg(const CRtspMsg & other)
{
	CRtspHdrList::ConstIterator itr(other.m_listHdrs.Begin());
	while (itr) {
		CRtspHdr *pHdr = *itr;
		m_listHdrs.InsertTail(new CRtspHdr(*pHdr));
		itr++;
	}

	m_nBufLen = other.m_nBufLen;
	m_pbuf = NULL;
	if (m_nBufLen) {
		m_pbuf = new BYTE[m_nBufLen];
		memcpy(m_pbuf, other.m_pbuf, m_nBufLen);
	}
}

CRtspMsg::~CRtspMsg(void)
{
	while (!m_listHdrs.IsEmpty()) {
		CRtspHdr *pHdr = m_listHdrs.RemoveHead();
		delete pHdr;
	}

	delete[]m_pbuf;
	m_pbuf = NULL;
}

const CRtspMsg & CRtspMsg::operator=(const CRtspMsg & other)
{
	m_nRtspVer = other.m_nRtspVer;
	m_nSeq = other.m_nSeq;

	while (!m_listHdrs.IsEmpty()) {
		CRtspHdr *pHdr = m_listHdrs.RemoveHead();
		delete pHdr;
	}
	CRtspHdrList::ConstIterator itr(other.m_listHdrs.Begin());
	while (itr) {
		CRtspHdr *pHdr = *itr;
		m_listHdrs.InsertTail(new CRtspHdr(*pHdr));
		itr++;
	}

	m_nBufLen = other.m_nBufLen;
	delete[]m_pbuf;
	m_pbuf = NULL;
	if (m_nBufLen) {
		m_pbuf = new BYTE[m_nBufLen];
		memcpy(m_pbuf, other.m_pbuf, m_nBufLen);
	}

	return *this;
}

CRtspMsg::operator  CPBYTE(void) const
{
	return m_pbuf;
}

BYTE CRtspMsg::operator[] (UINT nPos)
const {
	return GetAt(nPos);
} RtspMsgType CRtspMsg::GetType(void) const
{
	return RTSP_TYPE_NONE;
}

size_t CRtspMsg::GetHdrLen(void) const
{
	size_t nLen = 0;
	CRtspHdrList::ConstIterator itr(m_listHdrs.Begin());
	while (itr) {
		CRtspHdr *pHdr = *itr;
		nLen +=
		    (pHdr->GetKey().GetLength() + 2 +
		     pHdr->GetVal().GetLength() + 2);
		itr++;
	}

	return nLen;
}

size_t CRtspMsg::GetBufLen(void) const
{
	return m_nBufLen;
}

BYTE CRtspMsg::GetAt(UINT nPos) const
{
	return m_pbuf[nPos];
}

void CRtspMsg::SetAt(UINT nPos, BYTE by)
{
	m_pbuf[nPos] = by;
}

void CRtspMsg::GetRtspVer(UINT * puMajor, UINT * puMinor) const
{
	assert(puMajor && puMinor);

	*puMajor = HIWORD(m_nRtspVer);
	*puMinor = LOWORD(m_nRtspVer);
}

void CRtspMsg::SetRtspVer(UINT uMajor, UINT uMinor)
{
	assert(uMajor < 10 && uMinor < 10);

	m_nRtspVer = MAKEDWORD(uMajor, uMinor);
}

size_t CRtspMsg::GetHdrCount(void) const
{
	return m_listHdrs.GetCount();
}

CString CRtspMsg::GetHdr(const CString & strKey) const
{
	CString strVal;

	CRtspHdrList::ConstIterator itr(m_listHdrs.Begin());
	while (itr) {
		CRtspHdr *pHdr = *itr;
		if (0 == strcasecmp(strKey, pHdr->GetKey())) {
			strVal = pHdr->GetVal();
			break;
		}
		itr++;
	}

	return strVal;
}

CRtspHdr *CRtspMsg::GetHdr(UINT nIndex) const
{
	CRtspHdrList::ConstIterator itr(m_listHdrs.Begin());
	for (UINT n = 0; n < nIndex; n++) {
		itr++;
	}
	return *itr;
}

void CRtspMsg::SetHdr(const CString & strKey, const CString & strVal)
{
	CRtspHdrList::Iterator itr(m_listHdrs.Begin());
	while (itr) {
		CRtspHdr *pHdr = *itr;
		if (0 == strcasecmp(strKey, pHdr->GetKey())) {
			pHdr->SetVal(strVal);
			return;
		}
		itr++;
	}
	m_listHdrs.InsertTail(new CRtspHdr(strKey, strVal));
}

void CRtspMsg::SetHdr(const CRtspHdr & hdrNew)
{
	CRtspHdrList::Iterator itr(m_listHdrs.Begin());
	while (itr) {
		CRtspHdr *pHdr = *itr;
		if (hdrNew.GetKey() == pHdr->GetKey()) {
			pHdr->SetVal(hdrNew.GetVal());
			return;
		}
		itr++;
	}
	m_listHdrs.InsertTail(new CRtspHdr(hdrNew));
}

PBYTE CRtspMsg::GetBuf(void) const
{
	return m_pbuf;
}

void CRtspMsg::SetBuf(CPBYTE buf, size_t nLen)
{
	delete[]m_pbuf;
	m_pbuf = NULL;
	m_nBufLen = nLen;
	if ( m_nBufLen ) {
		m_pbuf = (uint8_t *)malloc( m_nBufLen );
		//new BYTE[m_nBufLen];
		memcpy(m_pbuf, buf, m_nBufLen);
	}
}

/**************************************
 *
 * CRtspRequestMsg
 *
 **************************************/

// These correspond with enum RtspVerb and must be sorted
static CPCHAR s_pVerbs[] = {
	"-NONE-",
	"ANNOUNCE",
	"DESCRIBE",
	"GET_PARAMETER",
	"OPTIONS",
	"PAUSE",
	"PLAY",
	"RECORD",
	"REDIRECT",
	"SETUP",
	"SET_PARAMETER",
	"TEARDOWN",
	NULL
};
static const UINT s_nVerbs = sizeof(s_pVerbs) / sizeof(CPCHAR) - 1;

CRtspRequestMsg::CRtspRequestMsg(void):CRtspMsg(), m_verb(VERB_NONE)
{
	// Empty
}

CRtspRequestMsg::
CRtspRequestMsg(const CRtspRequestMsg & other):CRtspMsg(other)
{
	m_verb = other.m_verb;
	m_strUrl = other.m_strUrl;
}

CRtspRequestMsg::~CRtspRequestMsg(void)
{
	// Empty
}

const CRtspRequestMsg & CRtspRequestMsg::
operator=(const CRtspRequestMsg & other)
{
	m_verb = other.m_verb;
	m_strUrl = other.m_strUrl;

	return *this;
}

RtspMsgType CRtspRequestMsg::GetType(void) const
{
	return RTSP_TYPE_REQUEST;
}

RtspVerb CRtspRequestMsg::GetVerb(void) const
{
	return m_verb;
}

CPCHAR CRtspRequestMsg::GetVerbStr(void) const
{
	return s_pVerbs[m_verb];
}

void CRtspRequestMsg::SetVerb(RtspVerb verb)
{
	assert(verb > VERB_NONE && verb < VERB_LAST);

	m_verb = verb;
}

void CRtspRequestMsg::SetVerb(CPCHAR szVerb)
{
	m_verb = VERB_NONE;

	int hi = s_nVerbs;
	int lo = -1;
	int mid;
	while (hi - lo > 1) {
		mid = (hi + lo) / 2;
		if (strcmp(szVerb, s_pVerbs[mid]) <= 0)
			hi = mid;
		else
			lo = mid;
	}
	if (0 == strcmp(szVerb, s_pVerbs[hi])) {
		m_verb = (RtspVerb) hi;
	}
	assert(VERB_NONE != m_verb);
}

CPCHAR CRtspRequestMsg::GetUrl(void) const
{
	return (CPCHAR) m_strUrl;
}

void CRtspRequestMsg::SetUrl(const CString & strUrl)
{
	m_strUrl = strUrl;
}

/**************************************
 *
 * CRtspResponseMsg
 *
 **************************************/

struct StatusMapEntry {
	UINT nCode;
	CPCHAR szName;
};

// These must be sorted
static StatusMapEntry s_mapStatus[] = {
	{100, "Continue"},

	{200, "OK"},
	{201, "Created"},
	{250, "Low on Storage Space"},

	{300, "Multiple Choices"},
	{301, "Moved Permanently"},
	{302, "Moved Temporarily"},
	{303, "See Other"},
	{304, "Not Modified"},
	{305, "Use Proxy"},

	{400, "Bad Request"},
	{401, "Unauthorized"},
	{402, "Payment Required"},
	{403, "Forbidden"},
	{404, "Not Found"},
	{405, "Method Not Allowed"},
	{406, "Not Acceptable"},
	{407, "Proxy Authentication Required"},
	{408, "Request Time-out"},
	{410, "Gone"},
	{411, "Length Required"},
	{412, "Precondition Failed"},
	{413, "Request Entity Too Large"},
	{414, "Request-URI Too Large"},
	{415, "Unsupported Media Type"},
	{451, "Parameter Not Understood"},
	{452, "Conference Not Found"},
	{453, "Not Enough Bandwidth"},
	{454, "Session Not Found"},
	{455, "Method Not Valid in This State"},
	{456, "Header Field Not Valid for Resource"},
	{457, "Invalid Range"},
	{458, "Parameter Is Read-Only"},
	{459, "Aggregate operation not allowed"},
	{460, "Only aggregate operation allowed"},
	{461, "Unsupported transport"},
	{462, "Destination unreachable"},

	{500, "Internal Server Error"},
	{501, "Not Implemented"},
	{502, "Bad Gateway"},
	{503, "Service Unavailable"},
	{504, "Gateway Time-out"},
	{505, "RTSP Version not supported"},
	{551, "Option not supported"},
	{0, NULL}
};
static const UINT s_nStatusEntries =
    sizeof(s_mapStatus) / sizeof(StatusMapEntry) - 1;

CRtspResponseMsg::CRtspResponseMsg(void):CRtspMsg(), m_nCode(0)
{
	// Empty
}

CRtspResponseMsg::
CRtspResponseMsg(const CRtspResponseMsg & other):CRtspMsg(other)
{
	m_nCode = other.m_nCode;
	m_strStatusMsg = other.m_strStatusMsg;
}

CRtspResponseMsg::~CRtspResponseMsg(void)
{
	// Empty
}

const CRtspResponseMsg & CRtspResponseMsg::
operator=(const CRtspResponseMsg & other)
{
	m_nCode = other.m_nCode;
	m_strStatusMsg = other.m_strStatusMsg;

	return *this;
}

RtspMsgType CRtspResponseMsg::GetType(void) const
{
	return RTSP_TYPE_RESPONSE;
}

UINT CRtspResponseMsg::GetStatusCode(void) const
{
	return m_nCode;
}

const CString & CRtspResponseMsg::GetStatusMsg(void) const
{
	return m_strStatusMsg;
}

void CRtspResponseMsg::SetStatus(UINT nCode, CPCHAR szMsg /* = NULL */ )
{
	assert(nCode >= 100 && nCode <= 999);

	m_nCode = nCode;
	if (!szMsg) {
		szMsg = "Unknown";

		int hi = s_nStatusEntries;
		int lo = -1;
		int mid;
		while (hi - lo > 1) {
			mid = (hi + lo) / 2;
			if (nCode <= s_mapStatus[mid].nCode)
				hi = mid;
			else
				lo = mid;
		}
		if (nCode == s_mapStatus[hi].nCode) {
			szMsg = s_mapStatus[hi].szName;
		}
	}
	m_strStatusMsg = szMsg;
}

/** LOG **
 *
 * $Log: rtspmsg.cpp,v $
 * Revision 1.2  2003/11/17 16:14:08  mat
 * make-up
 *
 *
 */

