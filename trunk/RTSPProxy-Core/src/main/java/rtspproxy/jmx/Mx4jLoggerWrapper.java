package rtspproxy.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple wrapper to log mx4j logging info into slf4j subsystem
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 * 
 */
public class Mx4jLoggerWrapper extends mx4j.log.Logger
{

    private Logger logger;

    /*
     * (non-Javadoc)
     * 
     * @see mx4j.log.Logger#log(int, java.lang.Object, java.lang.Throwable)
     */
    @Override
    protected void log( int level, Object msg, Throwable t )
    {
        switch ( level )
        {
        case mx4j.log.Logger.DEBUG:
            logger.debug( msg.toString(), t );
            break;
        case mx4j.log.Logger.ERROR:
            logger.error( msg.toString(), t );
            break;
        case mx4j.log.Logger.FATAL:
            logger.error( msg.toString(), t );
            break;
        case mx4j.log.Logger.INFO:
            logger.info( msg.toString(), t );
            break;
        case mx4j.log.Logger.TRACE:
            logger.debug( msg.toString(), t );
            break;
        case mx4j.log.Logger.WARN:
            logger.warn( msg.toString(), t );
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see mx4j.log.Logger#setCategory(java.lang.String)
     */
    @Override
    protected void setCategory( String category )
    {
        super.setCategory( category );
        logger = LoggerFactory.getLogger( category );
    }
}
