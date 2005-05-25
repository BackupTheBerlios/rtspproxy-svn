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

#include <string>

#include <sys/types.h>
#include <sys/socket.h>
#include <errno.h>

#include "Socket.h"

#define INVALID_SOCKET -1

Socket::Socket()
	: m_sockfd( INVALID_SOCKET ), m_type_ready(false)
{
}

Socket::Socket (Type type)
    : m_sockfd(INVALID_SOCKET), m_type_ready(true), 
    		m_type(type)
{
    int socket_domain=0;
    int socket_type=0, socket_fd=0;

    setDomainType( type );

    if ( (socket_fd = socket( m_domain, m_sock_type, 0)) < 0) {
		std::string error("failure from socket(2): ");
		error += strerror( errno );
		throw error;
	}

	m_sockfd = socket_fd;
}


Socket::~Socket()
{
}

int Socket::read( void *buffer, u_int32_t length, const Timeout &timeout) 
{
	int rc;

	void *buffer_ptr( buffer );

	for (;;) {
		//if (timeout && !readable(timeout)) 
		// 	return -1;

		if ( (rc = recv( m_sockfd, buffer_ptr, length, 0)) < 0) {
			
			if ( errno == EWOULDBLOCK )
				errno = EAGAIN;

			switch ( errno ) {
				case ECONNRESET:
					return 0;

				case EINTR:
					continue;

				case EAGAIN:
					return -1;

				default: {
					std::string error("read failure: ");
					error += strerror( errno );
					throw error;
				}
			}
		}

		break;
    }

	return rc;
}


int Socket::write( const void *buffer, u_int32_t length, const Timeout &timeout) 
{
	const char *buffer_ptr = static_cast<const char*>(buffer);
	int rc, bytes_written=0;

	while (length) {
		// if (timeout && !writable(timeout)) 
		//	return -1;

		if ( (rc = send( m_sockfd, buffer_ptr, length, 0)) < 0) {
			
			if ( errno == EWOULDBLOCK )
				errno = EAGAIN;

			switch ( errno ) {
				case EPIPE:
				case ECONNRESET:
					return 0;

				case EINTR:
					continue;

				case EAGAIN:
					return -1;

				default: {
					std::string error("write failed: ");
					error += strerror( errno );
					throw error;
				}
			}
		}

		buffer_ptr    += rc;
		bytes_written += rc;
		length        -= rc;
	}

	return bytes_written;
}

void Socket::close() 
{
	if ( m_sockfd !=  INVALID_SOCKET ) {
		::close( m_sockfd );
		m_sockfd = INVALID_SOCKET;
    }
}

void Socket::setSocketFD( int sockfd )
{
	close();
	m_sockfd = sockfd;
}

void Socket::setDomainType( Type type ) 
{
	switch ( type ) {
		case TCP:
			m_domain = PF_INET;
			m_sock_type	= SOCK_STREAM;
			break;

	    case UDP:
			m_domain	= PF_INET;
			m_sock_type	= SOCK_DGRAM;
			break;

#ifdef USE_INET6
	    case TCP6:
			m_domain	= PF_INET6;
			m_sock_type	= SOCK_STREAM;
			break;

	    case UDP6:
			m_domain	= PF_INET6;
			m_sock_type	= SOCK_DGRAM;
			break;
#endif // USE_INET6

	    default:
			m_domain = PF_INET;
			m_sock_type	= SOCK_STREAM;
			break;
	}
}


bool Socket::operator! () const 
{
	return m_sockfd == INVALID_SOCKET;
}

bool operator== (const Socket &first, const Socket &second) 
{
	return first.getSocketFD() == second.getSocketFD();
}

bool operator!= (const Socket &first, const Socket &second) 
{
	return !(first == second);
}

