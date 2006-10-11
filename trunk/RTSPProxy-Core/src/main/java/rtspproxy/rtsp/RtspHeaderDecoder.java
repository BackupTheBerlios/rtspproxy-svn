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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RtspHeaderDecoder extends ProtocolDecoderAdapter
{
    
    private static final TextLineDecoder textLineDecoder = new TextLineDecoder();
    
    private static Logger log = LoggerFactory
            .getLogger( RtspHeaderDecoder.class );
    
    private static final Pattern rtspRequestPattern = Pattern
            .compile( "([A-Z_]+) ([^ ]+) RTSP/1.0" );
    
    private static final Pattern rtspResponsePattern = Pattern
            .compile( "RTSP/1.0 ([0-9]+) .+" );
    
    private static final Pattern rtspHeaderPattern = Pattern
            .compile( "([a-zA-Z\\-]+[0-9]?)\\s?:\\s?(.*)" );
    
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.filter.codec.demux.MessageDecoder#decodable(org.apache.mina.common.IoSession,
     *      org.apache.mina.common.ByteBuffer)
     */
    public MessageDecoderResult decodable( IoSession session, ByteBuffer buf )
    {
        ReadState state = (ReadState) session
                .getAttribute( RtspAttr.readStateATTR );
        boolean canDecode = false;
        if ( state == null || state == ReadState.Ready
                || state == ReadState.Command || state == ReadState.Header )
            canDecode = true;
        
        log.debug( "canDecode: {}", canDecode );
        
        return canDecode ? MessageDecoderResult.OK
                : MessageDecoderResult.NOT_OK;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.filter.codec.ProtocolDecoder#decode(org.apache.mina.common.IoSession,
     *      org.apache.mina.common.ByteBuffer,
     *      org.apache.mina.filter.codec.ProtocolDecoderOutput)
     */
    public void decode( IoSession session, ByteBuffer buf,
            ProtocolDecoderOutput out ) throws Exception
    {
        log.debug( "Decoding message header." );
        LineDecoder lineDecoder = new LineDecoder();
        textLineDecoder.decode( session, buf, lineDecoder );
        
        // Retrieve status from session
        ReadState state = (ReadState) session
                .getAttribute( RtspAttr.readStateATTR );
        RtspMessage rtspMessage = (RtspMessage) session
                .getAttribute( RtspAttr.rtspMessageATTR );
        
        if ( state == null )
        {
            // Initialize the state the first time
            state = ReadState.Command;
        }
        
        while ( !lineDecoder.isEmpty() )
        {
            
            String line = lineDecoder.getLine();
            
            switch ( state )
            {
            case Command:
                log.debug( "Command line: {}", line );
                if ( line.startsWith( "RTSP" ) )
                {
                    // this is a RTSP response
                    rtspMessage = decodeResponseLine( line, session );
                }
                else
                {
                    // this is a RTSP request
                    rtspMessage = decodeRequestLine( line, session );
                }
                
                session.setAttribute( RtspAttr.rtspMessageATTR, rtspMessage );
                state = ReadState.Header;
                break;
            
            case Header:

                if ( line.length() == 0 )
                {
                    // This is the empty line that marks the end
                    // of the headers section
                    log.debug( "Empty line." );
                    
                    if ( rtspMessage == null )
                        throw new ProtocolDecoderException(
                                "Invalid header line." );
                    
                    if ( rtspMessage.hasHeader( "Content-Length" )
                            || rtspMessage.hasHeader( "Content-length" ) )
                    {
                        // Message is not complete, we have to read the body
                        log.debug( "We have to read the body" );
                        session.setAttribute( RtspAttr.readStateATTR,
                                ReadState.Body );
                        
                        if ( buf.remaining() > 0 )
                            RtspCodecFactory.getInstance().getBodyDecoder()
                                    .decode( session, buf, out );
                        return;
                    }
                    else
                    {
                        // There's no body content to read,
                        // dispatch the message
                        log.debug( "No body to be read." );
                        session.removeAttribute( RtspAttr.readStateATTR );
                        session.removeAttribute( RtspAttr.rtspMessageATTR );
                        out.write( rtspMessage );
                        return;
                    }
                }
                else
                {
                    // this is an header
                    decodeHeader( line, rtspMessage );
                }
                
                break;
            
            default:
                throw new ProtocolDecoderException( "Invalid RTSP read state." );
            }
            
        }
        
        session.setAttribute( RtspAttr.readStateATTR, state );
    }
    
    private RtspRequest decodeRequestLine( String line, IoSession session )
            throws ProtocolDecoderException
    {
        Matcher m = rtspRequestPattern.matcher( line );
        if ( !m.matches() )
            throw new ProtocolDecoderException( "Malformed request line: "
                    + line );
        
        String verb = m.group( 1 );
        String strUrl = m.group( 2 );
        URL url = null;
        if ( !strUrl.equalsIgnoreCase( "*" ) )
        {
            try
            {
                url = new URL( strUrl );
            } catch ( MalformedURLException e )
            {
                log.info( "malformed URL {}", url );
                url = null;
                throw new ProtocolDecoderException( "Invalid URL" );
            }
        }
        
        RtspRequest request = new RtspRequest();
        request.setVerb( verb );
        
        if ( request.getVerb() == RtspRequest.Verb.None )
        {
            throw new ProtocolDecoderException( "Invalid method: " + verb );
        }
        
        request.setUrl( url );
        return request;
    }
    
    private RtspResponse decodeResponseLine( String line, IoSession session )
            throws ProtocolDecoderException
    {
        Matcher m = rtspResponsePattern.matcher( line );
        if ( !m.matches() )
            throw new ProtocolDecoderException( "Malformed response line: "
                    + line );
        
        RtspCode code = RtspCode.fromString( m.group( 1 ) );
        RtspResponse response = new RtspResponse();
        response.setCode( code );
        RtspRequest.Verb verb = (RtspRequest.Verb) session
                .getAttribute( RtspMessage.lastRequestVerbATTR );
        response.setRequestVerb( verb );
        
        return response;
    }
    
    private void decodeHeader( String line, RtspMessage message )
            throws ProtocolDecoderException
    {
        log.debug( "Header line: {}", line );
        Matcher m = rtspHeaderPattern.matcher( line );
        
        if ( !m.matches() )
        {
            throw new ProtocolDecoderException( "RTSP header not valid, line="
                    + line );
        }
        
        message.setHeader( m.group( 1 ), m.group( 2 ) );
    }
    
    private static class LineDecoder implements ProtocolDecoderOutput
    {
        
        private Queue<String> queue = new LinkedList<String>();
        
        public boolean isEmpty()
        {
            return queue.peek() == null;
        }
        
        public String getLine()
        {
            return queue.poll();
        }
        
        public void write( Object obj )
        {
            queue.add( obj.toString() );
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see org.apache.mina.filter.codec.ProtocolDecoderOutput#flush()
         */
        public void flush()
        {
            // TODO Auto-generated method stub
        }
        
    }
    
}
