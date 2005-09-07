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

package rtspproxy.rtp;

import java.util.Arrays;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.mina.common.ByteBuffer;

/**
 * @author mat
 */
public class RtpPacketTest extends TestCase
{

	public static void main( String[] args )
	{
		junit.textui.TestRunner.run( RtpPacketTest.class );
	}

	public void testRtpPacket()
	{
		// Set the values
		byte version = 2;
		boolean padding = true;
		boolean extension = false;
		byte csrcCount = 3;
		boolean marker = false;
		int payloadType = 123;
		short sequence = 1234;
		int timestamp = 1231234;
		long ssrc = 43212L;
		int csrc[] = { 8, 8, 8 };
		byte payload[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };

		/* Construct a packet */
		RtpPacket p1 = new RtpPacket();
		p1.setVersion( version );
		p1.setPadding( padding );
		p1.setExtension( extension );
		p1.setCsrcCount( csrcCount );
		p1.setMarker( marker );
		p1.setPayloadType( payloadType );
		p1.setSequence( sequence );
		p1.setTimestamp( timestamp );
		p1.setSsrc( ssrc );
		p1.setCsrc( csrc );
		p1.setPayload( payload );

		/* Convert it to a ByteBuffer */
		ByteBuffer buffer = p1.toByteBuffer();
		System.err.println( "Buffer1: " + buffer );

		/* Reconvert it to a RtpPacket object */
		RtpPacket p2 = new RtpPacket( buffer );

		/* Test that they are equals */
		System.err.println( "Buffer2: " + p2.toByteBuffer() );
		Assert.assertEquals( p1.getVersion(), p2.getVersion() );
		Assert.assertEquals( p1.isPadding(), p2.isPadding() );
		Assert.assertEquals( p1.isExtension(), p2.isExtension() );
		Assert.assertEquals( p1.getCsrcCount(), p2.getCsrcCount() );
		Assert.assertEquals( p1.isMarker(), p2.isMarker() );
		Assert.assertEquals( p1.getPayloadType(), p2.getPayloadType() );
		Assert.assertEquals( p1.getSequence(), p2.getSequence() );
		Assert.assertTrue( Arrays.equals( p1.getCsrc(), p2.getCsrc() ) );
		Assert.assertTrue( Arrays.equals( p1.getPayload(), p2.getPayload() ) );
	}
}
