
#ifndef _CONNECTION_H_
#define _CONNECTION_H_

#include <QObject>

class QTcpSocket;

class Connection : public QObject
{
	Q_OBJECT

public:
	Connection( QObject *parent, QTcpSocket *client );
	virtual ~Connection();
	
private slots:
	void readData();
	
private:
	QTcpSocket *socket;
};

#endif // _CONNECTION_H_
