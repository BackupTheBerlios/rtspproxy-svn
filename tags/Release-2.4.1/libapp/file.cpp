/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#include "file.h"

#include <fcntl.h>

#include "dbg.h"

CFile::CFile(void):CStream(), m_file(INVALID_FILE)
{
	// Empty
}

CFile::CFile(CStreamResponse * pResponse):
CStream(pResponse), m_file(INVALID_FILE)
{
	// Empty
}

CFile::~CFile(void)
{
	if (IsOpen())
		Close();
}

bool CFile::IsOpen(void)
{
	return (INVALID_FILE != m_file);
}

void CFile::Close(void)
{
	assert(IsOpen());

#ifdef _UNIX
	close(m_file);
#endif
#ifdef _WIN32
	::CloseHandle(m_file);
#endif

	m_file = INVALID_FILE;
	if (m_pResponse)
		m_pResponse->OnClosed();
}

bool CFile::Open(const CString & strFile)
{
	assert(!IsOpen());

#ifdef _UNIX
	m_file = open(strFile, O_NONBLOCK );
#endif
#ifdef _WIN32
	m_file =::CreateFile(strFile, GENERIC_READ | GENERIC_WRITE,
			     FILE_SHARE_READ | FILE_SHARE_WRITE, NULL,
			     OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
#endif

	return IsOpen();
}

bool CFile::Stat(struct stat * pst)
{
	assert(IsOpen());

	//XXXFIXME: this does NOT work for Win32 but it will compile
	return (0 == fstat((int) m_file, pst));
}

size_t CFile::Read(PVOID pbuf, size_t len)
{
	assert(pbuf);
	assert(IsOpen());

#ifdef _UNIX
	ssize_t nRead = read(m_file, pbuf, len);
	if (nRead < 0) {
		Close();
		nRead = 0;
	}
#endif
#ifdef _WIN32
	DWORD nRead = 0;
	if (!::ReadFile(m_file, pbuf, len, &nRead, NULL)) {
		Close();
		nRead = 0;
	}
#endif

	return nRead;
}

size_t CFile::Write(CPVOID pbuf, size_t len)
{
	assert(pbuf);
	assert(IsOpen());

#ifdef _UNIX
	ssize_t nWritten = write(m_file, pbuf, len);
	if (nWritten < 0) {
		Close();
		nWritten = 0;
	}
#endif
#ifdef _WIN32
	DWORD nWritten = 0;
	if (!::WriteFile(m_file, pbuf, len, &nWritten, NULL)) {
		Close();
		nWritten = 0;
	}
#endif

	return nWritten;
}

bool CFile::Stat(const CString & strFile, struct stat * pst)
{
	return (0 == stat(strFile, pst));
}

/** LOG **
 *
 * $Log: file.cpp,v $
 * Revision 1.2  2003/11/17 16:14:02  mat
 * make-up
 *
 *
 */

