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

import java.nio.charset.Charset;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderException;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encode a RTSP message into a buffer for sending.
 */
public class RtspEncoder extends ProtocolEncoderAdapter
{

    private static final Charset asciiCharset = Charset.forName( "US-ASCII" );
    
    private static final Logger log = LoggerFactory.getLogger( RtspEncoder.class );

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.protocol.ProtocolEncoder#encode(org.apache.mina.protocol.ProtocolSession,
     *      java.lang.Object, org.apache.mina.protocol.ProtocolEncoderOutput)
     */
    public void encode( IoSession session, Object message, ProtocolEncoderOutput out )
            throws ProtocolEncoderException
    {
        log.debug( "Encoding message." );
        // Serialization to string is already provided in RTSP messages.
        String val = ((RtspMessage) message).toString();
        ByteBuffer buf = ByteBuffer.allocate( val.length() );
        buf.put( asciiCharset.encode( val ) );
        buf.flip();
        log.debug( "Message bytes: {}", buf.remaining() );
        log.debug( "Message capacity: {}", buf.capacity() );
        
        out.write( buf );
    }

}
