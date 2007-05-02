/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   Copyright (C) 2005 - Matteo Merli - matteo.merli@gmail.com            *
 *                                                                         *
 ***************************************************************************/

/*
 * $Id$
 * 
 * $URL$
 * 
 */

package rtspproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import rtspproxy.config.Config;
import rtspproxy.lib.Exceptions;

/**
 * The thread holded by this class is started in the shutdown phase.
 * 
 * @author Matteo Merli
 */
public class ShutdownHandler extends Thread
{
    
    private static Logger log = LoggerFactory.getLogger( ShutdownHandler.class );
    
    @Inject
    private IReactor reactor;
    
    @Override
    public void run()
    {
        log.info( "Shutting down" );
        try
        {
            log.info( "Stopping {} {}", Config.getName(), Config.getVersion() );
            reactor.stop();
            
        } catch ( Exception e )
        {
            log.error( "Exception in the reactor: ", e );
            Exceptions.logStackTrace( e );
        }
    }
}
