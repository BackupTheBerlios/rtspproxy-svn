/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#include "bitset.h"

#include "dbg.h"

#include <string.h>

/*
 *   n = ((n >>  1) & 0x55555555) | ((n <<  1) & 0xaaaaaaaa);
 *   n = ((n >>  2) & 0x33333333) | ((n <<  2) & 0xcccccccc);
 *   n = ((n >>  4) & 0x0f0f0f0f) | ((n <<  4) & 0xf0f0f0f0);
 *   n = ((n >>  8) & 0x00ff00ff) | ((n <<  8) & 0xff00ff00);
 *   n = ((n >> 16) & 0x0000ffff) | ((n << 16) & 0xffff0000);
 *
 *       -- C code which reverses the bits in a word.
 *
 * Everyone should have 'fortune' in their login script.  :-)
 */

// A large portion of this is copied from CBuffer

#define WORD_SIZE(bits) ( ((bits+31)/32)*4 )

/*****************************************************************************
 *
 * CBitSet
 *
 *****************************************************************************/

CBitSet::CBitSet(void):m_nAlloc(0), m_nBitLen(0), m_buf(NULL)
{
	// Empty
}

CBitSet::CBitSet(const CBitSet & other):m_nAlloc(0),
m_nBitLen(0), m_buf(NULL)
{
	assert(other.m_buf || (!other.m_nAlloc && !other.m_nBitLen));

	if (other.m_nBitLen) {
		m_nAlloc = WORD_SIZE(other.m_nBitLen);
		m_nBitLen = other.m_nBitLen;
		m_buf = new BYTE[m_nAlloc];
		memcpy(m_buf, other.m_buf, m_nAlloc);
	}
}

CBitSet::CBitSet(CPBYTE pbuf, size_t bitlen)
{
	assert(pbuf);

	m_nAlloc = WORD_SIZE(bitlen);
	m_nBitLen = bitlen;
	m_buf = new BYTE[m_nAlloc];
	memcpy(m_buf, pbuf, m_nAlloc);
}

CBitSet::~CBitSet(void)
{
	delete[]m_buf;
}

CBitSet & CBitSet::operator=(const CBitSet & other)
{
	assert(other.m_buf || (!other.m_nAlloc && !other.m_nBitLen));

	m_nAlloc = m_nBitLen = 0;
	delete[]m_buf;
	m_buf = NULL;
	if (other.m_nBitLen) {
		m_nAlloc = WORD_SIZE(other.m_nBitLen);
		m_nBitLen = other.m_nBitLen;
		m_buf = new BYTE[m_nAlloc];
		memcpy(m_buf, other.m_buf, m_nAlloc);
	}

	return *this;
}

size_t CBitSet::GetBitSize(void) const
{
	return m_nBitLen;
}

void CBitSet::SetBitSize(size_t bitlen)
{
	assert(m_buf || (!m_nAlloc && !m_nBitLen));

	if (bitlen) {
		size_t len = WORD_SIZE(bitlen);
		if (len > m_nAlloc) {
			BYTE *pbuf = new BYTE[len];
			memcpy(pbuf, m_buf, m_nAlloc);
			delete[]m_buf;
			m_nAlloc = len;
			m_buf = pbuf;
		}
		m_nBitLen = bitlen;
	} else {
		m_nAlloc = m_nBitLen = 0;
		delete[]m_buf;
		m_buf = NULL;
	}
}

void CBitSet::Set(CPBYTE pbuf, size_t bitlen)
{
	assert(pbuf || !bitlen);
	assert(m_buf || (!m_nAlloc && !m_nBitLen));

	if (bitlen) {
		size_t len = WORD_SIZE(bitlen);
		if (len > m_nAlloc) {
			delete[]m_buf;
			m_buf = new BYTE[len];
			m_nAlloc = len;
		}
		memcpy(m_buf, pbuf, len);
		m_nBitLen = bitlen;
	} else {
		m_nAlloc = m_nBitLen = 0;
		delete[]m_buf;
		m_buf = NULL;
	}
}

size_t CBitSet::GetSize(void) const
{
	return WORD_SIZE(m_nBitLen);
}

PBYTE CBitSet::GetBuffer(void)
{
	return m_buf;
}

CPBYTE CBitSet::GetBuffer(void) const
{
	return m_buf;
}

void CBitSet::ZeroBits(void)
{
	if (m_nAlloc) {
		assert_or_ret(m_buf);
		memset(m_buf, 0, m_nAlloc);
	}
}

void CBitSet::SetBit(size_t n)
{
	assert(m_buf && n < m_nBitLen);

	m_buf[n / 8] |= (1 << (n & 7));
}

void CBitSet::ClearBit(size_t n)
{
	assert(m_buf && n < m_nBitLen);

	m_buf[n / 8] &= ~(1 << (n & 7));
}

bool CBitSet::TestBit(size_t n)
{
	assert(m_buf && n < m_nBitLen);

	return ((m_buf[n / 8] & (1 << (n & 7))) != 0);
}

/*****************************************************************************
 *
 * CRevBitSet
 *
 *****************************************************************************/

void CRevBitSet::SetBit(size_t n)
{
	assert(m_buf && n < m_nBitLen);

	m_buf[n / 8] |= (1 << (7 - n & 7));
}

void CRevBitSet::ClearBit(size_t n)
{
	assert(m_buf && n < m_nBitLen);

	m_buf[n / 8] &= ~(1 << (7 - n & 7));
}

bool CRevBitSet::TestBit(size_t n)
{
	assert(m_buf && n < m_nBitLen);

	return ((m_buf[n / 8] & (1 << (7 - n & 7))) != 0);
}

/** LOG **
 *
 * $Log: bitset.cpp,v $
 * Revision 1.2  2003/11/17 16:14:02  mat
 * make-up
 *
 *
 */

