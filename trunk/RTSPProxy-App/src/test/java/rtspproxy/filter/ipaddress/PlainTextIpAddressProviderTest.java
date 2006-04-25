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
package rtspproxy.filter.ipaddress;

import junit.framework.TestCase;

/**
 * @author Matteo Merli
 */
public class PlainTextIpAddressProviderTest extends TestCase
{
	// TODO: Rewrite this test using the xml based configuration

	private static final String CRLF = "\r\n";

	public void test1() throws Exception
	{
	/*
		// prepare
		SimpleIpAddressProvider provider = new SimpleIpAddressProvider();
		StringBuilder rules = new StringBuilder();
		rules.append( "Deny *" + CRLF );
		rules.append( "Allow 127.0.0.1" + CRLF );
		provider.loadRules( new StringReader( rules.toString() ) );

		// tests
		assertTrue( provider.isBlocked( InetAddress.getByName( "10.0.0.2" ) ) );
		assertFalse( provider.isBlocked( InetAddress.getByName( "127.0.0.1" ) ) );

		// close
		provider.shutdown();
	*/
	}
	
	public void test2() throws Exception
	{
	/*
		// prepare
		SimpleIpAddressProvider provider = new SimpleIpAddressProvider();
		StringBuilder rules = new StringBuilder();
		rules.append( "Allow *" + CRLF );
		rules.append( "Deny 10.0.0.13" + CRLF );
		provider.loadRules( new StringReader( rules.toString() ) );

		// tests
		assertTrue( provider.isBlocked( InetAddress.getByName( "10.0.0.13" ) ) );
		assertFalse( provider.isBlocked( InetAddress.getByName( "127.0.0.1" ) ) );

		// close
		provider.shutdown();
	*/
	}

}
