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

package rtspproxy.proxy;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

import rtspproxy.lib.Exceptions;
import rtspproxy.proxy.track.RtpTrack;
import rtspproxy.proxy.track.Track;
import rtspproxy.rtp.rtcp.RtcpPacket;

/**
 * Handles RTCP packets from client and forward them to server. The RTSP session
 * is obtained using the client IP address and port.
 * 
 * @author Matteo Merli
 */
public class ClientRtcpPacketHandler extends IoHandlerAdapter
{

    private static Logger log = LoggerFactory.getLogger( ClientRtcpPacketHandler.class );

    @Override
    public void sessionCreated( IoSession session ) throws Exception
    {
    }

    @Override
    public void messageReceived( IoSession session, Object buffer ) throws Exception
    {
        RtcpPacket packet = new RtcpPacket( (ByteBuffer) buffer );
        // log.debug( "Received RTCP packet: {}", packet.getType() );

        RtpTrack track = (RtpTrack) Track.getByClientAddress( (InetSocketAddress) session
                .getRemoteAddress() );

        if ( track == null ) {
            // drop packet
            log.debug( "Invalid address: {} - Class: {}", session.getRemoteAddress(),
                    ((InetSocketAddress) session.getRemoteAddress()).getAddress()
                            .getClass() );
            
            log.debug( "Known Client Addresses: {}", Track.clientAddressMap.keySet() );
            return;
        }

        track.forwardRtcpToServer( packet );
    }

    @Override
    public void exceptionCaught( IoSession session, Throwable cause ) throws Exception
    {
        if ( log.isDebugEnabled() ) {
            log.debug( "Exception: " + cause );
            Exceptions.logStackTrace( cause );
        }
        session.close();
    }
}
