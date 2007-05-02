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

package rtspproxy.rtp.rtcp;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.apache.mina.common.ByteBuffer;
import org.testng.annotations.Test;

import rtspproxy.lib.number.UnsignedInt;

/**
 * @author mat
 */
public class RtcpPacketTest
{

    /*
	 * Test method for 'rtspproxy.Config.get(String, String)'
	 */
	@Test
    public void rtcpPacket()
	{
		/* Construct a new dummy packet */
		RtcpPacket packet = new RtcpPacket();
		packet.version = 2;
		packet.padding = true;
		packet.count = 4;
		packet.packetType = RtcpPacket.Type.SDES.getValue();
		packet.length = 4;
		packet.setSsrc( new UnsignedInt( 0xADADADADADL ) );
		byte[] random_data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
		packet.packetBuffer = random_data;

		/* Convert it to a ByteBuffer */
		ByteBuffer buffer = packet.toByteBuffer();

		/* Recreate a RtcpPacket from buffer */
		RtcpPacket packet2 = new RtcpPacket( buffer );

        /* Compare the two packets */
		assertEquals( packet.version, packet2.version );
		assertEquals( packet.padding, packet2.padding );
		assertEquals( packet.count, packet2.count );
		assertEquals( packet.packetType, packet2.packetType );
		assertEquals( packet.length, packet2.length );
		assertEquals( packet.ssrc, packet2.ssrc );
		assertTrue( Arrays.equals( packet.packetBuffer, packet2.packetBuffer ) );
	}

}
