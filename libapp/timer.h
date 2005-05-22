/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _TIMER_H
#define _TIMER_H

#include "types.h"
#include "tlist.h"

class CTimerResponse {
      public:
	virtual void OnTimer(void) = 0;
};

class CTimer {
	friend class CEventThread;

      public:
	typedef enum { Disabled, SingleShot, Repeating } Mode;

	 CTimer(CTimerResponse * pResponse);
	 virtual ~ CTimer(void);

	void SetResponse(CTimerResponse * pResponse);
	Mode GetMode(void);
	UINT32 GetTimeout(void);
	void SetRelative(UINT32 msec);
	void SetAbsolute(UINT32 t);
	void SetRepeating(UINT32 msec);
	void Disable(void);

	static UINT32 CurrentTime(void);

      protected:
	void Set(UINT32 t);
	CTimerResponse *GetResponse(void);

      private:
	 CTimerResponse * m_pResponse;
	Mode m_mode;
	UINT32 m_next;
	UINT32 m_interval;
};

typedef TDoubleList < CTimer * >CTimerList;

#endif				//ndef _TIMER_H

/** LOG **
 *
 * $Log: timer.h,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

