
AM_CXXFLAGS = $(cxx_define) $(debug)

noinst_LIBRARIES = libcache_manager.a

libcache_manager_a_SOURCES= cache.cpp cache_items.cpp cache_segment.cpp cache_status.cpp \
		    time_range.cpp sdp.cpp \
	            cache.h cache_items.h cache_segment.h cache_status.h \
		    time_range.h sdp.h

INCLUDES = -I$(top_srcdir)/libapp -I$(top_srcdir)/librtsp -I$(top_srcdir)/rtspproxy \
	-I$(top_srcdir)/cache_play -I$(top_srcdir)/config -I$(top_srcdir)/rtp


