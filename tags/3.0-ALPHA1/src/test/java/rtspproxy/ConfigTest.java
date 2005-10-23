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

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author Matteo Merli
 */
public class ConfigTest extends TestCase
{

	public static void main( String[] args )
	{
		junit.textui.TestRunner.run( ConfigTest.class );
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		new Config();
	}

	/*
	 * Test method for 'rtspproxy.Config.get(String, String)'
	 */
	public final void testGet()
	{
		Config.set( "testKey", "testValue" );

		Assert.assertEquals( Config.get( "testKey", null ), "testValue" );
		Assert.assertEquals( Config.get( "notPresentKey", null ), null );
	}

	/*
	 * Test method for 'rtspproxy.Config.getInt(String, int)'
	 */
	public final void testGetInt()
	{
		int value = 12345678;
		Config.setInt( "testKeyInt", value );

		Assert.assertEquals( Config.getInt( "testKeyInt", 0 ), value );
	}

	/*
	 * Test method for 'rtspproxy.Config.getIntArray(String, int)'
	 */
	public final void testGetIntArray()
	{
		int values[] = new int[] { 23, 4, 5, 62, -43, 23 };
		Config.setIntArray( "testIntArray", values );

		int results[] = Config.getIntArray( "testIntArray", 0 );

		if ( results.length != values.length )
			Assert.assertTrue( false );

		for ( int i = 0; i < values.length; i++ ) {
			if ( values[i] != results[i] )
				Assert.assertTrue( false );
		}
	}

	/*
	 * Test method for 'rtspproxy.Config.getBoolean(String, boolean)'
	 */
	public final void testGetBoolean()
	{
		Config.setBoolean( "testTrue", true );
		Config.setBoolean( "testFalse", false );

		Assert.assertTrue( Config.getBoolean( "testTrue", false ) );
		Assert.assertFalse( Config.getBoolean( "testFalse", false ) );
	}

}
