
TEMPLATE = app
DESTDIR = .
SOURCES = Main.cpp ProxyApplication.cpp Reactor.cpp Connection.cpp
HEADERS = ProxyApplication.h Reactor.h Connection.h

DEFINES += QT_DLL

QT -= gui
QT += network
CONFIG += qt warn_on release console

include( rtsp/rtsp.pri )


