
AM_CXXFLAGS = $(cxx_define)
AM_CFLAGS = $(cxx_define)

noinst_LIBRARIES = libcache_prefetching.a

libcache_prefetching_a_SOURCES = \
			prefetching.cpp rtsp_session.cpp \
			prefetching.h rtsp_session.h

INCLUDES = -I$(top_srcdir)/libapp -I$(top_srcdir)/librtsp \
	   -I$(top_srcdir)/rtspproxy -I$(top_srcdir)/rtp \
	   -I$(top_srcdir)/cache_manager -I$(top_srcdir)/config


