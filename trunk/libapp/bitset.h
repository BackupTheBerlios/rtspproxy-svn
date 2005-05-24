/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _BITSET_H
#define _BITSET_H

#include "types.h"
#include "tlist.h"
#include "str.h"

/*
 * Simple bitset class.  Bits are numbered in ascending order such that
 * bit N is at byte N/8, value 2^(N%8).
 *
 *   Example: L=14, { 0x04, 0x02 } = 00000100 xx000010 = ( 2, 9 )
 *                                   |      |   |    |
 *                                   7      0  13    8
 */
class CBitSet {
      public:
	CBitSet(void);
	 CBitSet(const CBitSet & other);
	 CBitSet(CPBYTE pbuf, size_t bitlen);
	~CBitSet(void);

	 CBitSet & operator=(const CBitSet & other);

	// Bit size operations
	size_t GetBitSize(void) const;
	void SetBitSize(size_t bitlen);
	void Set(CPBYTE pbuf, size_t bitlen);

	// Byte size operations
	size_t GetSize(void) const;
	PBYTE GetBuffer(void);
	CPBYTE GetBuffer(void) const;

	// Bit manipulation
	void ZeroBits(void);
	void SetBit(size_t n);
	void ClearBit(size_t n);
	bool TestBit(size_t n);

      protected:
	 size_t m_nAlloc;
	size_t m_nBitLen;
	PBYTE m_buf;
};
typedef TDoubleList < CBitSet * >CBitSetList;

/*
 * Reversed bitset class.  Bytes are numbered in ascending order but the bit
 * positions are reversed such that bit N is at byte N/8, value 2^(7-N%8).
 *
 *   Example: L=14, { 0x20, 0x40 } = 00100000 010000xx = ( 2, 9 )
 *                                   |      | |    |
 *                                   0      7 8   13
 */
class CRevBitSet:public CBitSet {
      public:
	// Bit manipulation
	void SetBit(size_t n);
	void ClearBit(size_t n);
	bool TestBit(size_t n);
};
typedef TDoubleList < CRevBitSet * >CRevBitSetList;

#endif				//ndef _BITSET_H

/** LOG **
 *
 * $Log: bitset.h,v $
 * Revision 1.2  2003/11/17 16:14:02  mat
 * make-up
 *
 *
 */

