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
 
#include "time_range.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <dbg.h>

TimeRange::TimeRange(const char* str)
{
	if ( !str ) {
		m_range_start = 0.0;
		m_range_end  =  0.0;
		return;
	}
	
	char *s = strdup( str );

	if ( !parse( s ) ) {
		m_range_start = 0.0;
		m_range_end =   0.0;
	}
	
	free( s );
}

TimeRange::TimeRange(double start, double end)
{
	if ( start < 0.0 )
		m_range_start = 0.0;
	else
		m_range_start = start;
	if ( end < 0.0 )
		m_range_end = 0.0;
	else
		m_range_end = end;
}

TimeRange::TimeRange()
{
	m_range_start = 0.0;
	m_range_end = 0.0;
}


TimeRange::~TimeRange()
{
}

void TimeRange::set_range(const char* str)
{
	if ( !str ) {
		m_range_start = 0.0;
		m_range_end  =  0.0;
		return;
	}

	if ( !parse( str ) ) {
		m_range_start = 0.0;
		m_range_end =   0.0;
	}
}

void TimeRange::to_str(char *str, int l)
{
	if (m_range_end != 0.0)
		snprintf(str, l, "npt=%f-%f", m_range_start, m_range_end);
	else
		snprintf(str, l, "npt=%f-", m_range_start);
}

bool TimeRange::contain(TimeRange *tr)
{
	if ( m_range_start > tr->start() ) 
		return false;

	if ( m_range_end == 0.0 )
		return true;
	
	/* We should use a margin because we are using
	 * the timestamp of the last packet.. so the end 
	 * of the range is always inferior to the play range.
	 * We use a margin of 2%
	 */
	if ( m_range_end * 1.02 < tr->end() )
		return false;

	return true;
}

bool TimeRange::parse(const char* str)
{
	double start, end;
	char *s = (char *)str;
 	if ( s[0]!='n' || s[1]!='p' || s[2]!='t' || s[3]!='=' ) {
		/* The format is not NPT, we cannot decode it..
		 */
		return false;
	}

	s += 4;
	int l = strlen(s);
	if ( s[0] == '-' ) {
		/* Start time is not specified. */
		sscanf(s, "-%lf", &end);
		start = 0.0;
	}
	if ( s[l] == '-' ) {
		/* End time is not specified.   */
		sscanf(s, "%lf-", &start);
		end = 0.0;
	} else {
		sscanf(s, "%lf-%lf", &start, &end);
	}
	
	if (start < 0.0)
		start = 0.0;
	if (end != 0.0 && end < start)
		return false;

	m_range_start = start;
	m_range_end = end;

	// dbg("TimeRange:: start: %f - end: %f\n", start, end );

	return true;
}



/** LOG **
 *
 * $Log: time_range.cpp,v $
 * Revision 1.3  2003/11/17 16:13:45  mat
 * make-up
 *
 *
 */

