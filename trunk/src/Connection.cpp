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
 
#include "Connection.h"

#include <QtNetwork>
#include <QtCore>

#include "rtsp/RtspProtocol.h"

Connection::Connection( int socketDescriptor )
	: QObject()
{
	this->socketDescriptor = socketDescriptor;
	
	socket = new QTcpSocket( this );
	socket->setSocketDescriptor( socketDescriptor );
	
	qDebug() << "New connection in thread: " 
	<< socket->peerAddress().toString() 
			<< ":" << socket->peerPort();
	
	// TODO: remove	
	RtspProtocol *rtsp = new RtspProtocol( socket );
			
	connect( socket, SIGNAL( disconnected() ), socket, SLOT( deleteLater() ) );
	connect( socket, SIGNAL( readyRead() ), rtsp, SLOT( readData() ) );
}

Connection::~Connection()
{
	socket->close();
	delete socket;
}

void Connection::readData()
{
	QByteArray a = socket->readAll();
	if ( a.size() ) {
		qWarning() << "Read:" << a;
		
		// Echo message
		socket->write( a );
	}
}
