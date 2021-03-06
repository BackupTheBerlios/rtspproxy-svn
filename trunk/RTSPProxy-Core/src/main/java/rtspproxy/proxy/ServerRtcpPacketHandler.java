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

import rtspproxy.config.Config;
import rtspproxy.lib.Exceptions;
import rtspproxy.proxy.track.RtpTrack;
import rtspproxy.proxy.track.Track;
import rtspproxy.rtp.rtcp.RtcpPacket;

/**
 * @author mat
 */
public class ServerRtcpPacketHandler extends IoHandlerAdapter
{

    private static Logger log = LoggerFactory.getLogger( ServerRtcpPacketHandler.class );

    @Override
    public void messageReceived( IoSession session, Object buffer ) throws Exception
    {
        RtcpPacket packet = new RtcpPacket( (ByteBuffer) buffer );
        // log.debug( "Receive RTCP packet: " + packet.getType() );
        RtpTrack track = null;

        if ( !Config.proxyServerRtpSsrcUnreliable.getValue() )
            track = RtpTrack.getByServerSSRC( packet.getSsrc() );

        if ( track == null ) {
            if ( Config.proxyServerRtpMultiplePorts.getValue() )
                track = (RtpTrack) Track.getByLocalRemoteServerAddress(
                        (InetSocketAddress) session.getLocalAddress(),
                        (InetSocketAddress) session.getRemoteAddress() );
            else
                track = (RtpTrack) Track.getByServerAddress( (InetSocketAddress) session
                        .getRemoteAddress() );

            if ( track == null ) {
                // drop packet
                log.debug( "Invalid SSRC identifier: {}", packet.getSsrc().toHexString() );
                return;
            }
            
            // hot-wire the ssrc into the track
            log.debug( "Adding SSRC identifier: {}", packet.getSsrc().toHexString() );
            track.setServerSSRC( packet.getSsrc() );
        }

        track.setRtcpServerSession( session );
        track.forwardRtcpToClient( packet );
    }

    @Override
    public void exceptionCaught( IoSession session, Throwable cause ) throws Exception
    {
        log.debug( "Exception: ", cause );
        Exceptions.logStackTrace( cause );
        session.close();
    }

    @Override
    public void sessionCreated( IoSession session ) throws Exception
    {

    }

}
