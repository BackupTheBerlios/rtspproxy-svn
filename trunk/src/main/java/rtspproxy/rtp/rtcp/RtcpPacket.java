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

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;

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

		private final byte value;

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

		public byte getValue()
		{
			return value;
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
	protected byte[] packetBuffer;

	/**
	 * TODO: At this moment, the RTCP packet is not completely parsed, only some
	 * informations are extracted such as the SSRC identificator. The rest of
	 * the packet is saved but not processed nor validated (for now).
	 */
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

		// we have already read 2 * 4 = 8 bytes 
		// out  of ( length + 1 ) * 4 totals
		int size = Math.min( ((length+1)*4 - 8 ), buffer.remaining() );
		packetBuffer = new byte[size];
		buffer.get( packetBuffer );

		/*
		System.err.println( "version: " + version );
		System.err.println( "Padding: " + padding );
		System.err.println( "count: " + count );
		System.err.println( "packetType: " + Type.fromByte( packetType ) );
		System.err.println( "length: " + length );
		System.err.println( "ssrc: " + Long.toHexString( (long) ssrc & 0xFFFFFFFFL ) );
		System.err.println( "buffer: " + Arrays.toString( packetBuffer ) );
		*/
		
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

	protected RtcpPacket()
	{
	}

	/**
	 * @return Returns the ssrc.
	 */
	public long getSsrc()
	{
		return ( (long) ssrc & 0xFFFFFFFFL );
	}

	/**
	 * @param ssrc
	 *        The ssrc to set.
	 */
	public void setSsrc( long ssrc )
	{
		this.ssrc = (int) ( ssrc & 0xFFFFFFFFL );
	}

	public Type getType()
	{
		return Type.fromByte( packetType );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.rtp.Packet#toByteBuffer()
	 */
	public ByteBuffer toByteBuffer()
	{
		int packetSize = ( packetBuffer.length + 1 ) * 4; // content
		ByteBuffer buffer = ByteBuffer.allocate( packetSize );
		buffer.limit( packetSize );

		// |V=2|P=1| SC=5 |
		byte c;
		c  = (byte) ( ( version << 6 ) & 0xC0 );
		c |= (byte) ( ( ( padding ? 1 : 0 ) << 5 ) & 0x20 ) ;
		c |= (byte) ( count & 0x1F );
		buffer.put( c );
		buffer.put( packetType );
		buffer.putShort( length );
		buffer.putInt( ssrc );

		buffer.put( packetBuffer );
		buffer.rewind();
		return buffer;
	}
}
