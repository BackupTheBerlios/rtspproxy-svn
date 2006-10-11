/**********************************************************************
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version. 
 * 
 *  Copyright (C) 2006 - Matteo Merli - matteo.merli@gmail.com 
 *   
 **********************************************************************/

/*
 * $Id$ 
 * $URL$
 */

package rtspproxy.rtsp;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * @author Matteo Merli
 * 
 */
public class TextLineDecoder extends ProtocolDecoderAdapter
{   
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.filter.codec.ProtocolDecoder#decode(org.apache.mina.common.IoSession,
     *      org.apache.mina.common.ByteBuffer,
     *      org.apache.mina.filter.codec.ProtocolDecoderOutput)
     */
    public void decode( IoSession session, ByteBuffer in,
            ProtocolDecoderOutput out ) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        
        while ( in.remaining() > 0 )
        {
            
            char current = (char) in.get();
            
            if ( current == '\r' )
            {
                if ( in.get() != '\n' )
                    throw new ProtocolDecoderException(
                            "Invalid end of line marker." );
                
                // End of line reached.
                String line = sb.toString();
                out.write( line );
                
                if ( line.length() == 0 )
                {
                    // Empty line marker reached.
                    return;
                }
                
                // Reset the string builder
                sb.delete( 0, sb.length() );
            }
            else
            {
                sb.append( current );
            }
        }
    }
    
}
