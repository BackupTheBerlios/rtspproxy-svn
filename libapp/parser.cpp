/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#include "parser.h"
#include "file.h"

#include "dbg.h"

#include <ctype.h>

CParser::CParser(void)
{
	// Empty
}

CParser::~CParser(void)
{
	// Empty
}

/**************************************
 *
 * CConfigParser
 *
 **************************************/

CConfigParser::CConfigParser(void):m_pos(0)
{
	// Empty
}

CConfigParser::CConfigParser(const CString & strFile):m_pos(0)
{
	Open(strFile);
}

CConfigParser::~CConfigParser(void)
{
	// Empty
}

void CConfigParser::Open(const CString & strFile)
{
	Close();

	CFile f;
	if (f.Open(strFile)) {
		struct stat st;
		if (f.Stat(&st) && st.st_size > 0) {
			size_t len = st.st_size;
			m_buf.SetSize(len);
			if (len != f.Read(m_buf.GetBuffer(), len)) {
				dbgout("Failed to read %s",
				       (CPCHAR) strFile);
				m_buf.Clear();
			}
		}
		f.Close();
	}
}

void CConfigParser::Close(void)
{
	m_buf.Clear();
	m_pos = 0;
}

CToken CConfigParser::NextToken(void)
{
	CToken tok;
	tok.type = CToken::TOK_EOF;
	CPCHAR p = (CPCHAR) m_buf.GetBuffer() + m_pos;

	while (m_pos < m_buf.GetSize() && isspace(*p)) {
		if ('\n' == *p || '\r' == *p) {
			tok.type = CToken::TOK_EOL;
			while (m_pos < m_buf.GetSize()
			       && ('\n' == *p || '\r' == *p)) {
				m_pos++;
				p++;
			}
			return tok;
		}
		m_pos++;
		p++;
	}
	if (m_pos < m_buf.GetSize()) {
		tok.type = CToken::TOK_STRING;
		CPCHAR q = p;
		while (m_pos < m_buf.GetSize() && !isspace(*q)) {
			m_pos++;
			q++;
		}
		tok.val.Set(p, q - p);
	}

	return tok;
}

void CConfigParser::NextLine(void)
{
	CPCHAR p = (CPCHAR) m_buf.GetBuffer() + m_pos;
	while (m_pos < m_buf.GetSize() && *p != '\n') {
		m_pos++;
		p++;
	}
	while (m_pos < m_buf.GetSize() && (*p == '\n' || *p == '\r')) {
		m_pos++;
		p++;
	}
}

/** LOG **
 *
 * $Log: parser.cpp,v $
 * Revision 1.2  2003/11/17 16:14:02  mat
 * make-up
 *
 *
 */

