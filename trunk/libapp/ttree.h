/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _TTREE_H
#define _TTREE_H

#include <assert.h>

#if 0

enum BalFactor { 
	bfLEFT2 = -2, 
	bfLEFT = -1, 
	bfBAL = 0, 
	bfRIGHT = 1, 
	bfRIGHT2 = 2 
};

#define LEFT  0
#define RIGHT 1
#define OPPOSITE(d) (1-d)

template < class T > class TAvlTree {
      private:
	TAvlTree(const TAvlTree &);
	TAvlTree & operator=(const TAvlTree &);

      protected:
	class TAvlNode {
	      public:
		TAvlNode(const T & Data):m_pNext(pNext), m_Data(Data) {
			// Empty
		}
		T & GetData(void) {
			return m_Data;
		}
		const T & GetData(void) const {
			return m_Data;
		} TAvlNode m_kids[2];
		BalFactor m_bal;
		T m_Data;
	};
      public:
      TAvlTree(void):m_pRoot(NULL), m_nCount(0) {
	}
	~TAvlTree(void) {
	}

	bool IsEmpty(void) const {
		return m_pRoot == NULL;
	} unsigned int GetCount(void) const {
		return m_nCount;
	} bool Insert(const T & Data) {
		TNode *pNode = new TNode(Data);
		assert(pNode);
		if (!m_pRoot) {
			assert(0 == m_nCount);
			m_pRoot = pNode;
			m_nCount++;
			return true;
		}
		return _Insert(pNode, m_pRoot);
	}

      protected:
	bool _Insert(TNode * pNode, TNode * pRoot) {
		bool ret = false;
		if (!(pNode->m_Data == pRoot->m_Data)) {
			if (pNode->m_Data < pRoot->m_Data) {
				if (pRoot->m_pLeft) {
					ret =
					    _Insert(pNode, pRoot->m_pLeft);
				} else {
					pRoot->m_pLeft = pNode;
					ret = true;
				}
			} else {
				if (pRoot->m_pRight) {
					ret =
					    _Insert(pNode,
						    pRoot->m_pRight);
				} else {
					pRoot->m_pRight = pNode;
					ret = true;
				}
			}
		}
		return ret;
	}
	bool _Remove(TNode * pNode, TNode * pRoot) {
		if (pNode->m_Data < pRoot->m_Data) {
		} else {
		}
	}


	bool RotateOnce(TNode * &rpRoot, int d) {
		int od = OPPOSITE(d);
		TNode *pOldRoot = rpRoot;
		bool bHeightChanged = (rpRoot->m_kids[od]->m_bal != 0);

		rpRoot = pOldRoot->m_kids[od];
		pOldRoot->m_kids[od] = rpRoot->m_kids[d];
		rpRoot->m_kids[d] = pOldRoot;

		rpRoot->m_bal += 2 * d - 1;
		pOldRoot->m_bal = -(rpRoot->mbal);

		return bHeightChanged;
	}

	bool RotateTwice(TNode * &rpRoot, int d) {
		int od = OPPOSITE(d);
		TNode *pOldRoot = rpRoot;
		TNode *pOldOther = rpRoot->m_kids[od];

		rpRoot = pOldRoot->m_kids[od]->m_kids[d];

		pOldRoot->m_kids[od] = rpRoot->m_kids[d];
		rpRoot->m_kids[d] = pOldRoot;

		pOldOther->m_kids[d] = rpRoot->m_kids[od];
		rpRoot->m_kids[od] = pOldOther;

		rpRoot->m_kids[LEFT]->m_bal = -max(rpRoot->m_bal, 0);
		rpRoot->m_kids[RIGHT]->m_bal = -min(rpRoot->m_bal, 0);
		rpRoot->m_bal = 0;

		return true;
	}

	bool Rebalance(TNode * &rpRoot) {
		bool bHeightChanged = false;
		if (rpRoot->m_bal == bfLEFT2) {
			if (rpRoot->m_kids[LEFT]->m_bal == bfRIGHT)
				bHeightChanged =
				    RotateTwice(rpRoot, RIGHT);
			else
				bHeightChanged = RotateOnce(rpRoot, RIGHT);
		} else if (rpRoot->m_bal == bfRIGHT2) {
			if (rpRoot->m_kids[RIGHT]->m_bal == bfLEFT)
				bHeightChanged = RotateTwice(rpRoot, LEFT);
			else
				bHeightChanged = RotateOnce(rpRoot, LEFT);
		}
		return bHeightChanged;
	}

	TNode *Insert(T * pData, TNode * &rpRoot, bool & rchanged) {
		if (!rpRoot) {
			rpRoot = new TNode(*pData);
			rchanged = true;
			return NULL;
		}

		T *found = NULL;
		int increase = 0;

		// Compare items and determine which direction to search
		cmp_t result = root->Compare(item->Key());
		dir_t dir = (result == MIN_CMP) ? LEFT : RIGHT;

		if (result != EQ_CMP) {
			// Insert into "dir" subtree 
			found = Insert(item, root->mySubtree[dir], change);
			if (found)
				return found;	// already here - dont insert
			increase = result * change;	// set balance factor increment
		} else {	// key already in tree at this node
			increase = HEIGHT_NOCHANGE;
			return root->myData;
		}

		rpRoot->m_bal += increase;
		rchanged = (increase
			    && rpRoot->m_bal) ? !Rebalance(rpRoot) : false;

		return NULL;
	}

      protected:
	TNode * m_pRoot;
	unsigned int m_nCount;
};

#else

#include "Avl.h"

#endif

#endif				//ndef _TTREE_H

/** LOG **
 *
 * $Log: ttree.h,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

