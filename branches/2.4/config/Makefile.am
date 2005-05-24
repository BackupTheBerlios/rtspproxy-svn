
AM_CXXFLAGS = $(cxx_define) $(debug)
AM_CFLAGS = $(cxx_define) $(debug)

noinst_LIBRARIES = libconfig.a

# syntax.y lexer.l

libconfig_a_SOURCES =  iniparser.c iniparser.h config_parser.cpp \
		config_parser.h

INCLUDES = -I$(top_srcdir)/cache_manager -I$(top_srcdir)/libapp -I$(top_srcdir)/librtsp

