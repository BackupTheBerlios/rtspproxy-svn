/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#include <string.h>

#ifdef _UNIX
#include <sys/types.h>
#include <sys/socket.h>
#endif

#include "thread.h"
#include "dbg.h"

#ifdef _UNIX

pthread_key_t g_keyself;

void *thread_start(void *pvoid)
{
	CThread *pth;

	pthread_setspecific(g_keyself, pvoid);

	pth = (CThread *) pvoid;
	if (pth->Init()) {
		pth->Run();
	}
	pth->m_retval = pth->Exit();
	return NULL;
}

#endif

/****************************************************************************
 *
 * CMutex
 *
 ****************************************************************************/

CMutex::CMutex(void)
{
	// We could use PTHREAD_MUTEX_ERRORCHECK_NP for Linux debug builds
	pthread_mutex_init(&m_mutex, NULL);
}

CMutex::~CMutex(void)
{
	pthread_mutex_destroy(&m_mutex);
}

void CMutex::Lock(void)
{
	pthread_mutex_lock(&m_mutex);
}

void CMutex::Unlock(void)
{
	pthread_mutex_unlock(&m_mutex);
}

/****************************************************************************
 *
 * CSemaphore
 *
 ****************************************************************************/

CSemaphore::CSemaphore(UINT nCount)
{
	// We could use PTHREAD_MUTEX_ERRORCHECK_NP for Linux debug builds
	pthread_mutex_init(&m_mutex, NULL);
	pthread_cond_init(&m_cond, NULL);
	m_count = nCount;
}

CSemaphore::~CSemaphore(void)
{
	pthread_cond_destroy(&m_cond);
	pthread_mutex_destroy(&m_mutex);
}

void CSemaphore::Lock(void)
{
	pthread_mutex_lock(&m_mutex);
	while (m_count == 0) {
		pthread_cond_wait(&m_cond, &m_mutex);
	}
	m_count--;
	pthread_mutex_unlock(&m_mutex);
}

void CSemaphore::Unlock(void)
{
	pthread_mutex_lock(&m_mutex);
	m_count++;
	pthread_mutex_unlock(&m_mutex);
	pthread_cond_signal(&m_cond);
}

/****************************************************************************
 *
 * CThread
 *
 ****************************************************************************/

CThread::CThread(void)
{
	// Empty
}

CThread::~CThread(void)
{
	// Empty
}

void CThread::Create(void)
{
	pthread_create(&m_thread, NULL, thread_start, this);
}

bool CThread::Init(void)
{
	return false;
}

int CThread::Exit(void)
{
	return 0;
}

void CThread::Run(void)
{
}

CThread *CThread::This(void)
{
	return (CThread *) pthread_getspecific(g_keyself);
}

/****************************************************************************
 *
 * CEventThread
 *
 ****************************************************************************/

#define MIN_TIMER_ALLOC     16
#define MIN_STREAM_ALLOC     4

#define LEFT(n)     (2*n)
#define RIGHT(n)    (2*n+1)
#define PARENT(n)   (n/2)
#define SWAP(t,a,b) { t=a; a=b; b=t; }

CEventThread::CEventThread(void):
m_nSocks(0),
m_nSockAlloc(0),
m_ppSocks(NULL),
m_pWaitObjs(NULL),
m_nTimers(0),
m_nTimerAlloc(0),
m_ppTimers(NULL)
{
	m_nSockAlloc = MIN_STREAM_ALLOC;
	m_ppSocks = new CSocket *[MIN_STREAM_ALLOC];
	memset(m_ppSocks, 0, MIN_STREAM_ALLOC * sizeof(CStream *));
	m_pWaitObjs = new waitobj_t[MIN_STREAM_ALLOC];
	memset(m_pWaitObjs, 0, MIN_STREAM_ALLOC * sizeof(waitobj_t));
	m_nTimerAlloc = MIN_TIMER_ALLOC;
	m_ppTimers = new CTimer *[MIN_TIMER_ALLOC];
	memset(m_ppTimers, 0, MIN_TIMER_ALLOC * sizeof(CTimer *));
}

CEventThread::~CEventThread(void)
{
	delete[]m_ppTimers;
	delete[]m_pWaitObjs;
	delete[]m_ppSocks;
}

void CEventThread::Run(void)
{
	if ( ! Init() ) {
		dbg("Failed init()\n");
		Exit();
		return;
	}

	bool running = true;
	while ( running ) {
		// Figure out how long we can remain in poll/WFMO, possibly forever
		waittimer_t nTimeout = INFTIM;
		if ( m_nTimers ) {
			assert( m_ppTimers[1] );
			nTimeout = m_ppTimers[1]->GetTimeout() - CTimer::CurrentTime();
			if (nTimeout < 0 || nTimeout > 0x7FFFFFFF)
				nTimeout = 0;	// Wrap - it's late
		}

		int rc = poll( m_pWaitObjs, m_nSocks, nTimeout );
		if (rc < 0) {
			dbg("poll() failed: error = %i (%s)", errno, strerror(errno));
			// break;
		}

		if (rc == 0 && m_nTimers) {

			assert( m_nTimers && m_nTimerAlloc >= 2 && m_ppTimers[1] );

			CTimer *pTimer = m_ppTimers[1];
			if (pTimer->GetMode() == CTimer::Repeating) {
				pTimer->m_next += pTimer->m_interval;
			} else {
				pTimer->m_mode = CTimer::Disabled;
				m_ppTimers[1] = m_ppTimers[m_nTimers--];
			}
			Heapify(CTimer::CurrentTime(), 1);
			pTimer->GetResponse()->OnTimer();
		}
		if (rc > 0) {
			UINT n;
			for (n = 0; n < m_nSocks; n++) {
				assert(WAITOBJ_IS_VALID(m_pWaitObjs[n]) && NULL != m_ppSocks[n]);
				
				int err = SOCKERR_NONE;
				waitevents_t wevt;
				CSocket *pSock = m_ppSocks[n];
				
				wevt = m_pWaitObjs[n].revents;
				if ((wevt & (POLLIN | POLLERR))
				    && (pSock->m_uSelectFlags & SF_ACCEPT) == SF_ACCEPT) {
					wevt = XPOLLACC;
				}
				if ((wevt & (POLLOUT | POLLERR)) && (pSock->m_uSelectFlags & SF_CONNECT) == SF_CONNECT) {
					socklen_t errlen =  sizeof(err);
					
					getsockopt(pSock->GetHandle(), SOL_SOCKET, SO_ERROR, &err,&errlen);
					wevt = XPOLLCNX;
				}
				if ((wevt & POLLERR)) {
					wevt = pSock->m_uSelectFlags;
				}
				
				if (WAIT_EVENT_READ(wevt)) {
					pSock->GetResponse()->OnReadReady();
					
				} else if (WAIT_EVENT_WRITE(wevt)) {
					pSock->GetResponse()->OnWriteReady();
				} else if (WAIT_EVENT_ACCEPT(wevt)) {
					CListenSocket *pListen = (CListenSocket *)pSock;
					sockaddr_in sa;
					socklen_t salen = sizeof(sa);
					sockobj_t sock = accept( pListen->GetHandle(),
								 (sockaddr *) &sa, &salen);
					if (INVALID_SOCKET != sock) {
						CTcpSocket *pNew = new CTcpSocket();
						pNew->m_sock = sock;
						pListen->m_pAcceptResponse->OnConnection(pNew);
					}
				} else
					if ( WAIT_EVENT_CONNECT( wevt ) ) {
						dbg("EventTread::run()  error....\n");
						pSock->GetResponse()->OnConnectDone(err);
					} else if ( WAIT_EVENT_EXCEPT( wevt ) ) {
						pSock->GetResponse()->OnExceptReady();
					}
			}
		}
	}	
	Exit();
}

bool CEventThread::Init(void)
{
	dbg("CEventThread::Init\n");
	return false;
}

int CEventThread::Exit(void)
{
	return 0;
}

bool CEventThread::AddStream(CSocket * pSock)
{
	assert_or_retv(false, pSock);

	if (m_nSocks >= WAITOBJ_MAX) {
		return false;
	}

	if (m_nSocks == m_nSockAlloc) {
		UINT nNewAlloc = m_nSocks * 2;
		CSocket **ppSocks = new CSocket *[nNewAlloc];
		memset(ppSocks, 0xDD, nNewAlloc * sizeof(CSocket *));
		memcpy(ppSocks, m_ppSocks, m_nSocks * sizeof(CSocket *));
		delete[]m_ppSocks;
		m_ppSocks = ppSocks;
		waitobj_t *pWaitObjs = new waitobj_t[nNewAlloc];
		memset(pWaitObjs, 0xDD, nNewAlloc * sizeof(waitobj_t));
		memcpy(pWaitObjs, m_pWaitObjs,
		       m_nSocks * sizeof(waitobj_t));
		delete[]m_pWaitObjs;
		m_pWaitObjs = pWaitObjs;
		m_nSockAlloc = nNewAlloc;
	}

	m_ppSocks[m_nSocks] = pSock;

	m_pWaitObjs[m_nSocks].fd = pSock->GetHandle();
	m_pWaitObjs[m_nSocks].events = 0;
	m_pWaitObjs[m_nSocks].revents = 0;

	m_nSocks++;
	return true;
}

void CEventThread::DelStream(CSocket * pSock)
{
	assert_or_ret(pSock);

	UINT n;
	for (n = 0; n < m_nSocks; n++) {
		if (m_ppSocks[n] == pSock) {

			m_nSocks--;
			m_ppSocks[n] = m_ppSocks[m_nSocks];
			m_pWaitObjs[n] = m_pWaitObjs[m_nSocks];
			break;
		}
	}

	if (m_nSocks <= m_nSockAlloc / 4
	    && m_nSockAlloc > MIN_STREAM_ALLOC) {
		UINT nNewAlloc = m_nSockAlloc / 2;
		CSocket **ppSocks = new CSocket *[nNewAlloc];
		memset(ppSocks, 0xDD, nNewAlloc * sizeof(CSocket *));
		memcpy(ppSocks, m_ppSocks, m_nSocks * sizeof(CSocket *));
		delete[]m_ppSocks;
		m_ppSocks = ppSocks;
		waitobj_t *pWaitObjs = new waitobj_t[nNewAlloc];
		memset(pWaitObjs, 0xDD, nNewAlloc * sizeof(waitobj_t));
		memcpy(pWaitObjs, m_pWaitObjs,
		       m_nSocks * sizeof(waitobj_t));
		delete[]m_pWaitObjs;
		m_pWaitObjs = pWaitObjs;
		m_nSockAlloc = nNewAlloc;
	}
}

void CEventThread::SetStreamSelect(CSocket * pSock, UINT nWhich)
{
	assert_or_ret(pSock && pSock->IsOpen());

	UINT n;
	for (n = 0; n < m_nSocks; n++) {
		if (m_ppSocks[n] == pSock) {

			m_pWaitObjs[n].events = nWhich;
			break;
		}
	}
}

// O( lg(n) )
void CEventThread::AddTimer(CTimer * pTimer)
{
	assert_or_ret(pTimer);

	if (1 + m_nTimers == m_nTimerAlloc) {
		UINT nNewAlloc = m_nTimerAlloc * 2;
		CTimer **ppTimers = new CTimer *[nNewAlloc];
		memset(ppTimers, 0xDD, nNewAlloc * sizeof(CTimer *));
		memcpy(ppTimers, m_ppTimers,
		       (1 + m_nTimers) * sizeof(CTimer *));
		delete[]m_ppTimers;
		m_nTimerAlloc = nNewAlloc;
		m_ppTimers = ppTimers;
	}

	m_nTimers++;
	UINT n = m_nTimers;
	UINT32 t = pTimer->GetTimeout();
	while (n > 1 && t < m_ppTimers[PARENT(n)]->GetTimeout()) {
		m_ppTimers[n] = m_ppTimers[PARENT(n)];
		n = PARENT(n);
	}

	m_ppTimers[n] = pTimer;
}

// O( n )
void CEventThread::DelTimer(CTimer * pTimer)
{
	assert_or_ret(pTimer);

	UINT n;
	for (n = 1; n <= m_nTimers; n++) {
		if (m_ppTimers[n] == pTimer) {
			m_ppTimers[n] = m_ppTimers[m_nTimers];
			m_nTimers--;
			Heapify(CTimer::CurrentTime(), n);
			break;
		}
	}

	// Shrink heap if we are using less than 1/4 and more than minimum
	if (m_nTimers <= m_nTimerAlloc / 4
	    && m_nTimerAlloc > MIN_TIMER_ALLOC) {
		UINT nNewAlloc = m_nTimerAlloc / 2;
		CTimer **ppTimers = new CTimer *[nNewAlloc];
		memset(ppTimers, 0xDD, nNewAlloc * sizeof(CTimer *));
		memcpy(ppTimers, m_ppTimers,
		       (1 + m_nTimers) * sizeof(CTimer *));
		delete[]m_ppTimers;
		m_nTimerAlloc = nNewAlloc;
		m_ppTimers = ppTimers;
	}
}

void CEventThread::Heapify(UINT32 now, UINT n)
{

	UINT lnode = LEFT(n);
	UINT rnode = RIGHT(n);
	UINT low = n;

	UINT32 dn, dl, dr;

	dn = m_ppTimers[n]->GetTimeout() - now;
	if (dn > 0x7FFFFFFF)
		dn = 0;

	if (lnode <= m_nTimers) {
		dl = m_ppTimers[lnode]->GetTimeout() - now;
		if (dl > 0x7FFFFFFF)
			dl = 0;
		if (dl < dn) {
			low = lnode;
			dn = dl;
		}
	}

	if (rnode <= m_nTimers) {
		dr = m_ppTimers[rnode]->GetTimeout() - now;
		if (dr > 0x7FFFFFFF)
			dr = 0;
		if (dr < dn) {
			low = rnode;
			dn = dr;
		}
	}

	if (low != n) {
		CTimer *tmp;
		SWAP(tmp, m_ppTimers[n], m_ppTimers[low]);
		Heapify(now, low);
	}
}

/** LOG **
 *
 * $Log: thread.cpp,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

