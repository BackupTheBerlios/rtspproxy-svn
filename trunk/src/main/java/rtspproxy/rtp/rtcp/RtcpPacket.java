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

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import rtspproxy.rtp.Packet;

/**
 * @author mat
 */
public class RtcpPacket implements Packet
{

	static Logger log = Logger.getLogger( RtcpPacket.class );

	public enum Type {
		/** Sender Report */
		SR(200),
		/** Receiver Report */
		RR(201),
		/** Source description */
		SDES(202),
		/** End of participation */
		BYE(203),
		/** Application specific */
		APP(204),

		NONE(0);

		public final byte value;

		public static Type fromByte( byte value )
		{
			for ( Type t : Type.values() )
				if ( t.value == value )
					return t;
			return NONE;
		}

		private Type( int value )
		{
			this.value = (byte) value;
		}
	}

	/** protocol version */
	protected byte version;
	/** padding flag */
	protected boolean padding;
	/** varies by packet type */
	protected byte count;
	/** RTCP packet type */
	protected byte packetType;
	/** pkt len in words, w/o this word */
	protected short length;

	protected int ssrc;

	// private RtcpInfo rtcpInfo;

	public RtcpPacket( ByteBuffer buffer )
	{
		byte c = buffer.get();
		// |V=2|P=1| SC=5 |
		version = (byte) ( ( c & 0xC0 ) >> 6 );
		padding = ( ( c & 0x20 ) >> 5 ) == 1;
		count = (byte) ( c & 0x1F );
		packetType = buffer.get();
		length = buffer.getShort();
		
		ssrc = buffer.getInt();
		
		/**
		 * <pre>
		 * switch ( Type.fromByte( packetType ) ) {
		 * 	case SR:
		 * 	case RR:
		 * 	case SDES:
		 * 		rtcpInfo = new SDESInfo( this, buffer );
		 * 		break;
		 * 	case BYE:
		 * 	case APP:
		 * 	case NONE:
		 * 		log.debug( &quot;Invalid RTCP	 packet.&quot; );
		 * }
		 * </pre>
		 */
	}
	
	public int getSsrc() 
	{
		return ssrc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.rtp.Packet#toByteBuffer()
	 */
	public ByteBuffer toByteBuffer()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
