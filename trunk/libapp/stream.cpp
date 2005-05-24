/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#include "types.h"
#include "dbg.h"
#include "stream.h"

CStream::CStream(void):
m_pResponse(NULL)
{
	// Empty
}

CStream::CStream(CStreamResponse * pResponse):
m_pResponse(pResponse)
{
	// Empty
}

CStream::~CStream(void)
{
	// Empty
}

void CStream::SetResponse(CStreamResponse * pResponse)
{
	m_pResponse = pResponse;
}

bool CStream::Read(CBuffer * pbuf)
{
	assert(pbuf);
	assert(pbuf->GetBuffer() != NULL && pbuf->GetSize() > 0);

	size_t n = Read(pbuf->GetBuffer(), pbuf->GetSize());
	pbuf->SetSize(n);
	return (n > 0);
}

bool CStream::Write(const CBuffer & buf)
{
	assert(buf.GetBuffer() != NULL && buf.GetSize() > 0);

	size_t n = Write(buf.GetBuffer(), buf.GetSize());
	return (n > 0);
}

/** LOG **
 *
 * $Log: stream.cpp,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

