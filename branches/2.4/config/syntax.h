#ifndef BISON_SYNTAX_TAB_H
# define BISON_SYNTAX_TAB_H

# ifndef YYSTYPE
#  define YYSTYPE int
#  define YYSTYPE_IS_TRIVIAL 1
# endif
# define	EQUAL	257
# define	NUMBER	258
# define	STRING	259
# define	YES	260
# define	NO	261
# define	TOK_DEBUG	262
# define	TOK_PORT	263
# define	TOK_CACHE_ENABLE	264
# define	TOK_CACHE_MAX_SIZE	265
# define	TOK_LOG_TO_FILE	266
# define	TOK_LOG_FILE	267
# define	TOK_DEAMON_MODE	268
# define	UNKNOWN	269
# define	TOK_CACHE_DIR	270


extern YYSTYPE yylval;

#endif /* not BISON_SYNTAX_TAB_H */

/** LOG **
 *
 * $Log: syntax.h,v $
 * Revision 1.2  2003/11/17 16:13:56  mat
 * make-up
 *
 *
 */

