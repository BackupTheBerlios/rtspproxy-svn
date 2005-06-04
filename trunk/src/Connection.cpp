
#include "Connection.h"

#include <QtNetwork>
#include <QtCore>

Connection::Connection( QObject *parent, QTcpSocket *client )
	: QObject( parent )
{
	socket = client;
	connect( socket, SIGNAL( readyRead() ), this, SLOT( readData() ) );
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