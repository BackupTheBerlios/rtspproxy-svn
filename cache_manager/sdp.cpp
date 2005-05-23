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
 
#include <sys/types.h>

#include "sdp.h"
#include "cache_items.h"
#include "../libapp/dbg.h"

#include <string.h>
#include <assert.h>

SDP::SDP()
{
	m_sdp_item_list = new sdp_item_list_t();
}

SDP::~SDP()
{
}

void SDP::add( const char* url, const char* sdp )
{
	assert( url );
	if ( !sdp || !strlen(sdp) )
		return;

	uint16_t cs = checksum( (void*)url, strlen(url) );

	sdp_item_list_t::Iterator it = m_sdp_item_list->Begin();
	for ( ; it; it++ ) {
		if ( (*it)->id == cs ) {
			if ( ! strcmp( url, (*it)->url ) ) {
				/* SDP item is already present in cache... */
				return;
			}
		}
	}

	dbg("\n[Adding SDP: '%s' - %u] \n", url, strlen(url) );

	sdp_item_t *item = (sdp_item_t *)malloc( sizeof(sdp_item_t) );
	item->id = cs;
	item->url = strdup( url );
	item->content = strdup( sdp );
	m_sdp_item_list->InsertTail( item );
}

char *SDP::get( const char* url )
{
	assert( url );

	uint16_t cs = checksum( (void*)url, strlen(url) );

	sdp_item_list_t::Iterator it = m_sdp_item_list->Begin();
	for ( ; it; it++ ) {
		if ( (*it)->id == cs ) {
			if ( ! strcmp( url, (*it)->url ) ) {
				dbg("Found SDP description for '%s'\n", url );
				return (*it)->content;
			}
		}
	}

	/*! Item is not present.. */
	return NULL;
}


