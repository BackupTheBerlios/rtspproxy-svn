/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>

#include "app.h"
#include "rtspprot.h"

#include "dbg.h"

/**************************************
 *
 * RTSP "pseudo-socket" class
 *
 **************************************/

CRtspInterleavedSocket::CRtspInterleavedSocket(void):CSocket(),
m_pProt(NULL), m_chan(0), m_pbuf(NULL)
{
	// Empty
}

CRtspInterleavedSocket::CRtspInterleavedSocket(CStreamResponse * pResponse):
CSocket(pResponse), m_pProt(NULL), m_chan(0), m_pbuf(NULL)
{
	// Empty
}

CRtspInterleavedSocket::~CRtspInterleavedSocket(void)
{
	Close();
	delete m_pbuf;
}

bool CRtspInterleavedSocket::IsOpen(void)
{
	return (m_pProt != NULL);
}

void CRtspInterleavedSocket::Close(void)
{
	if (m_pProt) {
		m_pProt->m_ppSockets[m_chan] = NULL;
		m_pProt = NULL;
		if (m_pResponse)
			m_pResponse->OnClosed();
	}
}

size_t CRtspInterleavedSocket::Read(PVOID pbuf, size_t nLen)
{
	assert_or_retv(0, (m_pbuf != NULL));

	// We will treat the read as a datagram -- copy as much as will fit and
	// discard any remainder
	size_t copylen = min(nLen, m_pbuf->GetSize());

	memcpy(pbuf, m_pbuf->GetBuffer(), copylen);
	delete m_pbuf;
	m_pbuf = NULL;
	return copylen;
}

size_t CRtspInterleavedSocket::Write(CPVOID pbuf, size_t nLen)
{
	assert_or_retv(0, (pbuf != NULL && nLen < 0xFFFF));
	assert_or_retv(0, (m_chan < 256 && m_pProt != NULL));
	assert_or_retv(0, (m_pProt->m_psock != NULL));

	CSocket *psock = m_pProt->m_psock;
	BYTE hdr[4];
	hdr[0] = 0x24;
	hdr[1] = m_chan;
	hdr[2] = nLen >> 8;
	hdr[3] = nLen & 0xFF;

	//TODO: implement vector read/write
	size_t n = 0;
	if (psock->Write(hdr, 4) == 4) {
		n = psock->Write(pbuf, nLen);
	}
	return n;
}

bool CRtspInterleavedSocket::Connect(UINT chan, CRtspProtocol * pProt)
{
	assert(chan < 256 && pProt != NULL);
	assert_or_retv(false, (m_pProt == NULL));

	if (pProt->m_ppSockets[chan])
		return false;
	pProt->m_ppSockets[chan] = this;
	m_chan = chan;
	m_pProt = pProt;
	return true;
}

/**************************************
 *
 * RTSP protocol class
 *
 **************************************/

const char *s_szHeaders[] = {
	// From s6.2
	"Accept",
	"Accept-Encoding",
	"Accept-Language",
	"Authorization",
	"From",
	"If-Modified-Since",
	"Range",
	"Referer",
	"User-Agent",
	NULL
};

// Initial and max line buffer lengths
#define MIN_READ_BUF 32
#define MAX_READ_BUF 1024
#define MAX_BODY_LEN 16384

CRtspProtocol::CRtspProtocol(CRtspProtocolResponse * pResponse):
m_pResponse(pResponse),
m_state(stCmd),
m_psock(NULL),
m_nBufLen(0),
m_pReadBuf(NULL),
m_pTail(NULL), m_cseqSend(0), m_cseqRecv(0), m_pmsg(NULL), m_nBodyLen(0)
{
	memset(m_ppSockets, 0, 256 * sizeof(CRtspInterleavedSocket *));
}

CRtspProtocol::~CRtspProtocol(void)
{
	delete m_psock;
	delete[]m_pReadBuf;
	delete m_pmsg;
}

void CRtspProtocol::Init(CSocket * pSocket)
{
	m_psock = pSocket;
	m_psock->SetResponse(this);
	m_psock->Select(SF_READ);
}

#define FUDGE 16
void CRtspProtocol::SendRequest(CRtspRequestMsg * pmsg)
{
	assert_or_ret(pmsg);

	UINT32 cseq = 0;
	CString strCSeq = pmsg->GetHdr("CSeq");
	if (strCSeq.IsEmpty()) {
		char buf[16];
		cseq = GetNextCseq();
		sprintf(buf, "%u", cseq);
		pmsg->SetHdr("CSeq", buf);
	} else {
		cseq = atoi(strCSeq);
	}

	CPCHAR pVerb = pmsg->GetVerbStr();
	CPCHAR pUrl = pmsg->GetUrl();
	size_t nHdrLen = pmsg->GetHdrLen();
	size_t nBufLen = pmsg->GetBufLen();

	// <verb> SP <url> SP "RTSP/1.0" CRLF
	// <headers> CRLF <buf>
	int len =
	    FUDGE + strlen(pVerb) + 1 + strlen(pUrl) + 1 + 8 + 2 +
	    nHdrLen + 2 + nBufLen;
	char *pbuf =
	    new char[FUDGE + strlen(pVerb) + 1 + strlen(pUrl) + 1 + 8 + 2 +
		     nHdrLen + 2 + nBufLen];
	char *p = pbuf;
	if (!pbuf) {
		printf("Out of memory\n");
		exit(-1);
	}

	p += sprintf(pbuf, "%s %s RTSP/1.0\r\n", pVerb, pUrl);
	for (UINT n = 0; n < pmsg->GetHdrCount(); n++) {
		CRtspHdr *pHdr = pmsg->GetHdr(n);
		p += sprintf(p, "%s: %s\r\n", (CPCHAR) pHdr->GetKey(),
			     (CPCHAR) pHdr->GetVal());
	}
	p += sprintf(p, "\r\n");
	if (nBufLen) {
		memcpy(p, pmsg->GetBuf(), nBufLen);
		p += nBufLen;
	}
        assert(p - pbuf <= len);

    printf("\nREQUEST:\n%s\n", pbuf );

	m_psock->Write(pbuf, p - pbuf);
	delete[]pbuf;

	m_listRequestQueue.
	    InsertTail(CRtspRequestTag(cseq, pmsg->GetVerb()));
}

void CRtspProtocol::SendResponse(CRtspResponseMsg * pmsg)
{
	UINT nCode = pmsg->GetStatusCode();
	CPCHAR pReason = pmsg->GetStatusMsg();
	size_t nHdrLen = pmsg->GetHdrLen();
	size_t nBufLen = pmsg->GetBufLen();

	// "RTSP/1.0" SP <code> SP <reason> CRLF
	// <headers> CRLF
	// <buf> (or terminating NULL from sprintf() if no buffer)
	size_t nResponseLen =
	    8 + 1 + 3 + 1 + strlen(pReason) + 2 + nHdrLen + 2 + nBufLen;
	if (0 == nBufLen)
		nResponseLen++;
	char *pbuf = new char[nResponseLen];
	char *p = pbuf;
	if (!pbuf) {
		printf("Out of memory\n");
		exit(-1);
	}

	p += sprintf(pbuf, "RTSP/1.0 %u %s\r\n", nCode, pReason);
	for (UINT n = 0; n < pmsg->GetHdrCount(); n++) {
		CRtspHdr *pHdr = pmsg->GetHdr(n);
		p += sprintf(p, "%s: %s\r\n", (CPCHAR) pHdr->GetKey(),
			     (CPCHAR) pHdr->GetVal());
	}
	p += sprintf(p, "\r\n");
	if (nBufLen) {
		memcpy(p, pmsg->GetBuf(), nBufLen);
		p += nBufLen;
	}

    printf("\nRESPONSE:\n%s\n", pbuf );

	m_psock->Write(pbuf, p - pbuf);
	delete[]pbuf;
}

// Parse newly read block and find start of next line
// If no EOL found: returns NULL
// If EOL found: Terminates line, returns ptr to next line
char *CRtspProtocol::parseLine(void)
{
	char *p = m_pReadBuf;
	char *pNextLine = NULL;
	while (p < m_pTail) {
		if (*p == '\n') {
			pNextLine = p + 1;
			// We found EOL but it may not be the end of the logical header.
			// First check for CRLF and adjust p, then if either the line is
			// empty, or the next line does not start with LWS, it's the end.
			if (p > m_pReadBuf && *(p - 1) == '\r')
				p--;
			if (p == m_pReadBuf
			    || (pNextLine < m_pTail && *pNextLine != ' '
				&& *pNextLine != '\t')) {
				*p = '\0';	// Terminate header
				break;
			}
			// It's not the end of the logical header so keep going
			p = pNextLine - 1;
			pNextLine = NULL;
		}
		p++;
	}

	if (!pNextLine && m_pTail == m_pReadBuf + m_nBufLen) {
		size_t nNewBufLen = m_nBufLen * 2;
		if (nNewBufLen > MAX_READ_BUF) {
			dbgout("CRtspProtocol::parseLine: Line too long");
			m_psock->Close();
			m_state = stFail;
			return NULL;
		}

		char *pNewBuf = new char[nNewBufLen];
		if (!pNewBuf) {
			dbgout("CRtspProtocol::parseLine: Out of memory");
			m_psock->Close();
			m_state = stFail;
			return NULL;
		}
		memcpy(pNewBuf, m_pReadBuf, m_nBufLen);
		m_pTail = pNewBuf + (m_pTail - m_pReadBuf);
		m_nBufLen = nNewBufLen;
		delete[]m_pReadBuf;
		m_pReadBuf = pNewBuf;
	}

	return pNextLine;
}

void CRtspProtocol::handleReadCmd(void)
{
	if (0 == strncmp(m_pReadBuf, "RTSP", 4) &&
	    m_pReadBuf[4] == '/' && isdigit(m_pReadBuf[5]) &&
	    m_pReadBuf[6] == '.' && isdigit(m_pReadBuf[7]) &&
	    m_pReadBuf[8] == ' ') {
		// Response: RTSP/#.# <code> <reason>
		if (!isdigit(m_pReadBuf[9]) || !isdigit(m_pReadBuf[10])
		    || !isdigit(m_pReadBuf[11]) || m_pReadBuf[12] != ' '
		    || m_pReadBuf[13] == ' ' || m_pReadBuf[13] == '\0') {
			dbg("RTSP protocol error: bad status line");
			m_state = stCmd;
			return;
		}

		assert(NULL == m_pmsg);
		CRtspResponseMsg *pmsg = new CRtspResponseMsg();
		pmsg->SetStatus(atoi(m_pReadBuf + 9));
		//pmsg->SetReason( m_pReadBuf+13 );
		m_pmsg = pmsg;
		m_state = stHdr;
	} else {
		// Request: <verb> <url> RTSP/#.#
		char *p = m_pReadBuf;
		if (*p == ' ') {
			dbg("RTSP protocol error: bad request line");
			m_state = stCmd;
			return;
		}
		char *pVerb = p;
		while (*p && *p != ' ')
			p++;
		if (*p != ' ' || *(p + 1) == ' ') {
			dbg("RTSP protocol error: bad request line");
			m_state = stCmd;
			return;
		}
		*p++ = '\0';
		char *pUrl = p;
		while (*p && *p != ' ')
			p++;
		if (*p != ' ' || *(p + 1) == ' ') {
			dbg("RTSP protocol error: bad request line");
			m_state = stCmd;
			return;
		}
		*p++ = '\0';
		if (0 != strncmp(p, "RTSP", 4) ||
		    p[4] != '/' || !isdigit(p[5]) ||
		    p[6] != '.' || !isdigit(p[7]) || p[8] != '\0') {
			dbg("RTSP protocol error: bad request line");
			m_state = stCmd;
			return;
		}

		CRtspRequestMsg *pmsg = new CRtspRequestMsg();
		pmsg->SetVerb(pVerb);
		pmsg->SetUrl(pUrl);
		m_pmsg = pmsg;
		m_state = stHdr;
	}
}

void CRtspProtocol::handleReadHdr(void)
{
	//XXX: headers need a better parser
	if ('\0' == *m_pReadBuf) {
		// Empty line indicates end of headers
		// Note: body may not be present, that's cool
		CString strBodyLen = m_pmsg->GetHdr("Content-Length");
		m_nBodyLen = atoi(strBodyLen);
		m_state = stBody;
	} else {
		char *p = m_pReadBuf;
		if (*p == ':') {
			dbg
			    ("RTSP protocol error: received header line '%s'",
			     m_pReadBuf);
			return;
		}
		while (*p && *p != ':')
			p++;
		if (*p != ':') {
			dbg
			    ("RTSP protocol error: received header line '%s'",
			     m_pReadBuf);
			return;
		}
		// Break into key and val, skip LWS, and save it
		*p++ = '\0';
		while (*p == ' ')
			p++;
		if (*p) {
			m_pmsg->SetHdr(m_pReadBuf, p);
		}
	}
}

void CRtspProtocol::handleReadBody(void)
{
	if (m_pTail >= m_pReadBuf + m_nBodyLen) {
		if (m_nBodyLen)
			m_pmsg->SetBuf((CPBYTE) m_pReadBuf, m_nBodyLen);
		m_state = stDispatch;
	} else {
		if (m_nBufLen < m_nBodyLen) {
			char *pNewBuf = new char[m_nBodyLen];
			if (!pNewBuf) {
				dbg
				    ("CRtspProtocol::handleReadBody: Out of memory");
				m_psock->Close();
				m_state = stFail;
				return;
			}
			memcpy(pNewBuf, m_pReadBuf, m_nBufLen);
			m_pTail = pNewBuf + (m_pTail - m_pReadBuf);
			m_nBufLen = m_nBodyLen;
			delete[]m_pReadBuf;
			m_pReadBuf = pNewBuf;
		}
	}
}

void CRtspProtocol::handleReadPkt(void)
{
	assert(NULL != m_pResponse);
	if (m_pTail < m_pReadBuf + 4)
		return;

	if (!m_nBodyLen) {
		UINT16 usTmp;
		memcpy(&usTmp, m_pReadBuf + 2, 2);
		m_nBodyLen = 4 + ntohs(usTmp);

		if (m_nBufLen < m_nBodyLen) {
			char *pNewBuf = new char[m_nBodyLen];
			if (!pNewBuf) {
				dbg
				    ("CRtspProtocol::handleReadPkt: Out of memory");
				m_psock->Close();
				m_state = stFail;
				return;
			}
			memcpy(pNewBuf, m_pReadBuf, m_nBufLen);
			m_pTail = pNewBuf + (m_pTail - m_pReadBuf);
			m_nBufLen = m_nBodyLen;
			delete[]m_pReadBuf;
			m_pReadBuf = pNewBuf;
		}
	}

	if (m_pTail >= m_pReadBuf + m_nBodyLen) {
		BYTE chan = *((BYTE *) m_pReadBuf + 1);
		CRtspInterleavedSocket *psock = m_ppSockets[chan];
		if (psock) {
			assert(!psock->m_pbuf);	// the last packet should be gone
			delete psock->m_pbuf;
			psock->m_pbuf =
			    new CBuffer((BYTE *) m_pReadBuf + 4,
					m_nBodyLen - 4);
			psock->m_pResponse->OnReadReady();
		} else {
			m_pResponse->OnError(RTSPE_NOTRAN);
		}

		m_state = stReady;
	}
}

void CRtspProtocol::dispatchMessage(void)
{
	assert(NULL != m_pResponse);
	assert(NULL != m_pmsg);

	switch (m_pmsg->GetType()) {
	case RTSP_TYPE_REQUEST:
		{
			CRtspRequestMsg *pmsg = (CRtspRequestMsg *) m_pmsg;
			switch (pmsg->GetVerb()) {
			case VERB_DESCRIBE:
				m_pResponse->OnDescribeRequest(pmsg);
				break;
			case VERB_ANNOUNCE:
				m_pResponse->OnAnnounceRequest(pmsg);
				break;
			case VERB_GETPARAM:
				m_pResponse->OnGetParamRequest(pmsg);
				break;
			case VERB_SETPARAM:
				m_pResponse->OnSetParamRequest(pmsg);
				break;
			case VERB_OPTIONS:
				m_pResponse->OnOptionsRequest(pmsg);
				break;
			case VERB_PAUSE:
				m_pResponse->OnPauseRequest(pmsg);
				break;
			case VERB_PLAY:
				m_pResponse->OnPlayRequest(pmsg);
				break;
			case VERB_RECORD:
				m_pResponse->OnRecordRequest(pmsg);
				break;
			case VERB_REDIRECT:
				m_pResponse->OnRedirectRequest(pmsg);
				break;
			case VERB_SETUP:
				m_pResponse->OnSetupRequest(pmsg);
				break;
			case VERB_TEARDOWN:
				m_pResponse->OnTeardownRequest(pmsg);
				break;
			default:
				assert(false);
			}
		}
		break;
	case RTSP_TYPE_RESPONSE:
		{
			UINT cseq = atoi(m_pmsg->GetHdr("CSeq"));
			CRtspRequestTagList::
			    Iterator itr(m_listRequestQueue.Begin());
			while (itr) {
				if (cseq == (*itr).m_cseq) {
					CRtspResponseMsg *pmsg =
					    (CRtspResponseMsg *) m_pmsg;
					switch ((*itr).m_verb) {
					case VERB_DESCRIBE:
						m_pResponse->
						    OnDescribeResponse
						    (pmsg);
						break;
					case VERB_ANNOUNCE:
						m_pResponse->
						    OnAnnounceResponse
						    (pmsg);
						break;
					case VERB_GETPARAM:
						m_pResponse->
						    OnGetParamResponse
						    (pmsg);
						break;
					case VERB_SETPARAM:
						m_pResponse->
						    OnSetParamResponse
						    (pmsg);
						break;
					case VERB_OPTIONS:
						m_pResponse->
						    OnOptionsResponse
						    (pmsg);
						break;
					case VERB_PAUSE:
						m_pResponse->
						    OnPauseResponse(pmsg);
						break;
					case VERB_PLAY:
						m_pResponse->
						    OnPlayResponse(pmsg);
						break;
					case VERB_RECORD:
						m_pResponse->
						    OnRecordResponse(pmsg);
						break;
					case VERB_REDIRECT:
						m_pResponse->
						    OnRedirectResponse
						    (pmsg);
						break;
					case VERB_SETUP:
						m_pResponse->
						    OnSetupResponse(pmsg);
						break;
					case VERB_TEARDOWN:
						m_pResponse->
						    OnTeardownResponse
						    (pmsg);
						break;
					default:
						assert(false);
					}

					m_listRequestQueue.Remove(itr);
					break;
				}
				itr++;
			}
		}
		break;
	default:
		assert(false);
	}

	delete m_pmsg;
	m_pmsg = NULL;
	m_nBodyLen = 0;
	m_state = stReady;
}

void CRtspProtocol::OnConnectDone(int err)
{
	// Socket must be connected before Init()
	assert(false);
}

void CRtspProtocol::OnReadReady(void)
{
	if (!m_pReadBuf) {
		assert(m_state < stBody);
		m_pReadBuf = new char[MIN_READ_BUF];
		m_nBufLen = MIN_READ_BUF;
		m_pTail = m_pReadBuf;
	}

	size_t len = m_nBufLen - (m_pTail - m_pReadBuf);
	size_t n = m_psock->Read(m_pTail, len);
	if (n == SOCKERR_EOF) {
		dbgout("CRtspProtocol::OnReadReady: Socket read failed");
		m_psock->Close();
		return;
	}
	m_pTail += n;

	bool bParsing = true;
	while (bParsing) {
		char *pNextLine = NULL;
		size_t nParsed = 0;
		bParsing = false;
		switch (m_state) {
		case stReady:
			if (m_pTail > m_pReadBuf) {
				m_state =
				    ('$' == *m_pReadBuf) ? stPkt : stCmd;
				nParsed = 0;
				bParsing = true;
			}
			break;
		case stPkt:
			handleReadPkt();
			if (m_state == stReady) {
				nParsed = m_nBodyLen;
				m_nBodyLen = 0;
				bParsing = true;
			}
			break;
		case stCmd:
			pNextLine = parseLine();
			if (pNextLine) {
				handleReadCmd();
				nParsed = (pNextLine - m_pReadBuf);
				bParsing = true;
			}
			break;
		case stHdr:
			pNextLine = parseLine();
			if (pNextLine) {
				handleReadHdr();
				nParsed = (pNextLine - m_pReadBuf);
				bParsing = true;
			}
			break;
		case stBody:
			handleReadBody();
			if (m_state == stDispatch) {
				nParsed = m_nBodyLen;
				bParsing = true;
			}
			break;
		case stDispatch:
			dispatchMessage();
			nParsed = 0;
			bParsing = true;
			break;
		default:
			assert(false);
		}

		if (nParsed) {
			char *pFrom = m_pReadBuf + nParsed;
			size_t len = (m_pTail - m_pReadBuf) - nParsed;
			memmove(m_pReadBuf, pFrom, len);
			m_pTail -= nParsed;
		}
	}
}

void CRtspProtocol::OnWriteReady(void)
{
	assert(false);
}

void CRtspProtocol::OnExceptReady(void)
{
	assert(false);
}

void CRtspProtocol::OnClosed(void)
{
	assert(m_pResponse);
	dbgout("CRtspProtocol::OnClosed: Socket closed");
	m_pResponse->OnError(RTSPE_CLOSED);
}

/** LOG **
 *
 * $Log: rtspprot.cpp,v $
 * Revision 1.2  2003/11/17 16:14:08  mat
 * make-up
 *
 *
 */

