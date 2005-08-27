/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _APP_H
#define _APP_H

#include "types.h"
#include "tlist.h"
#include "thread.h"

class CApp:public CEventThread {
      private:			// Unimplemented
	CApp(void);
	CApp(const CApp &);
	 CApp & operator=(const CApp &);

	void Create(void);	// App thread exists by definition

      public:
	 CApp(int argc, char **argv);
	 virtual ~ CApp(void);

	virtual void Run(void);

      protected:
	 virtual bool Init(void);
	virtual int Exit(void);

	void Daemonize(void);

      protected:
	int m_argc;
	char **m_argv;
};

extern CApp *g_pApp;

#endif				//ndef _APP_H

/** LOG **
 *
 * $Log: app.h,v $
 * Revision 1.2  2003/11/17 16:14:02  mat
 * make-up
 *
 *
 */

