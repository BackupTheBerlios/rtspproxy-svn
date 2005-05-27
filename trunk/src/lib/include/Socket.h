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

#ifndef _SOCKET_H_
#define _SOCKET_H_

#include <sys/types.h>

class Timeout;

class Socket
{
public:

	enum Type {
		TCP,		///< TCP IPv4 Address
		TCP6,		///< TCP IPv6 Address
		UDP,		///< UDP IPv4 Address
		UDP6,		///< UDP IPv6 Address
		LOCALSTREAM,	///< Unix Domain TCP
		LOCALDGRAM	///< Unix Domain UDP
    };

	/** 
	 * Creates a new socket with given type.
	 * 
	 * @param type Type of the socket: TCP, UDP...
	 */
	Socket( Type type );
	
	Socket( );
	
	/** Destructor */
	virtual ~Socket();
	
	/** 
	 * Read data from the socket and place it into the user buffer. If the given
	 * timeout is not a null timeout then this method will only wait for data
	 * until the timeout expires, at which time it will return -1. If you give a
	 * null timeout, this method will block until data arives.
	 *
	 * @param buffer The user buffer to write data into
	 * @param length The max bytes to place into buffer
	 * @param timeout How long to wait for data to arive
	 * @return The number of bytes read from the socket, or -1 for timeout
	 */
	int read( void *buffer, u_int32_t length, const Timeout &timeout );
	
	/** 
	 * Write data from a user buffer to the socket. If the given timeout
	 * is not a null timeout then this method will only wait for the
	 * socket to become writable until the timeout expires, at which
	 * point it will return -1. If you give a null timeout then this
	 * method will block until all data has been written to the socket.
	 *
	 * @param buffer The user buffer to read from
	 * @param length The number of bytes to read from the user buffer
	 * @return The number of bytes written, -1 for timeout
	 */
	int write (const void *buffer, u_int32_t length, const Timeout &timeout);
	
	/** 
	 * Close the socket.
	 */
	void close();
	
	/**
	 * Return the internal socket
	 */
	int getSocketFD() const { return m_sockfd; }
	
	/**
	 * Set the internal socket descriptor.
	 * 
	 * @param sockfd a socket descriptor
	 */
	void setSocketFD( int sockfd );
	
	/**
	 * Test the state of the socket.
	 * 
	 * @return true if socket is closed
	 */
	bool operator! (void) const;
	
private:

	void setDomainType( Type type ); 

	int m_sockfd;	
	int m_domain;
	int m_sock_type;
	bool m_type_ready;
    Type m_type;
};

bool operator== (const Socket &first, const Socket &second);
bool operator!= (const Socket &first, const Socket &second);

#endif //_SOCKET_H_
