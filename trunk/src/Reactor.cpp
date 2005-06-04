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

#include <QtNetwork>
#include <QtCore>

/**
 * \class Reactor
 * 
 * \brief Accept incoming connection and dispatches client
 * to be served in threads.
 */
 
Reactor::Reactor( QObject *parent )
	: QThread( parent )
{
	tcpServer = new QTcpServer( this );
	// TODO: Read port number from config
	if ( ! tcpServer->listen( 5540 ) ) {
		qDebug() << "Unable to start the proxy: " << tcpServer->errorString();
		return;
	}
	
	connect( tcpServer, SIGNAL( newConnection() ), 
			this, SLOT( newConnection() ) );
}
 
Reactor::~Reactor()
{
}

void Reactor::newConnection()
{
	// Serve the connection in a new thread
	start();
}

void Reactor::run()
{
	QTcpSocket *client = tcpServer->nextPendingConnection();
	qWarning() << "New connection in thread: " << client->peerAddress().toString() 
			<< ":" << client->peerPort();
	connect( client, SIGNAL( disconnected() ), 
			 client, SLOT( deleteLater() ) );
	
	new Connection( this, client );
}

