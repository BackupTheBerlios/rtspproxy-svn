/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _PARSER_H
#define _PARSER_H

#include "types.h"
#include "str.h"

class CToken {
      public:
	enum TokType {
		TOK_NONE,
		TOK_STRING,
		TOK_EOL,
		TOK_EOF
	};

	 CToken(void):type(TOK_NONE) {
	} TokType type;
	CString val;
};

class CParser {
      private:			// Unimplemented
	CParser(const CParser &);
	 CParser & operator=(const CParser &);

      protected:
	 CParser(void);
	 virtual ~ CParser(void);
};

class CConfigParser:public CParser {
      private:			// Unimplemented
	CConfigParser(const CConfigParser &);
	 CConfigParser & operator=(const CConfigParser &);

      public:
	 CConfigParser(void);
	 CConfigParser(const CString & strFile);
	 virtual ~ CConfigParser(void);

	void Open(const CString & strFile);
	void Close(void);
	CToken NextToken(void);
	void NextLine(void);

      protected:
	 size_t m_pos;
	CBuffer m_buf;
};

#endif				//ndef _PARSER_H

/** LOG **
 *
 * $Log: parser.h,v $
 * Revision 1.2  2003/11/17 16:14:02  mat
 * make-up
 *
 *
 */

