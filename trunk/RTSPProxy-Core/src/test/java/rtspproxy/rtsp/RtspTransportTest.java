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

import java.util.Arrays;

import junit.framework.TestCase;
import rtspproxy.rtsp.RtspTransport.DeliveryType;

/**
 * @author Matteo Merli
 */
public class RtspTransportTest extends TestCase
{

	public static void main( String[] args )
	{
		junit.textui.TestRunner.run( RtspTransportTest.class );
	}

	String test1 = "x-real-rdt/mcast;client_port=6972;mode=play";
	String test2 = "x-real-rdt/udp;client_port=6972;mode=play";
	String test3 = "x-pn-tng/udp;client_port=6972;mode=play";
	String test4 = "RTP/AVP;unicast;client_port=6972-6973;mode=play";
	String test5 = "x-pn-tng/tcp;mode=play";
	String test6 = "x-real-rdt/tcp;mode=play";
	String test7 = "RTP/AVP/TCP;unicast;mode=play";
    String test8 = "RTP/AVP/TCP;UNICAST;mode=play";

	RtspTransport transport1;
	RtspTransport transport2;
	RtspTransport transport3;
	RtspTransport transport4;
	RtspTransport transport5;
	RtspTransport transport6;
	RtspTransport transport7;
    RtspTransport transport8;

	@Override
	protected void setUp() throws Exception
	{
		transport1 = new RtspTransport( test1 );
		transport2 = new RtspTransport( test2 );
		transport3 = new RtspTransport( test3 );
		transport4 = new RtspTransport( test4 );
		transport5 = new RtspTransport( test5 );
		transport6 = new RtspTransport( test6 );
		transport7 = new RtspTransport( test7 );
        transport8 = new RtspTransport( test8 );
	}

	public void testIsSupportedByProxy()
	{
		assertEquals( false, transport1.isSupportedByProxy() );
		assertEquals( true, transport2.isSupportedByProxy() );
		assertEquals( false, transport3.isSupportedByProxy() );
		assertEquals( true, transport4.isSupportedByProxy() );
		assertEquals( false, transport5.isSupportedByProxy() );
		assertEquals( false, transport6.isSupportedByProxy() );
		assertEquals( false, transport7.isSupportedByProxy() );
        assertEquals( false, transport8.isSupportedByProxy() );
	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.toString()'
	 */
	public void testToString()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.isAppend()'
	 */
	public void testIsAppend()
	{
		assertEquals( false, transport1.isAppend() );
		assertEquals( false, transport2.isAppend() );
		assertEquals( false, transport3.isAppend() );
		assertEquals( false, transport4.isAppend() );
		assertEquals( false, transport5.isAppend() );
		assertEquals( false, transport6.isAppend() );
		assertEquals( false, transport7.isAppend() );
        assertEquals( false, transport8.isAppend() );
	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.getClientPort()'
	 */
	public void testGetClientPort()
	{
		assertTrue( Arrays.equals( new int[] { 6972, 0 }, transport1.getClientPort() ) );
		assertTrue( Arrays.equals( new int[] { 6972, 0 }, transport2.getClientPort() ) );
		assertTrue( Arrays.equals( new int[] { 6972, 0 }, transport3.getClientPort() ) );
		assertTrue( Arrays.equals( new int[] { 6972, 6973 }, transport4.getClientPort() ) );
		assertTrue( Arrays.equals( new int[] { 0, 0 }, transport5.getClientPort() ) );
		assertTrue( Arrays.equals( new int[] { 0, 0 }, transport6.getClientPort() ) );
		assertTrue( Arrays.equals( new int[] { 0, 0 }, transport7.getClientPort() ) );
        assertTrue( Arrays.equals( new int[] { 0, 0 }, transport8.getClientPort() ) );
	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.getDeliveryType()'
	 */
	public void testGetDeliveryType()
	{
		assertEquals( DeliveryType.unicast, transport1.getDeliveryType() );
		assertEquals( DeliveryType.unicast, transport2.getDeliveryType() );
		assertEquals( DeliveryType.None, transport3.getDeliveryType() );
		assertEquals( DeliveryType.unicast, transport4.getDeliveryType() );
		assertEquals( DeliveryType.None, transport5.getDeliveryType() );
		assertEquals( DeliveryType.unicast, transport6.getDeliveryType() );
		assertEquals( DeliveryType.unicast, transport7.getDeliveryType() );
        assertEquals( DeliveryType.unicast, transport8.getDeliveryType() );
	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.getInterleaved()'
	 */
	public void testGetInterleaved()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.setInterleaved(String)'
	 */
	public void testSetInterleaved()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.getLayers()'
	 */
	public void testGetLayers()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.setLayers(int)'
	 */
	public void testSetLayers()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.getLowerTransport()'
	 */
	public void testGetLowerTransport()
	{

	}

	/*
	 * Test method for
	 * 'rtspproxy.rtsp.RtspTransport.setLowerTransport(LowerTransport)'
	 */
	public void testSetLowerTransport()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.getMode()'
	 */
	public void testGetMode()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.setMode(String)'
	 */
	public void testSetMode()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.getPort()'
	 */
	public void testGetPort()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.setPort(int[])'
	 */
	public void testSetPort()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.getProfile()'
	 */
	public void testGetProfile()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.setProfile(Profile)'
	 */
	public void testSetProfile()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.getServerPort()'
	 */
	public void testGetServerPort()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.setServerPort(int[])'
	 */
	public void testSetServerPort()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.getSSRC()'
	 */
	public void testGetSSRC()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.setSSRC(String)'
	 */
	public void testSetSSRCString()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.setSSRC(long)'
	 */
	public void testSetSSRCLong()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.getTransportProtocol()'
	 */
	public void testGetTransportProtocol()
	{

	}

	/*
	 * Test method for
	 * 'rtspproxy.rtsp.RtspTransport.setTransportProtocol(TransportProtocol)'
	 */
	public void testSetTransportProtocol()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.getTTL()'
	 */
	public void testGetTTL()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.setTTL(int)'
	 */
	public void testSetTTL()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.setSource(String)'
	 */
	public void testSetSource()
	{

	}

	/*
	 * Test method for 'rtspproxy.rtsp.RtspTransport.getSource()'
	 */
	public void testGetSource()
	{

	}

}
