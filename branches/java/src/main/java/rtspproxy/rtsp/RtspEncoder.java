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

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.ProtocolViolationException;

/**
 * Encode a RTSP message into a buffer for sending.
 */
public class RtspEncoder implements ProtocolEncoder
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.mina.protocol.ProtocolEncoder#encode(org.apache.mina.protocol.ProtocolSession,
	 *      java.lang.Object, org.apache.mina.protocol.ProtocolEncoderOutput)
	 */
	public void encode( IoSession session, Object message, ProtocolEncoderOutput out )
			throws ProtocolViolationException
	{
		// Serialization to string is already provided in RTSP messages.
		String val = ( (RtspMessage) message ).toString();
		ByteBuffer buf = ByteBuffer.allocate( val.length() );
		for ( int i = 0; i < val.length(); i++ ) {
			buf.put( (byte) val.charAt( i ) );
		}

		buf.flip();
		out.write( buf );
	}

}
