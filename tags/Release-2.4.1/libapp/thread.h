/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _THREAD_H
#define _THREAD_H

#include "types.h"

#ifdef _UNIX
#include <pthread.h>
typedef pthread_t threadobj_t;
typedef int waittimer_t;
#ifndef INFTIM
#define INFTIM (-1)		/* Linux uses -1 but doesn't define INFTIM */
#endif
#endif
#ifdef _WIN32
typedef HANDLE threadobj_t;
typedef DWORD waittimer_t;
#define INFTIM  INFINITE
#endif

#include "types.h"
#include "tlist.h"
#include "stream.h"
#include "sock.h"
#include "timer.h"

class CMutex {
      private:			// Unimplemented
	CMutex(const CMutex &);
	 CMutex & operator=(const CMutex &);

      public:
	 CMutex(void);
	 virtual ~ CMutex(void);

	void Lock(void);
	void Unlock(void);

      private:
#ifdef _UNIX
	 pthread_mutex_t m_mutex;
#endif
#ifdef _WIN32
	HANDLE m_mutex;
#endif
};

class CSemaphore {
      private:			// Unimplemented
	CSemaphore(const CSemaphore &);
	 CSemaphore & operator=(const CSemaphore &);

      public:
	 CSemaphore(UINT nCount);
	 virtual ~ CSemaphore(void);

	void Lock(void);
	void Unlock(void);

      private:
#ifdef _UNIX
	 pthread_mutex_t m_mutex;
	pthread_cond_t m_cond;
	UINT m_count;
#endif
#ifdef _WIN32
	HANDLE m_semaphore;
#endif
};

/*
 * Initializing a C++ thread object must involve two steps: creating the
 * C++ object and creating the thread.  If we create the thread in the
 * ctor, we are courting disaster because the C++ object is not fully
 * constructed until after the ctor finishes.  If you think this is all
 * theoretical and doesn't apply in real life, consider this:
 *
 *   - GNU g++ will not call a derived virtual function until after
 *     the ctor is done -- it will *always* call the function defined in
 *     the current class.  Been there, done that, spent hours debugging.
 *
 *   - MS Visual C++ warns about using 'this' in the ctor.  Probably
 *     because more than one programmer has been bitten doing it.
 */

class CThread {
#ifdef _UNIX
	friend void *thread_start(void *);
#endif
#ifdef _WIN32
	friend DWORD WINAPI thread_start(LPVOID);
#endif

      private:			// Unimplemented
	 CThread(const CThread &);
	 CThread & operator=(const CThread &);

      public:
	 CThread(void);
	 virtual ~ CThread(void);

	void Create(void);

//    void Suspend( void );
//    void Resume( void );

	static CThread *This(void);

      protected:
	 virtual bool Init(void);
	virtual void Run(void);
	virtual int Exit(void);

      private:
	 threadobj_t m_thread;
	int m_retval;
};

/*
 * CEventThread -- the scheduler
 */

class CEventThread:public CThread {
      public:
	CEventThread(void);
	~CEventThread(void);

      protected:
	 virtual void Run(void);
	virtual bool Init(void);
	virtual int Exit(void);

      private:
	 friend class CSocket;
	friend class CTimer;
	bool AddStream(CSocket * pSock);
	void DelStream(CSocket * pSock);
	void SetStreamSelect(CSocket * pSock, UINT nWhich);
	void AddTimer(CTimer * pTimer);
	void DelTimer(CTimer * pTimer);

	void Heapify(UINT32 now, UINT n);

      private:
	 UINT m_nSocks;
	UINT m_nSockAlloc;
	CSocket **m_ppSocks;
	waitobj_t *m_pWaitObjs;
	UINT m_nTimers;
	UINT m_nTimerAlloc;
	CTimer **m_ppTimers;
};

#endif				//ndef _THREAD_H

/** LOG **
 *
 * $Log: thread.h,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

