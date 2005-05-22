/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#include <ctype.h>
#include <stdlib.h>
#include <string.h>
#include <types.h>

#include "types.h"
#include "str.h"

#include "dbg.h"

/**************************************
 *
 * CBuffer
 *
 **************************************/

CBuffer::CBuffer(void):
m_nAlloc(0), 
m_nLen(0), 
m_buf(NULL)
{
	// Empty
}

CBuffer::CBuffer(const CBuffer & other) : 
m_nAlloc(0), 
m_nLen(0), 
m_buf(NULL)
{
	assert(other.m_buf || (!other.m_nAlloc && !other.m_nLen));

	if (other.m_nLen) {
		m_nAlloc = m_nLen = other.m_nLen;
		m_buf = new BYTE[other.m_nLen];
		memcpy(m_buf, other.m_buf, m_nAlloc);
	}
}

CBuffer::CBuffer(size_t len)
{
	m_buf = new BYTE[len];
	m_nAlloc = m_nLen = len;
}

CBuffer::CBuffer(CPBYTE pbuf, size_t len)
{
	assert(pbuf);

	m_buf = new BYTE[len];
	m_nAlloc = m_nLen = len;
	memcpy(m_buf, pbuf, len);
}

CBuffer::~CBuffer(void)
{
	if (m_buf) {
		memset(m_buf, 0xDD, m_nAlloc);
		free( m_buf );
	}
}

CBuffer & CBuffer::operator=(const CBuffer & other)
{
	assert(other.m_buf || (!other.m_nAlloc && !other.m_nLen));

	m_nAlloc = m_nLen = 0;
	delete[]m_buf;
	m_buf = NULL;
	if (other.m_nLen) {
		m_nAlloc = m_nLen = other.m_nLen;
		m_buf = new BYTE[other.m_nLen];
		memcpy(m_buf, other.m_buf, m_nAlloc);
	}

	return *this;
}

BYTE CBuffer::operator[] (size_t n)
const {
	assert(m_buf);
	assert(n < m_nLen);

	return m_buf[n];
} BYTE & CBuffer::operator[] (size_t n)
{
	assert(m_buf);
	assert(n < m_nLen);

	return m_buf[n];
}

void CBuffer::Clear(void)
{
	m_nLen = m_nAlloc = 0;
	delete[]m_buf;
	m_buf = NULL;
}

void CBuffer::Set(CPBYTE pbuf, size_t len)
{
	assert(pbuf || !len);
	assert(m_buf || (!m_nAlloc && !m_nLen));

	if (len) {
		if (len > m_nAlloc) {
			if (m_buf) 
				free(m_buf);
		      
			m_buf = (u_int8_t *)malloc(len);
			m_nAlloc = len;
		}
		memcpy(m_buf, pbuf, len);
		m_nLen = len;
	} else {
		m_nAlloc = m_nLen = 0;
		if (m_buf)
			free( m_buf );
		m_buf = NULL;
	}
}

size_t CBuffer::GetSize(void) const
{
	return m_nLen;
}

void CBuffer::SetSize(size_t len)
{
	assert(m_buf || (!m_nAlloc && !m_nLen));

	if (len) {
		if (len > m_nAlloc) {
			char *pbuf = (char *) malloc( len );
			if ( m_buf ) {
				memcpy( pbuf, m_buf, m_nAlloc );
				free( m_buf );
			}
			m_nAlloc = len;
			m_buf = (uint8_t *)pbuf;
		}
		m_nLen = len;
	} else {
		m_nAlloc = m_nLen = 0;
		if (m_buf)
			free( m_buf );
		m_buf = NULL;
	}
}

PBYTE CBuffer::GetBuffer(void)
{
	return m_buf;
}

CPBYTE CBuffer::GetBuffer(void) const
{
	return m_buf;
}

void CBuffer::SetBuffer(CPBYTE pbuf)
{
	assert(pbuf);
	assert(m_buf && m_nAlloc && m_nLen);

	memcpy(m_buf, pbuf, m_nLen);
}

/**************************************
 *
 * CString
 *
 **************************************/

CString::CString(void)
{
	m_sz = new char[1];
	m_sz[0] = '\0';
}

CString::CString(const CString & other)
{
	assert(other.m_sz);
	m_sz = new char[strlen(other.m_sz) + 1];
	strcpy(m_sz, other.m_sz);
}

CString::CString(CPCHAR sz)
{
	m_sz = new char[strlen(sz) + 1];
	strcpy(m_sz, sz);
}

CString::CString(CPCHAR buf, size_t len)
{
	m_sz = new char[len + 1];
	memcpy(m_sz, buf, len);
	m_sz[len] = '\0';
}

CString::CString(char c, UINT nrep /* = 1 */ )
{
	m_sz = new char[nrep + 1];
	memset(m_sz, c, nrep);
	m_sz[nrep] = '\0';
}

CString::~CString(void)
{
	if (m_sz)
		memset(m_sz, 0xDD, strlen(m_sz) + 1);
	delete[]m_sz;
}

CString & CString::operator=(const CString & other)
{
	assert(other.m_sz);
	delete[]m_sz;
	m_sz = new char[strlen(other.m_sz) + 1];
	strcpy(m_sz, other.m_sz);

	return *this;
}

char * CString::to_str()
{
	return m_sz;
}

CString & CString::operator=(CPCHAR sz)
{
	assert(sz);

	delete[]m_sz;
	m_sz = new char[strlen(sz) + 1];
	strcpy(m_sz, sz);

	return *this;
}

void CString::Set(CPCHAR buf, size_t len)
{
	assert(buf);

	delete[]m_sz;
	m_sz = new char[len + 1];
	memcpy(m_sz, buf, len);
	m_sz[len] = '\0';
}

UINT CString::GetLength(void) const
{
	assert(m_sz);

	return strlen(m_sz);
}

bool CString::IsEmpty(void) const
{
	assert(m_sz);

	return (m_sz[0] == '\0');
}

int CString::Compare(const CString & other) const
{
	assert(m_sz && other.m_sz);

	return strcmp(m_sz, other.m_sz);
}

int CString::CompareNoCase(const CString & other) const
{
	assert(m_sz && other.m_sz);

	return strcasecmp(m_sz, other.m_sz);
}

char CString::GetAt(UINT pos) const
{
	assert(m_sz);
	assert(pos < strlen(m_sz));

	return m_sz[pos];
}

char &CString::GetAt(UINT pos)
{
	assert(m_sz);
	assert(pos < strlen(m_sz));

	return m_sz[pos];
}

void CString::SetAt(UINT pos, char c)
{
	assert(m_sz);
	assert(pos < strlen(m_sz));

	m_sz[pos] = c;
}

void CString::Append(CPCHAR sz)
{
	assert(sz && m_sz);

	if (*sz) {
		PCHAR sznew = new char[strlen(m_sz) + strlen(sz) + 1];
		strcpy(sznew, m_sz);
		strcat(sznew, sz);
		delete[]m_sz;
		m_sz = sznew;
	}
}

void CString::ToLower(void)
{
	assert_or_ret(m_sz);
	char *p = m_sz;
	while (*p) {
		*p = tolower(*p);
		p++;
	}
}

void CString::ToUpper(void)
{
	assert_or_ret(m_sz);
	char *p = m_sz;
	while (*p) {
		*p = toupper(*p);
		p++;
	}
}

void CString::DeQuote(void)
{
	assert_or_ret(m_sz);

	PCHAR p = m_sz;
	PCHAR q = m_sz;
	while (*p) {
		if (*p == '\\' && *(p + 1)) {
			switch (*(p + 1)) {
			case 'n':
				p++;
				*p = '\n';
				break;
			case 'r':
				p++;
				*p = '\r';
				break;
			case 't':
				p++;
				*p = '\t';
				break;
			case '"':
				p++;
				*p = '"';
				break;
			case '\\':
				p++;
				break;
			default:
				break;
			}
		}
		*q++ = *p++;
	}
	*q = '\0';
}

CPCHAR CString::Find(char c, UINT pos /* = 0 */ ) const
{
	assert_or_retv(NULL, m_sz);
	assert_or_retv(NULL, pos < strlen(m_sz));

	return strchr(m_sz + pos, c);
}

/** LOG **
 *
 * $Log: str.cpp,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

