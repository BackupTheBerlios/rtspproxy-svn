/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#include "timer.h"
#include "app.h"

#include "dbg.h"

#define TVNORM(tv) while( tv.tv_usec > 1000*1000 ) { tv.tv_usec -= 1000*1000; tv.tv_sec++; }

CTimer::CTimer(CTimerResponse * pResponse):
m_pResponse(pResponse), m_mode(Disabled), m_next(0), m_interval(0)
{
	// Empty
}

CTimer::~CTimer(void)
{
	Disable();
}

void CTimer::SetResponse(CTimerResponse * pResponse)
{
	m_pResponse = pResponse;
}

CTimer::Mode CTimer::GetMode(void)
{
	return m_mode;
}

UINT32 CTimer::GetTimeout(void)
{
	return m_next;
}

void CTimer::SetRelative(UINT32 msec)
{
	Set(CurrentTime() + msec);
	m_mode = SingleShot;
}

void CTimer::SetAbsolute(UINT32 t)
{
	Set(t);
	m_mode = SingleShot;
}

void CTimer::SetRepeating(UINT32 msec)
{
	Set(CurrentTime() + msec);
	m_interval = msec;
	m_mode = Repeating;
}

void CTimer::Disable(void)
{
	if (m_mode != Disabled) {
		CEventThread *pSelf;
		m_mode = Disabled;
#ifdef NO_RTTI
		pSelf = (CEventThread *) CThread::This();	//XXX: very bad, upgrade compiler
#else
		pSelf = dynamic_cast < CEventThread * >(CThread::This());
#endif
		assert_or_ret(pSelf);
		pSelf->DelTimer(this);
	}
}

UINT32 CTimer::CurrentTime(void)
{
#ifdef _UNIX
	timeval tv;
	gettimeofday(&tv, NULL);
	return (tv.tv_sec * 1000 + tv.tv_usec / 1000);
#endif
#ifdef _WIN32
	return::GetTickCount();
#endif
}

void CTimer::Set(UINT32 t)
{
	m_next = t;

	CEventThread *pSelf;
#ifdef NO_RTTI
	pSelf = (CEventThread *) CThread::This();	//XXX: very bad, upgrade compiler
#else
	pSelf = dynamic_cast < CEventThread * >(CThread::This());
#endif
	assert_or_ret(pSelf);

	if (m_mode != Disabled)
		pSelf->DelTimer(this);
	pSelf->AddTimer(this);
}

CTimerResponse *CTimer::GetResponse(void)
{
	assert(m_pResponse);
	return m_pResponse;
}

/** LOG **
 *
 * $Log: timer.cpp,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

