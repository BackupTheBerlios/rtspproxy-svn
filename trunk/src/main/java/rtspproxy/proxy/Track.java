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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;

import rtspproxy.RtpClientService;
import rtspproxy.RtpServerService;
import rtspproxy.lib.number.UnsignedInt;
import rtspproxy.rtp.RtpPacket;
import rtspproxy.rtp.rtcp.RtcpPacket;

/**
 * @author Matteo Merli
 */
public class Track
{

	private static Logger log = Logger.getLogger( Track.class );

	private String url;

	private UnsignedInt serverSSRC = new UnsignedInt( 0 );
	private UnsignedInt proxySSRC = new UnsignedInt( 0 );;

	private IoSession rtpServerSession = null;
	private IoSession rtcpServerSession = null;
	private IoSession rtpClientSession = null;
	private IoSession rtcpClientSession = null;

	/** Maps a server SSRC id to a Track */
	private static Map<UnsignedInt, Track> serverSsrcMap = Collections.synchronizedMap(new HashMap<UnsignedInt, Track>());
	
	/** Maps a client SSRC id to a Track */
	private static List<UnsignedInt> proxySsrcList = new LinkedList<UnsignedInt>();

	private static Map<InetSocketAddress, Track> clientAddressMap = new HashMap<InetSocketAddress, Track>();
	private static Map<InetSocketAddress, Track> serverAddressMap = new HashMap<InetSocketAddress, Track>();

	private InetAddress clientAddress;
	private int clientRtpPort;
	private int clientRtcpPort;
	private InetAddress serverAddress;
	private int serverRtpPort;
	private int serverRtcpPort;

	/**
	 * Construct a new Track.
	 * 
	 * @param url
	 *        the control name for this track.
	 */
	public Track( String url )
	{
		this.url = url;
		setProxySSRC( newSSRC() );
	}

	/**
	 * Get the track by looking at client socket address.
	 * 
	 * @return a Track instance if a matching pair is found or null
	 */
	public static Track getByClientAddress( InetSocketAddress clientAddress )
	{
		return clientAddressMap.get( clientAddress );
	}

	/**
	 * Get the track by looking at server socket address.
	 * <p>
	 * Used as a workaround for streaming servers which do not hand out a ssrc
	 * in the setup handshake.
	 * 
	 * @return a Track instance if a matching pair is found or null
	 */
	public static Track getByServerAddress( InetSocketAddress serverAddress )
	{
		return serverAddressMap.get( serverAddress );
	}

	public static Track getByServerSSRC( UnsignedInt serverSsrc )
	{
		return serverSsrcMap.get( serverSsrc );
	}

	public static Track getByServerSSRC( String serverSsrc )
	{
		return serverSsrcMap.get( UnsignedInt.fromString( serverSsrc ) );
	}

	public UnsignedInt getProxySSRC()
	{
		return proxySSRC;
	}

	public void setProxySSRC( String proxySSRC )
	{
		this.proxySSRC = UnsignedInt.fromString( proxySSRC, 16 );
		proxySsrcList.add( this.proxySSRC );
	}

	public UnsignedInt getServerSSRC()
	{
		return serverSSRC;
	}

	public void setServerSSRC( String serverSSRC )
	{
		this.serverSSRC = UnsignedInt.fromString( serverSSRC, 16 );
		serverSsrcMap.put( this.serverSSRC, this );
	}

	public void setServerSSRC( UnsignedInt serverSSRC )
	{
		this.serverSSRC = serverSSRC;
		serverSsrcMap.put( this.serverSSRC, this );
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl( String url )
	{
		this.url = url;
	}

	public void setRtcpClientSession( IoSession rtcpClientSession )
	{
		this.rtcpClientSession = rtcpClientSession;
	}

	public void setRtcpServerSession( IoSession rtcpServerSession )
	{
		this.rtcpServerSession = rtcpServerSession;
	}

	public void setRtpClientSession( IoSession rtpClientSession )
	{
		this.rtpClientSession = rtpClientSession;
	}

	public void setRtpServerSession( IoSession rtpServerSession )
	{
		this.rtpServerSession = rtpServerSession;
	}

	public void forwardRtpToServer( RtpPacket packet )
	{
		// modify the SSRC for the server
		packet.setSsrc( proxySSRC );

		if ( rtpServerSession == null )
			rtpServerSession = RtpServerService.newRtpSession( new InetSocketAddress(
					serverAddress, serverRtpPort ) );

		rtpServerSession.write( packet.toByteBuffer() );
	}

	public void forwardRtcpToServer( RtcpPacket packet )
	{
		// modify the SSRC for the server
		packet.setSsrc( proxySSRC );

		if ( rtcpServerSession == null )
			rtcpServerSession = RtpServerService.newRtcpSession( new InetSocketAddress(
					serverAddress, serverRtcpPort ) );

		rtcpServerSession.write( packet.toByteBuffer() );
	}

	public void forwardRtpToClient( RtpPacket packet )
	{
		// modify the SSRC for the client
		packet.setSsrc( proxySSRC );

		if ( rtpClientSession == null ) {
			rtpClientSession = RtpClientService.newRtpSession( new InetSocketAddress(
					clientAddress, clientRtpPort ) );

			// Client packets needs this attribute to find
			// the track
			rtpClientSession.setAttribute( "track", this );
		}

		rtpClientSession.write( packet.toByteBuffer() );
	}

	public void forwardRtcpToClient( RtcpPacket packet )
	{
		// modify the SSRC for the client
		packet.setSsrc( proxySSRC );

		if ( rtcpClientSession == null ) {
			rtcpClientSession = RtpClientService.newRtcpSession( new InetSocketAddress(
					clientAddress, clientRtcpPort ) );

			// Client packets needs this attribute to find
			// the track
			rtcpClientSession.setAttribute( "track", this );
		}
	}

	// / SSRC ID generation
	private static Random random = new Random();

	/**
	 * Creates a new SSRC id that is unique in the proxy.
	 * 
	 * @return the session ID
	 */
	private static String newSSRC()
	{
		long id;
		while ( true ) {
			id = random.nextLong() & 0xFFFFFFFFL;

			if ( !proxySsrcList.contains( id ) ) {
				// Ok, the id is unique
				String ids = Long.toString( id, 16 );
				return ids;
			}
			// try with another id
		}
	}

	/**
	 * @param clientHost
	 *        The clientHost to set.
	 */
	public void setClientAddress( InetAddress clientAddress, int rtpPort, int rtcpPort )
	{
		this.clientAddress = clientAddress;
		this.clientRtpPort = rtpPort;
		this.clientRtcpPort = rtcpPort;

		clientAddressMap.put( new InetSocketAddress( clientAddress, rtpPort ), this );
		clientAddressMap.put( new InetSocketAddress( clientAddress, rtcpPort ), this );
	}

	/**
	 * @param serverHost
	 *        The serverHost to set.
	 */
	public void setServerAddress( InetAddress serverAddress, int rtpPort, int rtcpPort )
	{
		this.serverAddress = serverAddress;
		this.serverRtpPort = rtpPort;
		this.serverRtcpPort = rtcpPort;

		serverAddressMap.put( new InetSocketAddress( serverAddress, rtpPort ), this );
		serverAddressMap.put( new InetSocketAddress( serverAddress, rtcpPort ), this );
	}

}
