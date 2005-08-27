
AM_CXXFLAGS = $(cxx_define) $(debug)

noinst_LIBRARIES = librtsp.a

librtsp_a_SOURCES = pkt.cpp proxypkt.cpp rtspmsg.cpp rtspprot.cpp \
		  tran.cpp					\
		  pkt.h proxypkt.h rtspmsg.h rtspprot.h tran.h

INCLUDES = -I$(top_srcdir)/libapp
