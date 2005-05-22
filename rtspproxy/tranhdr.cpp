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

#include "dbg.h"
#include "sock.h"
#include "tranhdr.h"

/**************************************
 *
 * CSingleTransportHdr
 *
 **************************************/
CSingleTransportHdr::CSingleTransportHdr(const CString & strValue) :
	m_strHdr(strValue),
        m_nServerPorts(0), 
        m_nClientPorts(0), 
        m_serverBasePort(0),
        m_clientBasePort(0), 
        m_bInterleaved(false)
{
	Parse(m_strHdr);
}

void CSingleTransportHdr::Parse(const CString & strValue)
{
	CPCHAR pCur, pNext;
	CPCHAR pVal;
	size_t nValLen = 0;
	CRtspTransportHdrField field;

	// Get first section as transport and check for tcp
	pCur = strValue;
	assert_or_ret(pCur != NULL);

	field.type = RTSP_TRANSPORTHDR_OTHER;
	pNext = strchr(pCur, ';');
	if (pNext != NULL) {
		field.strField.Set(pCur, (pNext - pCur));
		pNext++;
	} else {
		field.strField = pCur;
	}
	m_listFields.InsertTail(field);
	pCur = pNext;

	// Parse transport options
	while (pCur) {
		field.type = RTSP_TRANSPORTHDR_OTHER;

		pNext = strchr(pCur, ';');
		if (pNext != NULL) {
			field.strField.Set(pCur, (pNext - pCur));
			pVal = (CPCHAR) memchr(pCur, '=', (pNext - pCur));
			if (pVal) {
				pVal++;
				nValLen = pNext - pVal;
			}
			pNext++;
		} else {
			field.strField = pCur;
			pVal = strchr(pCur, '=');
			if (pVal) {
				pVal++;
				nValLen = strlen(pVal);
			}
		}

		if (pVal != NULL) {
			if (strncasecmp(pCur, "client_port=", 12) == 0) {
				field.type |= RTSP_TRANSPORTHDR_CLIENTPORT;

				m_nClientPorts = 1;
				//XXX: really cheezy parsing here
				if (memchr(pVal, '-', nValLen) != NULL) {
					m_nClientPorts = 2;
				}
				//XXX: verify we have a valid port
				m_clientBasePort = atoi(pVal);
			} else if (strncasecmp(pCur, "server_port=", 12) ==
				   0) {
				field.type |= RTSP_TRANSPORTHDR_SERVERPORT;

				m_nServerPorts = 1;
				//XXX: really cheezy parsing here
				if (memchr(pVal, '-', nValLen) != NULL) {
					m_nServerPorts = 2;
				}
				//XXX: verify we have a valid port
				m_serverBasePort = atoi(pVal);
			} else if (strncasecmp(pCur, "source=", 7) == 0) {
				field.type |= RTSP_TRANSPORTHDR_SOURCE;

				CString strHost(pVal, nValLen);
				m_addrSource.SetHost(strHost);
			} else if (strncasecmp(pCur, "interleaved=", 12) ==
				   0) {
				field.type |=
				    RTSP_TRANSPORTHDR_INTERLEAVED;

				m_bInterleaved = true;
				m_nServerPorts = m_nClientPorts = 1;
				//XXX: really cheezy parsing here
				if (memchr(pVal, '-', nValLen) != NULL) {
					m_nServerPorts = m_nClientPorts =
					    2;
				}
				//XXX: verify we have a valid port
				m_clientBasePort = m_serverBasePort =
				    atoi(pVal);
			}
		}
		m_listFields.InsertTail(field);
		pCur = pNext;
	}
}

void CSingleTransportHdr::GetServerBasePort(UINT16 * pBasePort, int *nPorts)
{
	*pBasePort = m_serverBasePort;
	*nPorts = m_nServerPorts;
}

void CSingleTransportHdr::GetClientBasePort(UINT16 * pBasePort,
					    int *nPorts)
{
	*pBasePort = m_clientBasePort;
	*nPorts = m_nClientPorts;
}

void CSingleTransportHdr::SetServerBasePort(UINT16 basePort)
{
	m_serverBasePort = basePort;
}

void CSingleTransportHdr::SetClientBasePort(UINT16 basePort)
{
	m_clientBasePort = basePort;
}

void CSingleTransportHdr::SetSourceAddr(const CInetAddr & addrSource)
{
	m_addrSource = addrSource;
}

bool CSingleTransportHdr::IsInterleaved(void)
{
	return m_bInterleaved;
}

CPCHAR CSingleTransportHdr::GetHdrString(void)
{
	char strRet[255];
	char *p = strRet;
	CRtspTransportHdrField field;
	CRtspTransportHdrFieldList::Iterator itr(m_listFields.Begin());
	while (itr) {
		field = *itr;
		if (field.type & RTSP_TRANSPORTHDR_CLIENTPORT) {
			if (m_nClientPorts == 2)
				p += sprintf(p, "client_port=%hu-%hu",
					     m_clientBasePort,
					     m_clientBasePort + 1);
			else if (m_nClientPorts == 1)
				p += sprintf(p, "client_port=%hu",
					     m_clientBasePort);
		} else if (field.type & RTSP_TRANSPORTHDR_SERVERPORT) {
			if (m_nServerPorts == 2)
				p += sprintf(p, "server_port=%hu-%hu",
					     m_serverBasePort,
					     m_serverBasePort + 1);
			else if (m_nServerPorts == 1)
				p += sprintf(p, "server_port=%hu",
					     m_serverBasePort);
		} else if (field.type & RTSP_TRANSPORTHDR_SOURCE) {
			p += snprintf(p, 255, "source=%s", inet_ntoa(m_addrSource));
		} else if (field.type & RTSP_TRANSPORTHDR_INTERLEAVED) {
			if (m_nServerPorts == 2)
				p += snprintf(p, 255, "interleaved=%hu-%hu",
					     m_serverBasePort,
					     m_serverBasePort + 1);
			else if (m_nServerPorts == 1)
				p += snprintf(p, 255, "interleaved=%hu",
					     m_serverBasePort);
		} else {
			p += snprintf(p, 255, "%s", (CPCHAR) field.strField);
		}
		itr++;
		if (itr) {
			*(p++) = ';';
		}
	}
	m_strHdr = strRet;
	return m_strHdr;
}

/**************************************
 *
 * CRequestTransportHdr
 *
 **************************************/
CRequestTransportHdr::CRequestTransportHdr(const CString & strValue):
m_strHdr(strValue), m_bIsInterleaved(true)
{
	Parse();
}

CRequestTransportHdr::~CRequestTransportHdr(void)
{
	while (!m_listTransports.IsEmpty()) {
		CSingleTransportHdr *pHdr = m_listTransports.RemoveHead();
		delete pHdr;
	}
}

void CRequestTransportHdr::Parse(void)
{
	CPCHAR pCur, pNext;
	CSingleTransportHdr *pSingleHdr = NULL;

	pCur = m_strHdr;
	assert_or_ret(pCur != NULL);
	while (pCur) {
		CString strSingleTran;
		pNext = strchr(pCur, ',');
		if (pNext) {
			strSingleTran.Set(pCur, (pNext - pCur));
			pNext++;
		} else {
			strSingleTran = pCur;
		}
		pSingleHdr = new CSingleTransportHdr(strSingleTran);

		m_bIsInterleaved = m_bIsInterleaved
		    && pSingleHdr->IsInterleaved();
		m_listTransports.InsertTail(pSingleHdr);

		pCur = pNext;
	}
}

void CRequestTransportHdr::GetBasePort(UINT16 * pBasePort, int *pnPorts)
{
	int nPorts = 0;
	UINT16 basePort = 0;
	CTransportHdrList::Iterator itr(m_listTransports.Begin());

	*pnPorts = 0;
	while (itr) {
		CSingleTransportHdr *pHdr = *itr;
		pHdr->GetClientBasePort(&basePort, &nPorts);
		if (nPorts == 2) {
			*pnPorts = nPorts;
			*pBasePort = basePort;
			return;
		} else if (nPorts > *pnPorts) {
			*pnPorts = nPorts;
			*pBasePort = basePort;
		}
		itr++;
	}
}

void CRequestTransportHdr::SetPort(UINT16 basePort)
{
	CTransportHdrList::Iterator itr(m_listTransports.Begin());

	while (itr) {
		CSingleTransportHdr *pHdr = *itr;
		pHdr->SetClientBasePort(basePort);
		itr++;
	}
}

bool CRequestTransportHdr::IsInterleaved(void)
{
	return m_bIsInterleaved;
}

CPCHAR CRequestTransportHdr::GetHdrString(void)
{
	m_strHdr = "";
	CTransportHdrList::Iterator itr(m_listTransports.Begin());
	while (itr) {
		CSingleTransportHdr *pHdr = *itr;
		m_strHdr.Append(pHdr->GetHdrString());
		itr++;
		if (itr) {
			m_strHdr.Append(",");
		}
	}
	return (CPCHAR) m_strHdr;
}

/** LOG **
 *
 * $Log: tranhdr.cpp,v $
 * Revision 1.3  2003/11/17 16:14:16  mat
 * make-up
 *
 *
 */

