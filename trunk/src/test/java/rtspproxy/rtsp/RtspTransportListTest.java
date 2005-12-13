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

package rtspproxy.rtsp;

import junit.framework.TestCase;

/**
 * @author Matteo Merli
 */
public class RtspTransportListTest extends TestCase
{

	public static void main( String[] args )
	{
		junit.textui.TestRunner.run( RtspTransportListTest.class );
	}

	RtspTransportList transportList;
	String transportTest;

	@Override
	protected void setUp() throws Exception
	{
		transportTest = "x-real-rdt/mcast;client_port=6972;mode=play,"
				+ "x-real-rdt/udp;client_port=6972;mode=play,"
				+ "x-pn-tng/udp;client_port=6972;mode=play,"
				+ "RTP/AVP;unicast;client_port=6972-6973;mode=play,"
				+ "x-pn-tng/tcp;mode=play," + "x-real-rdt/tcp;mode=play,"
				+ "RTP/AVP/TCP;unicast;mode=play";
		transportList = new RtspTransportList( transportTest );
	}

	public void testToString()
	{
		String expected = "RTP/AVP/UDP;unicast;client_port=6972-6973;mode=play";
		assertEquals( expected, transportList.toString() );
	}

	public void testCount()
	{
		// only a transport type is valid in this test
		assertEquals( 1, transportList.count() );
	}

}
