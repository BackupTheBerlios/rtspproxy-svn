TEMPLATE = app
DESTDIR = .
SOURCES = RtspTransport.cpp
HEADERS = RtspTransport.h

DEFINES += QT_DLL TEST_RTSP_TRANSPORT

QT -= gui
CONFIG += qt warn_on debug console
