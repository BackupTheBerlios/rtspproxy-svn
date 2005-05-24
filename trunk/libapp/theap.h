/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _THEAP_H
#define _THEAP_H

template < class T > class THeap {
      private:			// Unimplemented
      public:
	THeap(UINT len) {
	}
	~THeap(void) {
	}

	T *GetRoot(void) {
		return m_pHeap[0];
	}
	void Insert(T * Elem) {
	}
	void Remove(T * Elem) {
	}

      protected:
	UINT m_nLen;
	T **m_pHeap;
};

#endif				//ndef _THEAP_H

/** LOG **
 *
 * $Log: theap.h,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

