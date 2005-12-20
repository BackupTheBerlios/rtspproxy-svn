package rtspproxy.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ConfigReader
{
	private static Logger log = Logger.getLogger( ConfigReader.class );

	public ConfigReader( String fileName ) throws IllegalArgumentException
	{
		Properties properties = new Properties();
		
		File file = new File( fileName );

		try {
			InputStream is = new FileInputStream( file );
			properties.load( is );
		} catch ( FileNotFoundException e ) {
			// silently ignore
			return;
		} catch ( IOException e ) {
			log.error( "Error reading configuration file: " + e );
			return;
		}

		// cycle throuh all the properties
		for ( Object key : properties.keySet() ) {
			String name = (String) key;
			String value = properties.getProperty( name );

			Parameter parameter = Config.getParameter( name );
			if ( parameter == null ) {
				// The property name is invalid
				log.fatal( "Invalid parameter name: " + name );
				throw new IllegalArgumentException();
			}

			try {
				parameter.setValue( value );
			} catch ( IllegalArgumentException e ) {
				log.fatal( "Invalid value for parameter " + name + ": " + value );
				throw e;
			}
		}

		Config.updateDebugSettings();

		log.debug( "Reading configurations from '" + file + "'" );

	}
}
