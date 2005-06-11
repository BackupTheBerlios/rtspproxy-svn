
TEMPLATE = app
DESTDIR = .
SOURCES = Main.cpp ProxyApplication.cpp Reactor.cpp Connection.cpp Config.cpp
HEADERS = ProxyApplication.h Reactor.h Connection.h Config.h

DEFINES += QT_DLL

QT -= gui
QT += network
CONFIG += qt warn_on debug console

include( rtsp/rtsp.pri )


