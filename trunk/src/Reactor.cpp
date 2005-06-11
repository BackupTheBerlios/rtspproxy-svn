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

#include "Reactor.h"
#include "Connection.h"
#include "Config.h"

#include <QtNetwork>
#include <QtCore>

/**
 * \class Reactor
 * 
 * \brief Accept incoming connection and dispatches client
 * to be served in threads.
 */
 
Reactor::Reactor( QObject *parent )
	: QTcpServer( parent )// , QThread( parent )
{
	int port = Config::getIntValue( "rtsp_port" );
	
	while ( ! this->listen( port++ ) ) {
		qWarning() << "Unable to start the proxy on port" << port-1 << ":" << this->errorString();
		///// QCoreApplication::exit( -1 );
	} // else {
		qDebug() << "Listening on port" << port-1;
	// }
}
 
Reactor::~Reactor()
{
}

void Reactor::incomingConnection( int socketDescriptor )
{
	connectionQueue.enqueue( socketDescriptor );
	// start();
	run();
}

void Reactor::run() 
{
	if ( connectionQueue.isEmpty() )
		return;
	
	int socket = connectionQueue.dequeue();
	qDebug() << "New connection";
	Connection *connection = new Connection( socket );
	
	// exec();
}

