
#include <stdio.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>

#include "iniparser.h"

void error( const char* msg )
{
  fprintf( stderr, "Error: %s\n", msg );
  exit( -1 );
}


void eatup_comment( FILE* f )
{
  int c;
  while ( 1 ) {
	c = fgetc( f );
	if ( c == '\n' || c == EOF )
	  break;
  }
}

char* read_string( FILE* f)
{
  char buf[1024] = "";
  int i = 0;
  int c;
  while ( 1 ) {
	c = fgetc( f );
	if ( c == '"' || c == EOF )
	  break;
	buf[ i++ ] = c;
  }
  return strdup( buf );
}

int read_value( FILE* f )
{
  char buf[1024] = "";
  int i = 0;
  int c;
  int value = 0;
  
  while ( 1 ) {
	c = fgetc( f );
	if ( c == '"' ) 
	  return -1; /* Tells that the value is a string */
	  
	if ( (c >= 'a' && c <= 'z') || 
	   	 (c >= 'A' && c <= 'Z') ||
		 (c >= '0' && c <= '9')  ) {
	  buf[ i++ ] = c;
	  continue;
	}
	break;
  }
  
  if ( !strcasecmp(buf,"yes") ) {
	  value = 1;
  } else if ( !strcasecmp(buf, "no") ) {
	  value = 0;
  } else {
	  value = atoi( buf );
  }

  return value;
}

void read_equal( FILE* f )
{
  char c;
  int ok = 0;
  while ( 1 ) {
	c = fgetc( f );
	if ( c == ' ' || c == '\t' )
	  continue;
	if ( c == '=' ) {
	  if ( !ok ) {
		ok = 1;
		continue;
	  } else {
		/* two equal signs in expression */
		ok = 0;
		break;
	  }
	}
	break;
  }

  fseek( f, -1, SEEK_CUR );

  if ( !ok )
	error("A '=' sign is needed!");
}

char* read_name( FILE* f)
{
  char buf[1024] = "";
  int i = 0;
  char c;

  while ( 1 ) {
	c = fgetc( f );
	if ( (c >= 'a' && c <= 'z') ||
		 (c >= 'A' && c <= 'Z') || c == '_' ) {
	  buf[ i++ ] = c;
	  continue;
	}
	break;
  }

  fseek( f, -1, SEEK_CUR );
  return strdup( buf );
}

struct t_command* read_command( FILE* f )
{
  char *name = NULL;
  int value = 0;
  char *value_str = NULL;
  struct t_command *command = malloc( sizeof(struct t_command) );

  name = read_name( f );
  command->name = name;

  // printf("\nNome: '%s'\n", name);

  read_equal( f );

  value = read_value( f );
  if ( value == -1 ) {
	  /* Value is a string */
	  value_str = read_string( f );
	  // printf("Stringa('%s')\n", value_str );
    command->value_str = value_str;
    command->type = STRING;
  } else {
	  // printf("Value: %u\n", value );
    command->value_num = value;
    command->type = NUMERIC;
  }

  return command;
}

struct t_command* parse_file(FILE *f)
{
  int c;

  while ( (c=fgetc(f)) != EOF ) {
    switch ( c ) {

      case '#':
        eatup_comment( f );

      /* Ignore spaces */
      case ' ':
      case '\t':
      case '\n':
        break;

      default:
        fseek( f, -1, SEEK_CUR ); /* put back the char already read */
        return read_command( f );
    }
  }

  return (struct t_command*)NULL;
}


#ifdef DEBUG
int main(int argc, char** argv)
{
  FILE* f;

  if ( argc != 2 ) {
    printf("Usage: iniparser file.conf\n");
	  exit(-1);
  }

  f = fopen( argv[1], "r" );
  if ( ! f ) {
	perror( "Failed to opening file" );
	exit( -1 );
  }

  parse_file( f );

  fclose( f );
  exit( 0 );
}
#endif
