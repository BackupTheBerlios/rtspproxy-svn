/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#include "url.h"

#include <stdlib.h>
#include <string.h>

CUrl::CUrl(void):m_scheme(SCHEME_INVALID)
{
	// Empty
}

CUrl::CUrl(const CString & strUrl):m_scheme(SCHEME_INVALID)
{
	Parse(strUrl);
}

CUrl::~CUrl(void)
{
}

bool CUrl::IsValid(void) const
{
	return (m_scheme != SCHEME_INVALID);
}

bool CUrl::Set(const CString & strUrl)
{
	return Parse(strUrl);
}

bool CUrl::Get(Scheme * pscheme, CString * pstrHost, UINT16 * pusPort,
	       CString * pstrPath) const
{
	if (m_scheme == SCHEME_INVALID)
		return false;

	*pscheme = m_scheme;
	*pstrHost = m_strHost;
	*pusPort = m_usPort;
	*pstrPath = m_strPath;
	return true;
}

CUrl::Scheme CUrl::GetScheme(void) const
{
	return m_scheme;
}

const CString & CUrl::GetHost(void) const
{
	return m_strHost;
}

UINT16 CUrl::GetPort(void) const
{
	return m_usPort;
}

const CString & CUrl::GetPath(void) const
{
	return m_strPath;
}

bool CUrl::Parse(const CString & strUrl)
{
	CPCHAR p = NULL;

	//FIXME: Cheap and sleazy and *incomplete* parser for HTTP and RTSP only
	if (0 == strncasecmp(strUrl, "http://", 7)) {
		m_scheme = SCHEME_HTTP;
		m_usPort = 80;
	} else if (0 == strncasecmp(strUrl, "rtsp://", 7)) {
		m_scheme = SCHEME_RTSP;
		m_usPort = 554;
	} else {
		return false;
	}

	// Found host, look for port or path
	p = (CPCHAR) strUrl + 7;
	while (*p && *p != ':' && *p != '/')
		p++;

	m_strHost.Set((CPCHAR) strUrl + 7, p - (CPCHAR) strUrl - 7);

	if (*p == ':') {
		// Found port
		m_usPort = atoi(p + 1);
		while (*p && *p != '/')
			p++;
	}
	if (*p) {
		// Found path
		m_strPath = p + 1;
	}

	return true;
}

/** LOG **
 *
 * $Log: url.cpp,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

