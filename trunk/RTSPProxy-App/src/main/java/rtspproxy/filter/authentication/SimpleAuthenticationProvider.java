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

package rtspproxy.filter.authentication;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.config.StringParameter;
import rtspproxy.filter.authentication.scheme.Credentials;

/**
 * @author Matteo Merli
 */
public class SimpleAuthenticationProvider implements AuthenticationProvider, Observer
{

    private static Logger log = LoggerFactory
            .getLogger( SimpleAuthenticationProvider.class );

    private final StringParameter usersDbParameter;

    private final Properties usersDb = new Properties();

    public SimpleAuthenticationProvider()
    {
        usersDbParameter = new StringParameter( "filters.authentication.usersFile", // name
                "conf/user.properties", // default value
                true, // mutable
                "" );

        usersDbParameter.addObserver( this );
    }

    public void start() throws Exception
    {
        // Read user database
        try {
            String fileName = usersDbParameter.getValue();
            InputStream is = new FileInputStream( fileName );
            usersDb.load( is );

        } catch ( Exception e ) {
            log.error( "Error reading users DB: " + e );
        }
    }

    public void stop()
    {
        usersDb.clear();
    }

    public String getPassword( String username )
    {
        return usersDb.getProperty( username );
    }

    public boolean isAuthenticated( Credentials credentials )
    {
        String storedPassword = usersDb.getProperty( credentials.getUserName() );
        if ( storedPassword == null )
            // User is not present
            return false;

        if ( storedPassword.compareTo( credentials.getPassword() ) == 0 )
            // Password is ok
            return true;

        // Password is wrong
        return false;
    }

    public void configure( Configuration configuration )
    {
        usersDbParameter.readConfiguration( configuration );
    }

    public void update( Observable o, Object arg )
    {
        if ( o != usersDbParameter ) {
            log.debug( "Received notification of wrong object: {}", o );
            return;
        }

        try {
            stop();
            start();
        } catch ( Exception e ) {
            log.error( "Error restarting Authentication provider" );
        }
    }
}
