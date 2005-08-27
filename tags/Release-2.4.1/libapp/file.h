/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _FILE_H
#define _FILE_H

#include "types.h"
#include "str.h"
#include "stream.h"

#ifdef _UNIX
typedef int fileobj_t;
#define INVALID_FILE -1
#endif
#ifdef _WIN32
typedef HANDLE fileobj_t;
#define INVALID_FILE NULL
#endif

class CFile:public CStream {
      public:
	CFile(void);
	 CFile(CStreamResponse * pResponse);
	 virtual ~ CFile(void);

	virtual bool IsOpen(void);
	virtual void Close(void);

	bool Open(const CString & strFile);

	bool Stat(struct stat *pst);
	size_t Read(PVOID buf, size_t len);
	size_t Write(CPVOID buf, size_t len);

	static bool Stat(const CString & strFile, struct stat *pst);

      protected:
	 fileobj_t m_file;
};

#endif				//ndef _FILE_H

/** LOG **
 *
 * $Log: file.h,v $
 * Revision 1.2  2003/11/17 16:14:02  mat
 * make-up
 *
 *
 */

