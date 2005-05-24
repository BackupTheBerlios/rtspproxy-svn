/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _VALMAP_H
#define _VALMAP_H

#include "types.h"
#include "str.h"
#include "variant.h"
#include "Avl.h"

class CKeyValPair {
      public:
	CKeyValPair(void);
	 CKeyValPair(const CKeyValPair & other);
	 CKeyValPair(const CString & strKey);
	 CKeyValPair(const CString & strKey, const CVariant & varVal);
	~CKeyValPair(void);

	 CKeyValPair & operator=(const CKeyValPair & other);

	const CString & GetKey(void);
	const CVariant & GetVal(void);
	void SetVal(const CVariant & varVal);

	bool operator==(const CKeyValPair & other) const;
	bool operator<(const CKeyValPair & other) const;
	bool operator>(const CKeyValPair & other) const;

      protected:
	 CString m_strKey;
	CVariant m_varVal;
};
typedef AvlTree < CKeyValPair > CKeyValPairTree;

class CValueMap {
      private:			// Unimplemented
	CValueMap(const CValueMap &);
	 CValueMap & operator=(const CValueMap &);

      public:
	 CValueMap(bool bCaseSensitive = false);
	~CValueMap(void);

	bool Lookup(const CString & strKey, CVariant * pvarVal) const;
	CVariant Lookup(const CString & strKey) const;
	void SetAt(const CString & strKey, const CVariant & varVal);

      protected:
	 bool m_bCaseSensitive;
	CKeyValPairTree m_map;
};

#endif				//ndef _VALMAP_H

/** LOG **
 *
 * $Log: valmap.h,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

