/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _STR_H
#define _STR_H

#include "types.h"
#include "tlist.h"

class CBuffer {
      public:
	CBuffer(void);
	 CBuffer(const CBuffer & other);
	 CBuffer(size_t len);
	 CBuffer(CPBYTE pbuf, size_t len);
	~CBuffer(void);

	 CBuffer & operator=(const CBuffer & other);
	BYTE operator[] (size_t n) const;
	 BYTE & operator[] (size_t n);

	void Clear(void);

	void Set(CPBYTE pbuf, size_t len);
	size_t GetSize(void) const;
	void SetSize(size_t len);
	PBYTE GetBuffer(void);
	CPBYTE GetBuffer(void) const;
	void SetBuffer(CPBYTE pbuf);

      protected:
	 size_t m_nAlloc;
	 size_t m_nLen;
	 PBYTE m_buf;
};

class CString {
      public:
	CString(void);
	 CString(const CString & other);
	 CString(CPCHAR sz);
	 CString(CPCHAR buf, size_t len);
	 CString(char c, UINT nrep = 1);
	~CString(void);

	 CString & operator=(const CString & other);
	 CString & operator=(CPCHAR sz);

	void Set(CPCHAR buf, size_t len);

	UINT GetLength(void) const;
	bool IsEmpty(void) const;
	int Compare(const CString & other) const;
	int CompareNoCase(const CString & other) const;

	char GetAt(UINT pos) const;
	char &GetAt(UINT pos);
	void SetAt(UINT pos, char c);
	void Append(CPCHAR sz);
	void ToLower(void);
	void ToUpper(void);
	void DeQuote(void);
	
	char * to_str();

	CPCHAR Find(char c, UINT pos = 0) const;

	inline operator  CPCHAR(void) const {
		assert(m_sz);
		return m_sz;
	}
      // XXXX I commented this operator declaration because it makes the program crash...// Without it, the rest is fine// inline char  operator[]( int pos )  const { return GetAt( pos ); } protected:
	 PCHAR m_sz;
};
typedef TDoubleList < CString > CStringList;

inline bool operator==(const CString & lhs, const CString & rhs)
{
	return (lhs.Compare(rhs) == 0);
}
inline bool operator==(const CString & lhs, CPCHAR rhs)
{
	return (lhs.Compare(rhs) == 0);
}
inline bool operator==(CPCHAR lhs, const CString & rhs)
{
	return (rhs.Compare(lhs) == 0);
}

inline bool operator!=(const CString & lhs, const CString & rhs)
{
	return (lhs.Compare(rhs) != 0);
}
inline bool operator!=(const CString & lhs, CPCHAR rhs)
{
	return (lhs.Compare(rhs) != 0);
}
inline bool operator!=(CPCHAR lhs, const CString & rhs)
{
	return (rhs.Compare(lhs) != 0);
}

inline bool operator<=(const CString & lhs, const CString & rhs)
{
	return (lhs.Compare(rhs) <= 0);
}
inline bool operator<=(const CString & lhs, CPCHAR rhs)
{
	return (lhs.Compare(rhs) <= 0);
}
inline bool operator<=(CPCHAR lhs, const CString & rhs)
{
	return (rhs.Compare(lhs) > 0);
}

inline bool operator>=(const CString & lhs, const CString & rhs)
{
	return (lhs.Compare(rhs) >= 0);
}
inline bool operator>=(const CString & lhs, CPCHAR rhs)
{
	return (lhs.Compare(rhs) >= 0);
}
inline bool operator>=(CPCHAR lhs, const CString & rhs)
{
	return (rhs.Compare(lhs) < 0);
}

inline bool operator<(const CString & lhs, const CString & rhs)
{
	return (lhs.Compare(rhs) < 0);
}
inline bool operator<(const CString & lhs, CPCHAR rhs)
{
	return (lhs.Compare(rhs) < 0);
}
inline bool operator<(CPCHAR lhs, const CString & rhs)
{
	return (rhs.Compare(lhs) >= 0);
}

inline bool operator>(const CString & lhs, const CString & rhs)
{
	return (lhs.Compare(rhs) > 0);
}
inline bool operator>(const CString & lhs, CPCHAR rhs)
{
	return (lhs.Compare(rhs) > 0);
}
inline bool operator>(CPCHAR lhs, const CString & rhs)
{
	return (rhs.Compare(lhs) <= 0);
}

#endif				//ndef _STR_H

/** LOG **
 *
 * $Log: str.h,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

