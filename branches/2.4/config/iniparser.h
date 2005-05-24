

#ifndef INIPARSER_H
#define INIPARSER_H

#define NUMERIC 0
#define STRING 1

struct t_command {
	char* name;
	int value_num;
	char* value_str;
  char type;
};

#ifdef __cplusplus
  extern "C" {
#endif

struct t_command* parse_file( FILE* f );

#ifdef __cplusplus
  }
#endif

#endif
