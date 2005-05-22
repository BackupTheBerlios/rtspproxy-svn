/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _AVL_H
#define _AVL_H

enum cmp_t { 
	CMP_LT = -1, 
	CMP_EQ = 0, 
	CMP_GT = 1 
};

enum dir_t { 
	LEFT = 0, 
	RIGHT = 1 
};

static dir_t Opposite(dir_t dir)
{
	return dir_t(1 - int (dir));
}

// AvlNode -- Class to implement an AVL Tree
//
template < class KeyType > class AvlNode {
      private:			// Unimplemented
	AvlNode(const AvlNode < KeyType > &);
	AvlNode & operator=(const AvlNode < KeyType > &);

      public:
//    AvlNode( void ) : m_bal(0) { Reset(); }
	AvlNode(const KeyType & key):m_Data(key), m_bal(0) {
		Reset();
	}
	virtual ~ AvlNode(void) {
		delete m_tree[LEFT];
		delete m_tree[RIGHT];
	}

	const KeyType & Key(void) const {
		return m_Data;
	} KeyType & Key(void) {
		return m_Data;
	}
	short Bal(void) const {
		return m_bal;
	} AvlNode *Subtree(dir_t dir) const {
		return m_tree[dir];
	} UINT Height(void) const {
		UINT lh = (m_tree[LEFT]) ? m_tree[LEFT]->Height() : 0;
		UINT rh = (m_tree[RIGHT]) ? m_tree[RIGHT]->Height() : 0;
		 return (1 + max(lh, rh));
	}
	// Look for the given key, return NULL if not found,
        // otherwise return the item's address. 
	static KeyType * Search(const KeyType & key, AvlNode < KeyType > *root) {
		cmp_t res;
		while (root && (res = root->Compare(key))) {
			root = root->m_tree[(res < 0) ? LEFT : RIGHT];
		}
		return (root ? &root->m_Data : NULL);
	}

	// Insert the given key, return NULL if it was inserted,
	// otherwise return the existing item with the same key.
	static KeyType *Insert(const KeyType & item,
			       AvlNode < KeyType > *&root) {
		int change;
		return Insert(item, root, change);
	}

	// Delete the given key from the tree. Return the corresponding
	// node, or return NULL if it was not found.
	static KeyType *Delete(const KeyType & key,
			       AvlNode < KeyType > *&root, cmp_t cmp =
			       CMP_EQ) {
		int change;
		return Delete(key, root, change);
	}

      private:
	KeyType m_Data;
	AvlNode < KeyType > *m_tree[2];
	short m_bal;

	void Reset(void) {
		m_bal = 0;
		m_tree[LEFT] = m_tree[RIGHT] = NULL;
	}

	// ----- Routines that do the *real* insertion/deletion


	// Insert the given key into the given tree. Return the node if
	// it already exists. Otherwise return NULL to indicate that
	// the key was successfully inserted.  Upon return, the "change"
	// parameter will be '1' if the tree height changed as a result
	// of the insertion (otherwise "change" will be 0).
	static KeyType *Insert(const KeyType & item,
			       AvlNode < KeyType > *&root, int &change);

	// Delete the given key from the given tree. Return NULL if the
	// key is not found in the tree. Otherwise return a pointer to the
	// node that was removed from the tree.  Upon return, the "change"
	// parameter will be '1' if the tree height changed as a result
	// of the deletion (otherwise "change" will be 0).
	static KeyType *Delete(const KeyType & key,
			       AvlNode < KeyType > *&root, int &change,
			       cmp_t cmp = CMP_EQ);

	// Routines for rebalancing and rotating subtrees
	static int RotateOnce(AvlNode < KeyType > *&root, dir_t dir);

	static int RotateTwice(AvlNode < KeyType > *&root, dir_t dir);

	static int ReBalance(AvlNode < KeyType > *&root);

	cmp_t Compare(const KeyType & key, cmp_t cmp = CMP_EQ) const;
};

template < class KeyType > class AvlTree {
      private:			// Unimplemented
	AvlTree(const AvlTree < KeyType > &);
	AvlTree & operator=(const AvlTree < KeyType > &);

      public:
	AvlTree(void):m_root(NULL) {
	};
	~AvlTree(void) {
		delete m_root;
	}

	bool IsEmpty(void) const {
		return (m_root == NULL);
	} AvlNode < KeyType > *GetRoot(void) {
		return m_root;
	}

	KeyType *Search(const KeyType & key) const {
		return AvlNode < KeyType >::Search(key, m_root);
	} KeyType *Insert(const KeyType & item) {
		return AvlNode < KeyType >::Insert(item, m_root);
	}
	KeyType *Delete(const KeyType & key) {
		return AvlNode < KeyType >::Delete(key, m_root);
	}

      private:
	AvlNode < KeyType > *m_root;
};

/*** START OF Avl.cc ***/

// ---------------------------------------------------------------- Definitions
#ifndef MIN
inline static int MIN(int a, int b)
{
	return (a < b) ? a : b;
}
#endif
#ifndef MAX
inline static int MAX(int a, int b)
{
	return (a > b) ? a : b;
}
#endif

// Use mnemonic constants for valid balance-factor values
enum balance_t { LEFT_HEAVY = -1, BALANCED = 0, RIGHT_HEAVY = 1 };

// Use mnemonic constants for indicating a change in height
enum height_effect_t { HEIGHT_NOCHANGE = 0, HEIGHT_CHANGE = 1 };

// Return true if the tree is too heavy on the left side
inline static int LEFT_IMBALANCE(short bal)
{
	return (bal < LEFT_HEAVY);
}

// Return true if the tree is too heavy on the right side
inline static int RIGHT_IMBALANCE(short bal)
{
	return (bal > RIGHT_HEAVY);
}

// ----------------------------------------------- Constructors and Destructors
// ------------------------------------------------- Rotating and Re-Balancing

template < class KeyType >
    int
    AvlNode < KeyType >::RotateOnce(AvlNode < KeyType > *&root, dir_t dir)
{
	dir_t otherDir = Opposite(dir);
	AvlNode < KeyType > *oldRoot = root;

	// See if otherDir subtree is balanced. If it is, then this
	// rotation will *not* change the overall tree height.
	// Otherwise, this rotation will shorten the tree height.
	int heightChange = (root->m_tree[otherDir]->m_bal == 0)
	    ? HEIGHT_NOCHANGE : HEIGHT_CHANGE;

	// assign new root
	root = oldRoot->m_tree[otherDir];

	// new-root exchanges it's "dir" m_tree for it's parent
	oldRoot->m_tree[otherDir] = root->m_tree[dir];
	root->m_tree[dir] = oldRoot;

	// update balances
	oldRoot->m_bal =
	    -((dir == LEFT) ? --(root->m_bal) : ++(root->m_bal));

	return heightChange;
}

template < class KeyType >
    int
    AvlNode < KeyType >::RotateTwice(AvlNode < KeyType > *&root, dir_t dir)
{
	dir_t otherDir = Opposite(dir);
	AvlNode < KeyType > *oldRoot = root;
	AvlNode < KeyType > *oldOtherDirSubtree = root->m_tree[otherDir];

	// assign new root
	root = oldRoot->m_tree[otherDir]->m_tree[dir];

	// new-root exchanges it's "dir" m_tree for it's grandparent
	oldRoot->m_tree[otherDir] = root->m_tree[dir];
	root->m_tree[dir] = oldRoot;

	// new-root exchanges it's "other-dir" m_tree for it's parent
	oldOtherDirSubtree->m_tree[dir] = root->m_tree[otherDir];
	root->m_tree[otherDir] = oldOtherDirSubtree;

	// update balances
	root->m_tree[LEFT]->m_bal = -MAX(root->m_bal, 0);
	root->m_tree[RIGHT]->m_bal = -MIN(root->m_bal, 0);
	root->m_bal = 0;

	// A double rotation always shortens the overall height of the tree
	return HEIGHT_CHANGE;
}

template < class KeyType >
    int AvlNode < KeyType >::ReBalance(AvlNode < KeyType > *&root)
{
	int heightChange = HEIGHT_NOCHANGE;

	if (LEFT_IMBALANCE(root->m_bal)) {
		// Need a right rotation
		if (root->m_tree[LEFT]->m_bal == RIGHT_HEAVY) {
			// RL rotation needed
			heightChange = RotateTwice(root, RIGHT);
		} else {
			// RR rotation needed
			heightChange = RotateOnce(root, RIGHT);
		}
	} else if (RIGHT_IMBALANCE(root->m_bal)) {
		// Need a left rotation
		if (root->m_tree[RIGHT]->m_bal == LEFT_HEAVY) {
			// LR rotation needed
			heightChange = RotateTwice(root, LEFT);
		} else {
			// LL rotation needed
			heightChange = RotateOnce(root, LEFT);
		}
	}

	return heightChange;
}

// ------------------------------------------------------- Comparisons

template < class KeyType >
    cmp_t
    AvlNode < KeyType >::Compare(const KeyType & key, cmp_t cmp) const
{
	cmp_t res = CMP_EQ;

	switch (cmp) {
	case CMP_EQ:
		res =
		    (key == m_Data) ? CMP_EQ : (key <
						m_Data) ? CMP_LT : CMP_GT;
		break;
	case CMP_LT:
		res = (m_tree[LEFT] == NULL) ? CMP_EQ : CMP_LT;
		break;
	case CMP_GT:
		res = (m_tree[RIGHT] == NULL) ? CMP_EQ : CMP_GT;
		break;
	default:
		break;
	}

	return res;
}

// ------------------------------------------------------- Search/Insert/Delete

template < class KeyType >
    KeyType *
    AvlNode < KeyType >::Insert(const KeyType & item,
				AvlNode < KeyType > *&root, int &change)
{
	if (root == NULL) {
		root = new AvlNode < KeyType > (item);
		change = HEIGHT_CHANGE;
		return NULL;
	}

	KeyType *found = NULL;
	int increase = 0;

	// Compare items and determine which direction to search
	cmp_t result = root->Compare(item);
	dir_t dir = (result == CMP_LT) ? LEFT : RIGHT;

	if (result != CMP_EQ) {
		found = Insert(item, root->m_tree[dir], change);
		if (found)
			return found;	// already here - dont insert
		increase = result * change;	// set balance factor increment
	} else {
		increase = HEIGHT_NOCHANGE;
		return &root->m_Data;
	}

	root->m_bal += increase;	// update balance factor 

	change = (increase && root->m_bal) ? (1 - ReBalance(root)) :
	    HEIGHT_NOCHANGE;

	return NULL;
}

template < class KeyType >
    KeyType *
    AvlNode < KeyType >::Delete(const KeyType & key,
				AvlNode < KeyType > *&root, int &change,
				cmp_t cmp)
{
	if (root == NULL) {
		change = HEIGHT_NOCHANGE;
		return NULL;
	}

	KeyType *found = NULL;
	int decrease = 0;

	cmp_t result = root->Compare(key, cmp);
	dir_t dir = (result == CMP_LT) ? LEFT : RIGHT;

	if (result != CMP_EQ) {
		found = Delete(key, root->m_tree[dir], change, cmp);
		if (!found)
			return found;	// not found - can't delete
		decrease = result * change;	// set balance factor decrement
	} else {
		found = &root->m_Data;

		// -------------------------------------------------------------------
		// At this point we know "result" is zero and "root" points to
		// the node that we need to delete.  There are three cases:
		//
		//    1) The node is a leaf.  Remove it and return.
		//
		//    2) The node is a branch (has only 1 child). Make "root"
		//       (the pointer to this node) point to the child.
		//
		//    3) The node has two children. We swap items with the successor
		//       of "root" (the smallest item in its right subtree) and delete
		//       the successor from the right subtree of "root".  The
		//       identifier "decrease" should be reset if the subtree height
		//       decreased due to the deletion of the successor of "root".
		// -------------------------------------------------------------------

		if ((root->m_tree[LEFT] == NULL) &&
		    (root->m_tree[RIGHT] == NULL)) {
			// We have a leaf -- remove it
			delete root;
			root = NULL;
			change = HEIGHT_CHANGE;	// height changed from 1 to 0
			return found;
		} else if ((root->m_tree[LEFT] == NULL) ||
			   (root->m_tree[RIGHT] == NULL)) {
			// We have one child -- only child becomes new root 
			AvlNode < KeyType > *toDelete = root;
			root =
			    root->
			    m_tree[(root->m_tree[RIGHT]) ? RIGHT : LEFT];
			change = HEIGHT_CHANGE;	// We just shortened the subtree
			// Null-out the subtree pointers so we dont recursively delete
			toDelete->m_tree[LEFT] = toDelete->m_tree[RIGHT] =
			    NULL;
			delete toDelete;
			return found;
		} else {
			// We have two children -- find successor and replace our current
			// data item with that of the successor
			//XXXTDM: we shouldn't be copying data items
			root->m_Data =
			    *Delete(key, root->m_tree[RIGHT], decrease,
				    CMP_LT);
		}
	}

	root->m_bal -= decrease;

	// ------------------------------------------------------------------------
	// Rebalance if necessary -- the height of current tree changes if one
	// of two things happens: (1) a rotation was performed which changed
	// the height of the subtree (2) the subtree height decreased and now
	// matches the height of its other subtree (so the current tree now
	// has a zero balance when it previously did not).
	// ------------------------------------------------------------------------
	//change = (decrease) ? ((root->m_bal) ? balance(root) : HEIGHT_CHANGE)
	//                    : HEIGHT_NOCHANGE ;
	if (decrease) {
		if (root->m_bal) {
			change = ReBalance(root);
		} else {
			change = HEIGHT_CHANGE;
		}
	} else {
		change = HEIGHT_NOCHANGE;
	}

	return found;
}

#endif				//ndef _AVL_H

/** LOG **
 *
 * $Log: Avl.h,v $
 * Revision 1.2  2003/11/17 16:14:02  mat
 * make-up
 *
 *
 */

