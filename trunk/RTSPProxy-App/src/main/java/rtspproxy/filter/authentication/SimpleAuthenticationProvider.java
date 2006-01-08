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

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import rtspproxy.config.AAAConfigurable;
import rtspproxy.filter.GenericProviderAdapter;
import rtspproxy.filter.authentication.scheme.Credentials;

/**
 * @author Matteo Merli
 */
public class SimpleAuthenticationProvider extends GenericProviderAdapter
		implements AuthenticationProvider, AAAConfigurable {

	private static Logger log = Logger
			.getLogger(SimpleAuthenticationProvider.class);

	private Properties usersDb = new Properties();

	public String getPassword(String username) {
		return usersDb.getProperty(username);
	}

	public boolean isAuthenticated(Credentials credentials) {
		String storedPassword = usersDb.getProperty(credentials.getUserName());
		if (storedPassword == null)
			// User is not present
			return false;

		if (storedPassword.compareTo(credentials.getPassword()) == 0)
			// Password is ok
			return true;
		else
			// Password is wrong
			return false;
	}

	public void configure(List<Element> configElements) throws Exception {
		for (Element el : configElements) {
			if (el.getName().equals("user")) {
				Element nameEl = el.element("name");
				Element passwordEl = el.element("password");

				if (nameEl == null)
					throw new IllegalArgumentException(
							"no name element available in user configuration");
				if (passwordEl == null)
					throw new IllegalArgumentException(
							"no password element available in user configuration");

				String name = nameEl.getTextTrim();
				String password = passwordEl.getTextTrim();

				if (name == null || name.length() == 0)
					throw new IllegalArgumentException("invalid username given");
				if (password == null || password.length() == 0)
					throw new IllegalArgumentException("invalid password given");

				log.debug("adding user " + name + " with password " + password);
				this.usersDb.put(name, password);
			}
		}
	}

}
