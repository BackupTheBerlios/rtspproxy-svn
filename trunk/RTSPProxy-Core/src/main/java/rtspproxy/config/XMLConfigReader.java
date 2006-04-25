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
package rtspproxy.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a parser for XML configuration files.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 */
public final class XMLConfigReader
{

    private static final Logger log = LoggerFactory.getLogger( XMLConfigReader.class );

   
    private static final XMLConfiguration configuration = new XMLConfiguration(); 


    /**
     * read the configuration file.
     * 
     * @param fileName
     *            the pathname of the configuration file
     * @exception IOException
     *                the file denoted by the file name cannot be read
     * @throws DocumentException
     *             parsing the config file failed.
     */
    public void readConfig( String fileName ) throws FileNotFoundException
    {
        log.debug( "Reading configuration file='{}'", fileName );
        File file = new File( fileName );

        if ( file.canRead() ) {
            readConfig( new BufferedReader( new FileReader( file ) ) );
        }
    }

    /**
     * read the configuration file
     * 
     * @param is
     *            the input stream to read the configuration from.
     */
    public void readConfig( Reader reader )
    {
        try {
            configuration.load( reader );
        } catch ( ConfigurationException e ) {
            log.error( "Error reading configuration file." );
            throw new RuntimeException( e );
        }

        for ( Parameter param : Config.getAllParameters() ) {
            try {
                param.readConfiguration( configuration );
                log.debug( "Parameter value: {}", param.getStringValue() );

            } catch ( NoSuchElementException e ) {
                log.debug( "Parameter NOT found: '{}'. Using default value: {}", param
                        .getName(), param.getStringValue() );

            }
        }

        Config.updateDebugSettings();
    }
    
    public static Configuration getConfiguration()
    {
    	return configuration;    
    }
}
