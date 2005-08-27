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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.io.IoHandler;
import org.apache.mina.io.IoSession;
import org.apache.mina.io.datagram.DatagramConnector;

import rtspproxy.lib.NoPortAvailableException;
import rtspproxy.lib.PortManager;

/**
 * @author mat
 * 
 */
public class Channel
{

	static Logger log = Logger.getLogger( Channel.class );

	private InetSocketAddress remoteDataAddress = null;
	private InetSocketAddress remoteControlAddress = null;
	private InetSocketAddress localDataAddress = null;
	private InetSocketAddress localControlAddress = null;

	private IoSession dataSession = null;
	private IoSession controlSession = null;

	public Channel( String remoteAddress, int dataPort, int controlPort, IoHandler handler )
			throws UnknownHostException, NoPortAvailableException, IOException
	{
		int[] ports = null;
		// If we have a controlChannel we need 2 channels
		int n = ( controlPort == 0 ) ? 1 : 2;

		try {
			ports = PortManager.findAvailablePorts( n );

		} catch ( NoPortAvailableException e ) {
			log.fatal( "No UDP port available found!" );
			throw e;
		}

		try {
			localDataAddress = new InetSocketAddress( InetAddress.getLocalHost(),
					ports[0] );
			remoteDataAddress = new InetSocketAddress( remoteAddress, dataPort );

			if ( n == 2 ) {
				localControlAddress = new InetSocketAddress( InetAddress.getLocalHost(),
						ports[1] );
				remoteControlAddress = new InetSocketAddress( remoteAddress, controlPort );
			}

		} catch ( UnknownHostException e ) {
			log.debug( "Unknown host: " + remoteAddress );
			throw new UnknownHostException( e.getMessage() );
		}

		// ok, now we have all the addresses.. let's create the virtual
		// connections
		DatagramConnector dataConnector = new DatagramConnector();
		dataSession = dataConnector.connect( remoteDataAddress, localDataAddress, handler );
		dataSession.setAttribute( "sessionType", PacketType.DataPacket );
		
		if ( localControlAddress != null && remoteControlAddress != null ) {
			controlSession = dataConnector.connect( remoteControlAddress,
					localControlAddress, handler );
			controlSession.setAttribute( "sessionType", PacketType.ControlPacket );
		}
	}

	public void sendPacket( ByteBuffer packet, PacketType type )
	{
		switch ( type ) {
			case DataPacket:
				dataSession.write( packet, null );
			case ControlPacket:
				controlSession.write( packet, null );
		}
	}

	public int getLocalDataPort()
	{
		if ( localDataAddress == null )
			return 0;
		return localDataAddress.getPort();
	}

	public int getLocalControlPort()
	{
		if ( localControlAddress == null )
			return 0;
		return localControlAddress.getPort();
	}

	public int getRemoteDataPort()
	{
		if ( remoteDataAddress == null )
			return 0;
		return remoteDataAddress.getPort();
	}

	public int getRemoteControlPort()
	{
		if ( remoteControlAddress == null )
			return 0;
		return remoteControlAddress.getPort();
	}
}
