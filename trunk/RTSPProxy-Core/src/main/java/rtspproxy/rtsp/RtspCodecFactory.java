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
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RtspCodecFactory extends DemuxingProtocolCodecFactory
{
    
    private static RtspCodecFactory instance = null;
    
    private static final Logger log = LoggerFactory
            .getLogger( RtspCodecFactory.class );
    
    private final RtspDecoder rtspDecoder;
    
    private final ProtocolEncoder messageEncoder;
    
    private RtspCodecFactory()
    {
        rtspDecoder = new RtspDecoder();
        messageEncoder = new RtspEncoder();
    }
    
    public static synchronized RtspCodecFactory getInstance()
    {
        if ( instance == null ) instance = new RtspCodecFactory();
        
        return instance;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory#getDecoder()
     */
    @Override
    public ProtocolDecoder getDecoder() throws Exception
    {
        return rtspDecoder;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory#getEncoder()
     */
    @Override
    public ProtocolEncoder getEncoder() throws Exception
    {
        log.debug( "Requested the message encoder.." );
        return messageEncoder;
    }
    
    public ProtocolDecoder getHeaderDecoder()
    {
        return rtspDecoder.rtspHeaderDecoder;
    }
    
    public ProtocolDecoder getBodyDecoder()
    {
        return rtspDecoder.rtspBodyDecoder;
    }
    
    private static class RtspDecoder extends CumulativeProtocolDecoder
    {
        private final ProtocolDecoder rtspHeaderDecoder = new RtspHeaderDecoder();
        
        private final ProtocolDecoder rtspBodyDecoder = new RtspBodyDecoder();
        
        /*
         * (non-Javadoc)
         * 
         * @see org.apache.mina.filter.codec.CumulativeProtocolDecoder#doDecode(org.apache.mina.common.IoSession,
         *      org.apache.mina.common.ByteBuffer,
         *      org.apache.mina.filter.codec.ProtocolDecoderOutput)
         */
        @Override
        protected boolean doDecode( IoSession session, ByteBuffer in,
                ProtocolDecoderOutput out ) throws Exception
        {
            ReadState state = (ReadState) session
                    .getAttribute( RtspAttr.readStateATTR );
            
            log.debug( "Read State: {}", state );
            
            if ( state == ReadState.Body ) rtspBodyDecoder.decode( session, in,
                    out );
            else if ( state == null || state == ReadState.Ready
                    || state == ReadState.Command || state == ReadState.Header )
                rtspHeaderDecoder.decode( session, in, out );
            
            return false;
        }
        
    }
    
}
