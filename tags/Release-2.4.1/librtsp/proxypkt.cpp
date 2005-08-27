/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#include "proxypkt.h"

#include "dbg.h"

/*
 * A proxy packet is a wrapper for a buffer.  It does not "own" the buffer.
 */

CProxyPacket::CProxyPacket(void):m_pbuf(NULL)
{
	// Empty
}

CProxyPacket::~CProxyPacket(void)
{
	// Empty
}

CBuffer *CProxyPacket::Get(void)
{
	assert(m_pbuf);

	return m_pbuf;
}

bool CProxyPacket::Set(CBuffer * pbuf)
{
	assert_or_retv(false, (m_pbuf == NULL && pbuf != NULL));

	m_pbuf = pbuf;
	return true;
}

/** LOG **
 *
 * $Log: proxypkt.cpp,v $
 * Revision 1.2  2003/11/17 16:14:08  mat
 * make-up
 *
 *
 */

