/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _URL_H
#define _URL_H

#include "types.h"
#include "str.h"

/*
 * Should we make a virtual base CUrl and derive? eg.
 * CUrl
 *   CFtpUrl
 *   CHttpUrl
 *   ...
 */

class CUrl {
      public:
	CUrl(void);
	 CUrl(const CString & strUrl);
	 virtual ~ CUrl(void);

      public:
	enum Scheme {
		SCHEME_INVALID,
		SCHEME_FTP,
		SCHEME_HTTP,
		SCHEME_GOPHER,
		SCHEME_MAILTO,
		SCHEME_NEWS,
		SCHEME_NNTP,	// Why news and nntp?
		SCHEME_TELNET,
		SCHEME_WAIS,
		SCHEME_FILE,
		SCHEME_PROSPERO,	// WTF is this doing here?
		SCHEME_RTSP,	// Not in RFC1738
		SCHEME_UNKNOWN
	};

      public:
	 bool IsValid(void) const;
	bool Set(const CString & strUrl);
	bool Get(Scheme * pscheme, CString * pstrHost, UINT16 * pusPort,
		 CString * pstrPath) const;
	Scheme GetScheme(void) const;
	const CString & GetHost(void) const;
	UINT16 GetPort(void) const;
	const CString & GetPath(void) const;

      protected:
	 bool Parse(const CString & strUrl);

      protected:
	 Scheme m_scheme;
	CString m_strHost;
	UINT16 m_usPort;
	CString m_strPath;
};

#endif				//ndef _URL_H

/** LOG **
 *
 * $Log: url.h,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

