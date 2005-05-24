AM_CXXFLAGS = $(cxx_define) $(debug)

bin_PROGRAMS = rtspproxy
rtspproxy_SOURCES = main.cpp proxysession.cpp proxytran.cpp rtspproxy.cpp tranhdr.cpp \
		    proxysession.h proxytran.h rtspproxy.h tranhdr.h

rtspproxy_LDADD = ../cache_manager/libcache_manager.a ../libapp/libapp.a \
		../librtsp/librtsp.a ../config/libconfig.a \
		../cache_play/libcache_play.a ../rtp/librtp.a \
		../cache_prefetching/libcache_prefetching.a \
		../reflector/libreflector.a \
		-lpthread -lm

INCLUDES = -I$(top_srcdir)/cache_manager -I$(top_srcdir)/libapp -I$(top_srcdir)/librtsp \
	   -I$(top_srcdir)/cache_play -I$(top_srcdir)/rtp -I$(top_srcdir)/config
