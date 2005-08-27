/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#include <ctype.h>

#include "valmap.h"

/*****************************************************************************
 *
 * CKeyValPair
 *
 *****************************************************************************/

CKeyValPair::CKeyValPair(void)
{
	// Empty
}

CKeyValPair::CKeyValPair(const CKeyValPair & other):m_strKey(other.
							     m_strKey),
m_varVal(other.m_varVal)
{
	// Empty
}

CKeyValPair::CKeyValPair(const CString & strKey):m_strKey(strKey)
{
	// Empty
}

CKeyValPair::CKeyValPair(const CString & strKey,
			 const CVariant & varVal):m_strKey(strKey),
m_varVal(varVal)
{
	// Empty
}

CKeyValPair::~CKeyValPair(void)
{
	// Empty
}

CKeyValPair & CKeyValPair::operator=(const CKeyValPair & other)
{
	m_strKey = other.m_strKey;
	m_varVal = other.m_varVal;
	return *this;
}

const CString & CKeyValPair::GetKey(void)
{
	return m_strKey;
}

const CVariant & CKeyValPair::GetVal(void)
{
	return m_varVal;
}

void CKeyValPair::SetVal(const CVariant & varVal)
{
	m_varVal = varVal;
}

bool CKeyValPair::operator==(const CKeyValPair & other) const
{
	return (m_strKey == other.m_strKey);
}

bool CKeyValPair::operator<(const CKeyValPair & other) const
{
	return (m_strKey < other.m_strKey);
}

bool CKeyValPair::operator>(const CKeyValPair & other) const
{
	return (m_strKey > other.m_strKey);
}

/*****************************************************************************
 *
 * CValueMap
 *
 *****************************************************************************/

CValueMap::CValueMap(bool bCaseSensitive /* = false */ ):
m_bCaseSensitive(bCaseSensitive)
{
	// Empty
}

CValueMap::~CValueMap(void)
{
	// Empty
}

bool CValueMap::Lookup(const CString & strKey, CVariant * pvarVal) const
{
	bool found = false;

	CString strRealKey = strKey;
	if (!m_bCaseSensitive) {
		strRealKey.ToLower();
	}

	CKeyValPair *pResult = m_map.Search(CKeyValPair(strRealKey));
	if (pResult) {
		*pvarVal = pResult->GetVal();
		found = true;
	}

	return found;
}

CVariant CValueMap::Lookup(const CString & strKey) const
{
	CVariant var;
	Lookup(strKey, &var);
	return var;
}

void CValueMap::SetAt(const CString & strKey, const CVariant & varVal)
{
	CString strRealKey = strKey;
	if (!m_bCaseSensitive) {
		strRealKey.ToLower();
	}
	CKeyValPair kvp(strRealKey, varVal);

	m_map.Delete(kvp);
	m_map.Insert(kvp);
}

/** LOG **
 *
 * $Log: valmap.cpp,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

