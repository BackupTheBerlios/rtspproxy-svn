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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import rtspproxy.Reactor;
import rtspproxy.config.AAAConfigurable;
import rtspproxy.config.Config;
import rtspproxy.filter.authentication.scheme.Credentials;

/**
 * @author Matteo Merli
 */
public class PlainTextAuthenticationProvider implements AuthenticationProvider, AAAConfigurable
{

	private static Logger log = Logger.getLogger( PlainTextAuthenticationProvider.class );

	private static Properties usersDb = new Properties();

	public void init() throws Exception
	{
		/*
		// Load users from file
		String fileName = Config.getHome() + File.separator
				+ Config.proxyFilterAuthenticationTextFile.getValue();

		try {
			usersDb.load( new FileInputStream( new File( fileName ) ) );

		} catch ( FileNotFoundException e ) {
			log.fatal( "Users file not found:" + e );
			Reactor.stop();
		} catch ( IOException e ) {
			log.fatal( "Error reading users file: " + e );
			Reactor.stop();
		} catch ( IllegalArgumentException e ) {
			log.fatal( "The users file is not valid" );
			Reactor.stop();
		}
		*/
	}

	public void shutdown() throws Exception
	{
		// Do nothing
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
		else
			// Password is wrong
			return false;
	}

	public void configure(List<Element> configElements) throws Exception {
		for(Element el : configElements) {
			if(el.getName().equals("user")) {
				Element nameEl = el.element("name");
				Element passwordEl = el.element("password");
				
				if(nameEl == null)
					throw new IllegalArgumentException("no name element available in user configuration");
				if(passwordEl == null)
					throw new IllegalArgumentException("no password element available in user configuration");
				
				String name = nameEl.getTextTrim();
				String password = passwordEl.getTextTrim();
				
				if(name == null || name.length() == 0)
					throw new IllegalArgumentException("invalid username given");
				if(password ==  null || password.length() == 0)
					throw new IllegalArgumentException("invalid password given");
				
					this.usersDb.put(name, password);
			}
		}
	}

}
