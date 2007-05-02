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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import rtspproxy.rtsp.RtspTransport.DeliveryType;

/**
 * @author Matteo Merli
 */
public class RtspTransportTest
{
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
    
    @Configuration(beforeTestMethod = true)
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
    
    @Test()
    public void isSupportedByProxy()
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
    @Test()
    public void testToString()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.isAppend()'
     */
    @Test()
    public void isAppend()
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
    @Test()
    public void testGetClientPort()
    {
        assertTrue( Arrays.equals( new int[] { 6972, 0 },
                                   transport1.getClientPort() ) );
        assertTrue( Arrays.equals( new int[] { 6972, 0 },
                                   transport2.getClientPort() ) );
        assertTrue( Arrays.equals( new int[] { 6972, 0 },
                                   transport3.getClientPort() ) );
        assertTrue( Arrays.equals( new int[] { 6972, 6973 },
                                   transport4.getClientPort() ) );
        assertTrue( Arrays.equals( new int[] { 0, 0 },
                                   transport5.getClientPort() ) );
        assertTrue( Arrays.equals( new int[] { 0, 0 },
                                   transport6.getClientPort() ) );
        assertTrue( Arrays.equals( new int[] { 0, 0 },
                                   transport7.getClientPort() ) );
        assertTrue( Arrays.equals( new int[] { 0, 0 },
                                   transport8.getClientPort() ) );
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.getDeliveryType()'
     */
    @Test()
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
    @Test()
    public void testGetInterleaved()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.setInterleaved(String)'
     */
    @Test()
    public void testSetInterleaved()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.getLayers()'
     */
    @Test()
    public void testGetLayers()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.setLayers(int)'
     */
    @Test()
    public void testSetLayers()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.getLowerTransport()'
     */
    @Test()
    public void testGetLowerTransport()
    {
        
    }
    
    /*
     * Test method for
     * 'rtspproxy.rtsp.RtspTransport.setLowerTransport(LowerTransport)'
     */
    @Test()
    public void testSetLowerTransport()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.getMode()'
     */
    @Test()
    public void testGetMode()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.setMode(String)'
     */
    @Test()
    public void testSetMode()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.getPort()'
     */
    @Test()
    public void testGetPort()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.setPort(int[])'
     */
    @Test()
    public void testSetPort()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.getProfile()'
     */
    @Test()
    public void testGetProfile()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.setProfile(Profile)'
     */
    @Test()
    public void testSetProfile()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.getServerPort()'
     */
    @Test()
    public void testGetServerPort()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.setServerPort(int[])'
     */
    @Test()
    public void testSetServerPort()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.getSSRC()'
     */
    @Test()
    public void testGetSSRC()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.setSSRC(String)'
     */
    @Test()
    public void testSetSSRCString()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.setSSRC(long)'
     */
    @Test()
    public void testSetSSRCLong()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.getTransportProtocol()'
     */
    @Test()
    public void testGetTransportProtocol()
    {
        
    }
    
    /*
     * Test method for
     * 'rtspproxy.rtsp.RtspTransport.setTransportProtocol(TransportProtocol)'
     */
    @Test()
    public void testSetTransportProtocol()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.getTTL()'
     */
    @Test()
    public void testGetTTL()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.setTTL(int)'
     */
    @Test()
    public void testSetTTL()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.setSource(String)'
     */
    @Test()
    public void testSetSource()
    {
        
    }
    
    /*
     * Test method for 'rtspproxy.rtsp.RtspTransport.getSource()'
     */
    @Test()
    public void testGetSource()
    {
        
    }
    
}
