/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _VARIANT_H
#define _VARIANT_H

#include "types.h"
#include "tlist.h"

class CVariant {
      public:
	enum Type { VT_NONE, VT_BOOL, VT_INT32, VT_UINT32, VT_FLOAT,
		    VT_STRING };

      public:
	 CVariant(void);
	 CVariant(Type t);
	 CVariant(const CVariant & other);
	~CVariant(void);

	 CVariant & operator=(const CVariant & other);

	void Destroy(void);
	Type GetType(void);
	void SetType(Type t);

	bool GetBool(void) const;
	void SetBool(bool b);
	INT32 GetInt(void) const;
	void SetInt(INT32 i);
	UINT32 GetUint(void) const;
	void SetUint(UINT32 u);
	float GetFloat(void) const;
	void SetFloat(float f);
	CPCHAR GetString(void) const;
	void SetString(CPCHAR s);

	bool operator==(const CVariant & other) const;
	bool operator!=(const CVariant & other) const;
	bool operator<=(const CVariant & other) const;
	bool operator>=(const CVariant & other) const;
	bool operator<(const CVariant & other) const;
	bool operator>(const CVariant & other) const;

      protected:
	 Type m_type;
	union {
		bool bval;
		INT32 ival;
		UINT32 uval;
		float fval;
		PCHAR sval;
	} m_val;
};
typedef TDoubleList < CVariant > CVariantList;

#endif				//ndef _VARIANT_H

/** LOG **
 *
 * $Log: variant.h,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

