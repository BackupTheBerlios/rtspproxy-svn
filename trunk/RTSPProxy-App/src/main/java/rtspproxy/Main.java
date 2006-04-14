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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import rtspproxy.config.Config;
import rtspproxy.config.XMLConfigReader;
import rtspproxy.lib.Exceptions;

/**
 * 
 */
public class Main
{

    static Logger log = Logger.getLogger( "rtspproxy" );

    public static void main( String[] args )
    {
        // Configure the logger with default settings
        // useful to track pre-config file errors
        BasicConfigurator.configure();

        // Register the signal handler
        Runtime.getRuntime().addShutdownHook( new ShutdownHandler() );

        try {
            // Read configuration files
            new Config();

            // Log4J configuration
            List<String> log4jList = new ArrayList<String>();

            // system wide configuration (typical on un*x-like systems)
            log4jList.add( "/etc/rtspproxy.log4j." );

            // Per-user configuration
            log4jList.add( System.getProperty( "user.home", "" ) + "/.rtspproxy.log4j." );

            // RtspProxy home folder
            if ( Config.getHome() != null )
                log4jList.add( Config.getHome() + "/conf/rtspproxy.log4j." );

            // Current directory configuration
            log4jList.add( "rtspproxy.log4j." );

            // Used for testing purposes:
            // checks for the configuration file
            log4jList.add( "src/resources/conf/rtspproxy.log4j." );

            for ( String path : log4jList ) {
                File propFile = new File( path + "properties" );
                File xmlFile = new File( path + "xml" );

                if ( propFile.canRead() ) {
                    PropertyConfigurator.configure( propFile.toURL() );
                    
                } else if ( xmlFile.canRead() ) {
                    DOMConfigurator.configure( xmlFile.toURL() );
                }
            }

            List<String> pathlist = new ArrayList<String>();

            // System wide configuration (tipical in unix systems)
            pathlist.add( "/etc/rtspproxy.conf.xml" );

            // Per user config
            pathlist.add( System.getProperty( "user.home", "" ) + "/.rtspproxy.conf.xml" );

            // RtspProxy home folder
            if ( Config.getHome() != null )
                pathlist.add( Config.getHome() + "/conf/rtspproxy.conf.xml" );

            // Current directory configuration
            pathlist.add( "rtspproxy.conf.xml" );

            // Used for testing purposes:
            // checks for the configuration file
            pathlist.add( "src/resources/conf/rtspproxy.conf.xml" );

            XMLConfigReader configReader = new XMLConfigReader();

            for ( String path : pathlist ) {
                configReader.readConfig( path );
            }

            if ( log.isDebugEnabled() ) {
                log.debug( Config.debugParameters() );
            }

            Reactor.setStandalone( true );
            Reactor.start();

        } catch ( Exception e ) {
            log.fatal( "Exception in the reactor: ", e );
            Exceptions.logStackTrace( e );
            System.exit( -1 );
        }
    }

}
