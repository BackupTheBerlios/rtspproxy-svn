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

import org.testng.annotations.Test;

/**
 * @author Matteo Merli
 */
public class RtspTransportListTest
{
    String transportTest =
            "x-real-rdt/mcast;client_port=6972;mode=play,"
                    + "x-real-rdt/udp;client_port=6972;mode=play,"
                    + "x-pn-tng/udp;client_port=6972;mode=play,"
                    + "RTP/AVP;unicast;client_port=6972-6973;mode=play,"
                    + "x-pn-tng/tcp;mode=play," + "x-real-rdt/tcp;mode=play,"
                    + "RTP/AVP/TCP;unicast;mode=play";
    
    RtspTransportList transportList = new RtspTransportList( transportTest );
    
    @Test
    public void ToString()
    {
        String expected =
                "x-real-rdt/udp;client_port=6972;mode=\"PLAY\","
                        + "RTP/AVP/UDP;unicast;client_port=6972-6973;mode=\"PLAY\"";
        assertEquals( expected, transportList.toString() );
    }
    
    @Test
    public void count()
    {
        // only a transport type is valid in this test
        assertEquals( 2, transportList.count() );
    }
    
}
