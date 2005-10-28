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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;

import rtspproxy.RtpClientService;
import rtspproxy.RtpServerService;
import rtspproxy.rtp.RtpPacket;
import rtspproxy.rtp.rtcp.RtcpPacket;

/**
 * @author Matteo Merli
 */
public class Track
{

	static Logger log = Logger.getLogger( Track.class );

	private String url;

	private long serverSSRC = 0;
	private long proxySSRC = 0;

	private IoSession rtpServerSession = null;
	private IoSession rtcpServerSession = null;
	private IoSession rtpClientSession = null;
	private IoSession rtcpClientSession = null;

	/** Maps a server SSRC id to a Track */
	private static HashMap<Long, Track> serverSsrcMap = new HashMap<Long, Track>();
	/** Maps a client SSRC id to a Track */
	private static List<Long> proxySsrcList = new LinkedList<Long>();

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
	 * Obtain a Track using the client SSRC id.
	 * 
	 * @param clientSsrc
	 *        SSRC id returned by a client.
	 * @return
	 */
	/*
	 * public static Track getByClientSSRC( long clientSsrc ) {
	 * System.err.println( "clientSsrc: " + Long.toHexString( clientSsrc ) );
	 * System.err.print( "List of clientSssrcs: " ); for ( long ssrc :
	 * clientSsrcMap.keySet() ) { System.err.print( Long.toHexString( ssrc ) + " " ); }
	 * System.err.println(); return clientSsrcMap.get( Long.valueOf( clientSsrc ) ); }
	 */
	public static Track getByServerSSRC( long serverSsrc )
	{
		return serverSsrcMap.get( Long.valueOf( serverSsrc ) );
	}

	/*
	 * public static Track getByClientSSRC( String clientSsrc ) { return
	 * clientSsrcMap.get( Long.parseLong( clientSsrc ) ); }
	 */
	public static Track getByServerSSRC( String serverSsrc )
	{
		return serverSsrcMap.get( Long.parseLong( serverSsrc ) );
	}

	public long getProxySSRC()
	{
		return proxySSRC;
	}

	public void setProxySSRC( String proxySSRC )
	{
		this.proxySSRC = Long.parseLong( proxySSRC, 16 ) & 0xFFFFFFFFL;
		proxySsrcList.add( this.proxySSRC );
	}

	public long getServerSSRC()
	{
		return serverSSRC;
	}

	public void setServerSSRC( String serverSSRC )
	{
		this.serverSSRC = Long.parseLong( serverSSRC, 16 ) & 0xFFFFFFFFL;
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
		packet.setSsrc( (int) proxySSRC );

		if ( rtpServerSession == null )
			rtpServerSession = RtpServerService.newRtpSession( new InetSocketAddress(
					serverAddress, serverRtpPort ) );

		rtpServerSession.write( packet.toByteBuffer() );
	}

	public void forwardRtcpToServer( RtcpPacket packet )
	{
		// modify the SSRC for the server
		packet.setSsrc( (int) proxySSRC );

		if ( rtcpServerSession == null )
			rtcpServerSession = RtpServerService.newRtcpSession( new InetSocketAddress(
					serverAddress, serverRtcpPort ) );

		rtcpServerSession.write( packet.toByteBuffer() );
	}

	public void forwardRtpToClient( RtpPacket packet )
	{
		// modify the SSRC for the client
		packet.setSsrc( (int) proxySSRC );

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
		packet.setSsrc( (int) proxySSRC );
		
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
	public void setClientAddress( InetAddress clientAddress )
	{
		this.clientAddress = clientAddress;
	}

	/**
	 * @param clientRtcpPort
	 *        The clientRtcpPort to set.
	 */
	public void setClientRtcpPort( int clientRtcpPort )
	{
		this.clientRtcpPort = clientRtcpPort;
	}

	/**
	 * @param clientRtpPort
	 *        The clientRtpPort to set.
	 */
	public void setClientRtpPort( int clientRtpPort )
	{
		this.clientRtpPort = clientRtpPort;
	}

	/**
	 * @param serverHost
	 *        The serverHost to set.
	 */
	public void setServerAddress( InetAddress serverAddress )
	{
		this.serverAddress = serverAddress;
	}

	/**
	 * @param serverRtcpPort
	 *        The serverRtcpPort to set.
	 */
	public void setServerRtcpPort( int serverRtcpPort )
	{
		this.serverRtcpPort = serverRtcpPort;
	}

	/**
	 * @param serverRtpPort
	 *        The serverRtpPort to set.
	 */
	public void setServerRtpPort( int serverRtpPort )
	{
		this.serverRtpPort = serverRtpPort;
	}

}
