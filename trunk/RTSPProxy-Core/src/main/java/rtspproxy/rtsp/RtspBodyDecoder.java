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
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RtspBodyDecoder extends ProtocolDecoderAdapter
{
    
    private static Logger log = LoggerFactory.getLogger( RtspBodyDecoder.class );
    
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.filter.codec.demux.MessageDecoder#decodable(org.apache.mina.common.IoSession,
     *      org.apache.mina.common.ByteBuffer)
     */
    public MessageDecoderResult decodable( IoSession session, ByteBuffer in )
    {
        ReadState state = (ReadState) session
                .getAttribute( RtspAttr.readStateATTR );
        log.debug( "ReadState: {}", state );
        
        if ( state == ReadState.Body )
            return MessageDecoderResult.OK;
        
        return MessageDecoderResult.NOT_OK;
    }
    
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
        log.debug( "Decoding message body." );
        log.debug( "ByteBuffer size: {}", in.remaining() );
        
        ReadState state = (ReadState) session
                .getAttribute( RtspAttr.readStateATTR );
        RtspMessage rtspMessage = (RtspMessage) session
                .getAttribute( RtspAttr.rtspMessageATTR );
        
        if ( state != ReadState.Body )
            throw new ProtocolDecoderException( "Expecting the message body." );
        
        int bufferLen = 0;
        if ( rtspMessage.hasHeader( "Content-Length" ) )
            bufferLen = Integer.parseInt( rtspMessage
                    .getHeader( "Content-Length" ) );
        else if ( rtspMessage.hasHeader( "Content-length" ) )
            bufferLen = Integer.parseInt( rtspMessage
                    .getHeader( "Content-length" ) );
        
        if ( bufferLen == 0 )
        {
            // there's no buffer to be read
            state = ReadState.Dispatch;
            
        } else
        {
            log.debug( "Reading content buffer." );
            // we have a content buffer to read
            int bytesToRead = bufferLen - rtspMessage.getBufferSize();
            bytesToRead = Math.min( bytesToRead, in.remaining() );
            
            log.debug( "Bytes To Read: {}", bytesToRead );
            
            // read the content buffer
            rtspMessage.appendToBuffer( in, bytesToRead );
            log.debug( "We read {} bytes", rtspMessage.getBufferSize() );
            
            // this is an ugly hack to avoid content underruns
            // produced by bogus servers
            // TODO: revise this
//          if ( rtspMessage.getBufferSize() == (bufferLen - 2) )
//              rtspMessage.appendToBuffer( "\r\n" );
//          if ( rtspMessage.getBufferSize() == (bufferLen - 1) )
//              rtspMessage.appendToBuffer( "\n" );
            
            // terminate message here
            if ( rtspMessage.getBufferSize() >= bufferLen )
            {
                // The RTSP message parsing is completed
                state = ReadState.Dispatch;
            }
        }
        
        if ( state == ReadState.Dispatch )
        {
            log.debug( "sending decoded RTSP message" );
            // The message is already formed
            // send it
            session.removeAttribute( RtspAttr.readStateATTR );
            session.removeAttribute( RtspAttr.rtspMessageATTR );
            out.write( rtspMessage );
        }
        
        log.debug( "Message Buffer Size: {}", rtspMessage.getBufferSize() );
    }    
}
