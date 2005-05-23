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

#ifndef TIME_RANGE_H
#define TIME_RANGE_H

/*!
 * The class TimeRange can convert a range time indication (for the moment 
 * only in npt format) in a couple of float value.
 */
class TimeRange {

 public:
	/*! Constructor.
	 * @param str String to be converted. Must be like 'npt=12.0-34.005' or
	 * something like that...
	 */
	TimeRange(const char *str);

	/*!
	 * Constructor.
	 * @param start Start of the range.
	 * @param end End of the range.
	 */
	TimeRange(double start, double end=0.0);

	/*! Generic constructor. */
	TimeRange();

        /*! Destructor.  */
	~TimeRange();

	/*! */
	double start() {return m_range_start;}
	double end()   {return m_range_end;  }
	
	void set_start(double s) {m_range_start = s;}
	void set_end(double e) {m_range_end = e;}

	void set_range(const char *str);

	/*!
	 * Returns a string representation of the range.
	 * The string should be freed by the caller using free()
	 */
	void to_str(char *str, int l);

	/*!
	 * @return Returns true if the TimeRange tr is "included" into the current
	 *         TimeRange.
	 */
	bool contain(TimeRange *tr);

 private:

	bool parse(const char* str);

	double m_range_start;
	double m_range_end;
};




#endif

