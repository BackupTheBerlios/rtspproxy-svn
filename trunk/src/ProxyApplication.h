
#ifndef _PROXY_APPLICATION_H_
#define _PROXY_APPLICATION_H_

#include <QCoreApplication>

class ProxyApplication : public QCoreApplication
{
public:
	ProxyApplication( int& argc, char** argv );
	~ProxyApplication();

};

#endif _PROXY_APPLICATION_H_