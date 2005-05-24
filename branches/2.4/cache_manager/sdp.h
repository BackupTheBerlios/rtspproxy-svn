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
 
#ifndef _SDP_H_
#define _SDP_H_

#include <sys/types.h>
#include "../libapp/tlist.h"

/*! SDP Item */
typedef struct {
	/*! Checksum of the URL associated with this SDP description. */
	u_int16_t id;

	/*! URL of the resource. */
	char *url;

	/*! SDP description. */
	char *content;
} sdp_item_t;

typedef TSingleList< sdp_item_t * > sdp_item_list_t;

/*! This class will keep track of all the SDP description
 *  that will pass in the proxy.
 */
class SDP
{
public:
	/*! Constructor. */
	SDP();

	/*! Destructor. */
	~SDP();

	/*! Check if the SDP item associated with url, is present
	 *  in the list.
	 */
	// check( const char* url );

	/*! Adds an SDP item to the list.. */
	void add( const char* url, const char* sdp );

	/*! Returns the SDP item associated with url. */
	char *get( const char* url );

protected:
	sdp_item_list_t* m_sdp_item_list;

};

#endif

