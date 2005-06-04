/**************************************************************************
 *   Copyright (C) 2005 Matteo Merli <matteo.merli@gmail.com>
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
 * 
 *   $URL$
 * 
 *****************************************************************************/

#ifndef _HOST_ADDRESS_H_
#define _HOST_ADDRESS_H_

#include <sys/types.h>

#include "Socket.h"
#include "String.h"

class HostAddress
{
public:
	enum SpecialAddress {
		Null,
		Broadcast,
		LocalHost,
		LocalHostIPv6,
		Any,
		AnyIPv6
	};
	
	/** Default Constructor */
	HostAddress();
	
	/** Construct an IPv4 address */
	HostAddress( uint32_t ip4Addr );
	
	/** Construct an address using string representation */
	HostAddress( const String& address );
	
	/** Construct using a predefined address */
	HostAddress( SpecialAddress address );
	
	/** Copy Constructor */
	HostAddress( const HostAddress& other );
	
	/** Destructor */
	~HostAddress();
	
	void setAddress( uint32_t ip4Addr );
	bool setAddress( const String& address );
	
	String toString() const;
	
private:

	bool parse();
	
	void clear();

	uint32_t m_address;
	Socket::NetworkLayerProtocol m_protocol;
	
	bool m_isParsed;
	String m_ipString;
	
};

#endif // _HOST_ADDRESS_H_
