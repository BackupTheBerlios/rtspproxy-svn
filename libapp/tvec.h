/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _TVEC_H
#define _TVEC_H

#include <assert.h>

template < class T > class TGrowingVector {
      public:
	TGrowingVector(void):m_len(0), m_vec(NULL) {
	}
	~TGrowingVector(void) {
		delete[]m_vec;
	}
	const T & operator[] (unsigned n) const {
		assert(m_len && n < m_len);
		return m_vec[n];
	} T operator[] (unsigned n) {
		if (m_len < n) {
			lenNew = m_len * 2;
			if (!lenNew)
				lenNew = DEF_VEC_SIZE;
			T *vecNew = new T[lenNew];
			assert(vecNew);
			if (m_len)
				memcpy(vecNew, m_vec, m_len * sizeof(T));
			memset(vecNew + m_len, 0,
			       (lenNew - m_len) * sizeof(T));
			m_len = lenNew;
			delete[]m_vec;
			m_vec = vecNew;
		}
		return m_vec[n];
	}
      protected:
	unsigned m_len;
	T *m_vec;
};

#endif				//ndef _TVEC_H

/** LOG **
 *
 * $Log: tvec.h,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

