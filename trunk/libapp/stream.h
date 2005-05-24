/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _STREAM_H
#define _STREAM_H

#include "types.h"
#include "str.h"
#include "tlist.h"

    // some custom bits (we rely on pollfd events being a 16-bit type)
    // note that these are combined with POLLIN/POLLOUT
#define XPOLLACC    0x00010000	/* accept */
#define XPOLLCNX    0x00020000	/* connect */

#include <sys/poll.h>
typedef pollfd waitobj_t;
typedef INT32 waitevents_t;
#define WAITOBJ_MAX          0xffff

#define WAITOBJ_IS_VALID(obj)       (obj.fd != -1)
#define WAIT_EVENT_ACCEPT(wevt)     (wevt & XPOLLACC)
#define WAIT_EVENT_CONNECT(wevt)    (wevt & XPOLLCNX)
#define WAIT_EVENT_READ(wevt)       (wevt & POLLIN)
#define WAIT_EVENT_WRITE(wevt)      (wevt & POLLOUT)
#define WAIT_EVENT_EXCEPT(wevt)     (wevt & POLLPRI)

class CStreamResponse {
      public:
	virtual void OnConnectDone(int err) = 0;
	virtual void OnReadReady(void) = 0;
	virtual void OnWriteReady(void) = 0;
	virtual void OnExceptReady(void) = 0;
	virtual void OnClosed(void) = 0;
};

class CStream {
//    friend class CEventThread;

 private:			// Unimplemented
	CStream(const CStream &);
	CStream & operator=(const CStream &);
	
 public:
	CStream(void);
	CStream(CStreamResponse * pResponse);
	virtual ~ CStream(void);
	
	virtual bool IsOpen(void) = 0;
	virtual void Close(void) = 0;
	virtual size_t Read(PVOID pbuf, size_t nLen) = 0;
	virtual size_t Write(CPVOID pbuf, size_t nLen) = 0;

	void SetResponse(CStreamResponse * pResponse);
	bool Read(CBuffer * pbuf);
	bool Write(const CBuffer & buf);

 protected:
	 inline CStreamResponse * GetResponse(void) {
		return m_pResponse;
	 } protected:
		 CStreamResponse * m_pResponse;
};
typedef TDoubleList < CStream * >CStreamList;

#endif				//ndef _STREAM_H

/** LOG **
 *
 * $Log: stream.h,v $
 * Revision 1.2  2003/11/17 16:14:03  mat
 * make-up
 *
 *
 */

