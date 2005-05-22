/**************************************************************************
 *   Copyright (C) 2003 Matteo Merli <matteo.merli@studenti.unipr.it>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *   $Id$
 *****************************************************************************/
 
#ifndef _PREFETCHING_H_
#define _PREFETCHING_H_

#include <pthread.h>

#include "time_range.h"
#include "cache_segment.h"
#include "rtp_session.h"
#include "rtsp_session.h"

/*! This class ...
 */
class Prefetching 
{
public:
	/*! Constructor */
	Prefetching();
	
	/*! Destructor */ 
	~Prefetching();
	
	void set_stream_url(const char* url);
	
	void set_time_range(double start, double end) {m_time_range = new TimeRange(start, end);}
	
	void set_segment(CacheSegment *cs) {m_segment = cs;}
	
	/*! Start the execution in a new thread. */ 
	void start();
	
	void end();
	
	/*! The function will be executed in the new thread. 
	 *  Should not be called from exterior. Use start().
	 */
	void run();

protected:

	TimeRange *m_time_range;
	
	CacheSegment *m_segment;
	
	char *m_stream_url;

	pthread_t m_thread;
	
	RtspSession *m_rtsp;
	
	RtpSession *m_rtp;

};

#endif





/** LOG **
 *
 * $Log: prefetching.h,v $
 * Revision 1.3  2003/11/17 16:13:51  mat
 * make-up
 *
 *
 */

