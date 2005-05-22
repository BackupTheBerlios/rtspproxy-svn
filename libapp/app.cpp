/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#include "app.h"
#include "dbg.h"
#include "resolver.h"

#ifdef _UNIX
#include <signal.h>
#endif

CApp *g_pApp = NULL;

#ifdef _UNIX
extern pthread_key_t g_keyself;
#endif
#ifdef _WIN32
extern DWORD g_tlsself;
#endif

/*****************************************************************************
 *
 * CApp
 *
 *****************************************************************************/

CApp::CApp(int argc, char **argv):m_argc(argc), m_argv(argv)
{
	g_pApp = this;
	atexit(dump_alloc_heaps);
#ifdef _UNIX
	pthread_key_create(&g_keyself, NULL);
	pthread_setspecific(g_keyself, this);
#endif
#ifdef _WIN32
	g_tlsself =::TlsAlloc();
	::TlsSetValue(g_tlsself, this);
#endif
}

CApp::~CApp(void)
{
#ifdef _UNIX
	pthread_key_delete(g_keyself);
#endif
#ifdef _WIN32
	::TlsFree(g_tlsself);
#endif
}

void CApp::Run(void)
{
	CEventThread::Run();
}

bool CApp::Init(void)
{
#ifdef _WIN32
	WSADATA wd;
	int res =::WSAStartup(MAKEWORD(2, 0), &wd);
	if (res != 0 || wd.wVersion < MAKEWORD(2, 0)) {
		dbgout("Cannot start winsock 2.0");
		return false;
	}
#endif

	// init CResolver
	CResolver::GetResolver();
	return true;
}

int CApp::Exit(void)
{
#ifdef _WIN32
	::WSACleanup();
#endif

	delete CResolver::GetResolver();
	return 0;
}

/**
 * Become process lead and detach from tty
 * Code lifted from fetchmail 5.0.0
 */
void CApp::Daemonize(void)
{
#ifdef _UNIX
	/* Ignore BSD terminal stop signals */
#ifdef  SIGTTOU
	signal(SIGTTOU, SIG_IGN);
#endif
#ifdef  SIGTTIN
	signal(SIGTTIN, SIG_IGN);
#endif
#ifdef  SIGTSTP
	signal(SIGTSTP, SIG_IGN);
#endif

	/* Change to root dir so we don't hold any mount points open */
	if (chdir("/") != 0) {
		dbgout("chdir failed (%s)", strerror(errno));
	}

	/* In case we were not started in the background, fork and let
	   the parent exit.  Guarantees that the child is not a process
	   group leader */

	pid_t childpid;
	if ((childpid = fork()) < 0) {
		dbgout("fork failed (%s)", strerror(errno));
		return;
	} else if (childpid > 0) {
		exit(0);	/* parent */
	}


	/* Make ourselves the leader of a new process group with no
	   controlling terminal */

	/* POSIX makes this soooo easy to do */
	if (setsid() < 0) {
		dbgout("setsid failed (%s)", strerror(errno));
		return;
	}

	/* lose controlling tty */
	signal(SIGHUP, SIG_IGN);
	if ((childpid = fork()) < 0) {
		dbgout("fork failed (%s)", strerror(errno));
		return;
	} else if (childpid > 0) {
		exit(0);	/* parent */
	}
#endif
}

/** LOG **
 *
 * $Log: app.cpp,v $
 * Revision 1.2  2003/11/17 16:14:02  mat
 * make-up
 *
 *
 */

