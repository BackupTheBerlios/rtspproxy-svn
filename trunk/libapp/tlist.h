/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _TLIST_H
#define _TLIST_H

#include <assert.h>

template <class T>
class TSingleList
{
private: // Unimplemented
    TSingleList( const TSingleList& );
    TSingleList& operator=( const TSingleList& );

protected:
    class TSingleNode
    {
    public:
        TSingleNode( TSingleNode* pNext, const T& Data ) :
            m_pNext(pNext),
            m_Data(Data)
        {
            // Empty
        }
        T& GetData( void ) { return m_Data; }
        const T& GetData( void ) const { return m_Data; }

        TSingleNode* m_pNext;
        T            m_Data;
    };
    class IteratorBase
    {
        friend class TSingleList<T>;
    public:
        IteratorBase( void ) :
            m_pList(NULL),
            m_pNode(NULL)
        {
            // Empty
        }
        bool operator++( void )
        {
            assert( m_pList && m_pNode );
            m_pNode = m_pNode->m_pNext;
            return ( m_pNode != NULL );
        }
        bool operator++( int )
        {
            assert( m_pList && m_pNode );
            m_pNode = m_pNode->m_pNext;
            return ( m_pNode != NULL );
        }
        operator bool( void ) const
        {
            return ( m_pList != NULL && m_pNode != NULL );
        }
        bool operator==( const IteratorBase& itr ) const
        {
            return ( itr.m_pList == m_pList && itr.m_pNode == m_pNode );
        }
        bool operator!=( const IteratorBase& itr ) const
        {
            return ( itr.m_pList != m_pList || itr.m_pNode != m_pNode );
        }

    protected:
        IteratorBase( const TSingleList<T>* pList, TSingleNode* pNode ) :
             m_pList(pList),
             m_pNode(pNode)
        {
            // Empty
        }

    protected:
        const TSingleList<T>* m_pList;
        TSingleNode*          m_pNode;
    };
public:
    class Iterator : public IteratorBase
    {
        friend class TSingleList<T>;
    public:
        Iterator( void ) : IteratorBase()
        {
            // Empty
        }
        T& operator*( void )
        {
            assert( this->m_pList && this->m_pNode );
            return this->m_pNode->GetData();
        }
    protected:
        Iterator( TSingleList<T>* pList, TSingleNode* pNode ) :
            IteratorBase( pList, pNode )
        {
            // Empty
        }
    };
    class ConstIterator : public IteratorBase
    {
        friend class TSingleList<T>;
    public:
        ConstIterator( void ) : IteratorBase()
        {
            // Empty
        }
        const T& operator*( void ) const
        {
            assert( this->m_pList && this->m_pNode );
            return this->m_pNode->GetData();
        }
    protected:
        ConstIterator( const TSingleList<T>* pList, TSingleNode* pNode ) :
            IteratorBase( pList, pNode )
        {
            // Empty
        }
    };

    TSingleList( void ) : m_pHead(NULL), m_pTail(NULL), m_nCount(0) {}
    ~TSingleList( void )
    {
        while( ! IsEmpty() )
        {
            RemoveHead();
        }
    }

    bool IsEmpty( void ) const { return m_pHead == NULL; }
    unsigned int GetCount( void ) const { return m_nCount; }
    void InsertHead( const T& Data )
    {
        TSingleNode* pNode = new TSingleNode( m_pHead, Data );
        assert( pNode );
        m_pHead = pNode;
        if( ! m_pTail )
        {
            m_pTail = pNode;
        }
        m_nCount++;
    }
    void InsertTail( const T& Data )
    {
        TSingleNode* pNode = new TSingleNode( NULL, Data );
        assert( pNode );
        if( m_pTail )
        {
            m_pTail->m_pNext = pNode;
        }
        m_pTail = pNode;
        if( ! m_pHead )
        {
            m_pHead = pNode;
        }
        m_nCount++;
    }
    T RemoveHead( void )
    {
        TSingleNode* pNode = m_pHead;
        m_pHead = pNode->m_pNext;
        if( ! m_pHead )
        {
            m_pTail = NULL;
        }
        T Data = pNode->GetData();
        delete pNode;
        m_nCount--;
        return Data;
    }

    Iterator      Begin( void )       { return Iterator( this, m_pHead ); }
    ConstIterator Begin( void ) const { return ConstIterator( this, m_pHead ); }
    Iterator      End( void )       { return Iterator( this, NULL ); }
    ConstIterator End( void ) const { return ConstIterator( this, NULL ); }

protected:
    TSingleNode* m_pHead;
    TSingleNode* m_pTail;
    unsigned int m_nCount;
};

template <class T>
class TDoubleList
{
private: // Unimplemented
    TDoubleList( const TDoubleList& );
    TDoubleList& operator=( const TDoubleList& );

protected:
    class TDoubleNode
    {
    public:
         TDoubleNode( TDoubleNode* pPrev, TDoubleNode* pNext, const T& Data ) :
            m_pPrev(pPrev),
            m_pNext(pNext),
            m_Data(Data)
        {
            // Empty
        }
        T& GetData( void ) { return m_Data; }
        const T& GetData( void ) const { return m_Data; }

        TDoubleNode* m_pPrev;
        TDoubleNode* m_pNext;
        T            m_Data;
    };
    class IteratorBase
    {
        friend class TDoubleList<T>;
    public:
        IteratorBase( void ) :
            m_pList(NULL),
            m_pNode(NULL)
        {
            // Empty
        }
        bool operator++( void )
        {
            assert( m_pList && m_pNode );
            m_pNode = m_pNode->m_pNext;
            return ( m_pNode != NULL );
        }
        bool operator++( int )
        {
            assert( m_pList && m_pNode );
            m_pNode = m_pNode->m_pNext;
            return ( m_pNode != NULL );
        }
        bool operator--( void )
        {
            assert( m_pList && m_pNode );
            m_pNode = m_pNode->m_pPrev;
            return ( m_pNode != NULL );
        }
        bool operator--( int )
        {
            assert( m_pList && m_pNode );
            m_pNode = m_pNode->m_pNext;
            return ( m_pNode != NULL );
        }
        operator bool( void ) const
        {
            return ( m_pList != NULL && m_pNode != NULL );
        }
        bool operator==( const IteratorBase& itr ) const
        {
            return ( itr.m_pList == m_pList && itr.m_pNode == m_pNode );
        }
        bool operator!=( const IteratorBase& itr ) const
        {
            return ( itr.m_pList != m_pList || itr.m_pNode != m_pNode );
        }

    protected:
        IteratorBase( const TDoubleList<T>* pList, TDoubleNode* pNode ) :
             m_pList(pList),
             m_pNode(pNode)
        {
            // Empty
        }

    protected:
        const TDoubleList<T>* m_pList;
        TDoubleNode*          m_pNode;
    };
public:
    class Iterator : public IteratorBase
    {
        friend class TDoubleList<T>;
    public:
        Iterator( void ) : IteratorBase()
        {
            // Empty
        }
        T& operator*( void )
        {
            assert( this->m_pList && this->m_pNode );
            return this->m_pNode->GetData();
        }
    protected:
        Iterator( const TDoubleList<T>* pList, TDoubleNode* pNode ) :
            IteratorBase( pList, pNode )
        {
            // Empty
        }
    };
    class ConstIterator : public IteratorBase
    {
        friend class TDoubleList<T>;
    public:
        ConstIterator( void ) : IteratorBase()
        {
            // Empty
        }
        const T& operator*( void ) const
        {
            assert( this->m_pList && this->m_pNode );
            return this->m_pNode->GetData();
        }
    protected:
        ConstIterator( const TDoubleList<T>* pList, TDoubleNode* pNode ) :
            IteratorBase( pList, pNode )
        {
            // Empty
        }
    };

    TDoubleList( void ) : m_pHead(NULL), m_pTail(NULL), m_nCount(0) {}
    ~TDoubleList( void )
    {
        while( ! IsEmpty() )
        {
            RemoveHead();
        }
    }

    bool IsEmpty( void ) const { return m_pHead == NULL; }
    unsigned int GetCount( void ) const { return m_nCount; }
    void InsertHead( const T& Data )
    {
        TDoubleNode* pNode = new TDoubleNode( NULL, m_pHead, Data );
        assert( pNode );
        if( m_pHead )
        {
            m_pHead->m_pPrev = pNode;
        }
        m_pHead = pNode;
        if( ! m_pTail )
        {
            m_pTail = pNode;
        }
        m_nCount++;
    }
    void InsertTail( const T& Data )
    {
        TDoubleNode* pNode = new TDoubleNode( m_pTail, NULL, Data );
        assert( pNode );
        if( m_pTail )
        {
            m_pTail->m_pNext = pNode;
        }
        m_pTail = pNode;
        if( ! m_pHead )
        {
            m_pHead = pNode;
        }
        m_nCount++;
    }
    void InsertBefore( const Iterator& itr, const T& Data )
    {
        assert( this == itr.m_pList && NULL != itr.m_pNode );
        assert( m_pHead && m_pTail && m_nCount );
        TDoubleNode* pNode = new TDoubleNode( NULL, itr.m_pNode, Data );
        assert( pNode );
        if( itr.m_pNode->m_pPrev )
        {
            itr.m_pNode->m_pPrev->m_pNext = pNode;
            pNode->m_pPrev = itr.m_pNode->m_pPrev;
        }
        else
        {
            assert( m_pHead == itr.m_pNode );
            m_pHead = pNode;
        }
        itr.m_pNode->m_pPrev = pNode;
        m_nCount++;
    }
    void InsertAfter( const Iterator& itr, const T& Data )
    {
        assert( this == itr.m_pList && NULL != itr.m_pNode );
        assert( m_pHead && m_pTail && m_nCount );
        TDoubleNode* pNode = new TDoubleNode( itr.m_pNode, NULL, Data );
        assert( pNode );
        if( itr.m_pNode->m_pNext )
        {
            itr.m_pNode->m_pNext->m_pPrev = pNode;
            pNode->m_pNext = itr.m_pNode->m_pNext;
        }
        else
        {
            assert( m_pTail == itr.m_pNode );
            m_pTail = pNode;
        }
        itr.m_pNode->m_pNext = pNode;
        m_nCount++;
    }
    T Remove( Iterator& itr )
    {
        assert( this == itr.m_pList && NULL != itr.m_pNode );
        assert( m_pHead && m_pTail && m_nCount );
        TDoubleNode* pNode = itr.m_pNode;
        itr.m_pNode = NULL;
        if( pNode->m_pPrev )
        {
            pNode->m_pPrev->m_pNext = pNode->m_pNext;
        }
        else
        {
            assert( m_pHead == pNode );
            m_pHead = pNode->m_pNext;
        }
        if( pNode->m_pNext )
        {
            pNode->m_pNext->m_pPrev = pNode->m_pPrev;
        }
        else
        {
            assert( m_pTail == pNode );
            m_pTail = pNode->m_pPrev;
        }
        T Data = pNode->GetData();
        delete pNode;
        m_nCount--;
        return Data;
    }
    T RemoveHead( void )
    {
        assert( m_pHead && m_pTail && m_nCount );
        TDoubleNode* pNode = m_pHead;
        m_pHead = pNode->m_pNext;
        if( ! m_pHead )
        {
            m_pTail = NULL;
        }
        T Data = pNode->GetData();
        delete pNode;
        m_nCount--;
        return Data;
    }
    T RemoveTail( void )
    {
        assert( m_pHead && m_pTail && m_nCount );
        TDoubleNode* pNode = m_pTail;
        m_pTail = pNode->m_pPrev;
        if( ! m_pTail )
        {
            m_pHead = NULL;
        }
        T Data = pNode->GetData();
        delete pNode;
        m_nCount--;
        return Data;
    }

    Iterator      Begin( void )       { return Iterator( this, m_pHead ); }
    ConstIterator Begin( void ) const { return ConstIterator( this, m_pHead ); }
    Iterator      End( void )       { return Iterator( this, NULL ); }
    ConstIterator End( void ) const { return ConstIterator( this, NULL ); }

    protected:
        TDoubleNode* m_pHead;
        TDoubleNode* m_pTail;
        unsigned int m_nCount;
};

#endif  // _TLIST_H

