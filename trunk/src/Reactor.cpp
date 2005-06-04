 
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
	qWarning() << "New connection in thread: " << client->peerAddress().toString() << ":" << client->peerPort();
	connect( client, SIGNAL( disconnected() ), 
			 client, SLOT( deleteLater() ) );
	
	new Connection( this, client );
}

