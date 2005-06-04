
#include "ProxyApplication.h"
#include "Reactor.h"

#include <iostream>

ProxyApplication::ProxyApplication( int &argc, char** argv )
	: QCoreApplication( argc, argv ) 
{
	std::cout << "Partito" << std::endl;
	
	Reactor *reactor = new Reactor( this );
}

ProxyApplication::~ProxyApplication()
{
}