
#ifndef _REACTOR_H_
#define _REACTOR_H_

#include <QThread>

class QTcpServer;

class Reactor : public QThread
{
	Q_OBJECT
	
public:
	Reactor( QObject *parent );
	virtual ~Reactor();
	
private slots:
	void newConnection();
	
private:

	void run();

	QTcpServer *tcpServer;
};

#endif // _REACTOR_H_