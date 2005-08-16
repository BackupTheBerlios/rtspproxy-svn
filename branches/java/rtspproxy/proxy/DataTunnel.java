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

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.io.IoSession;

/**
 * Manages various UDP connections handles.
 */
public class DataTunnel
{

	public enum Type {
		/** RTP tunnel has 2 channels (RTP and RTCP) */
		RTP,

		/** RDT only has one channel */
		RDT
	}

	public enum ChannelType {
		Data, Control
	}

	/**
	 * Adds two virtual UDP channel, one for data and (if needed) one
	 * for control messages. 
	 * @param dataPort
	 * @param controlPort
	 */
	public void addServerChannel( DataChannel channel)
	{
		
	}	
	
	public void addClientChannel( DataChannel channel )
	{
		
	}

	public void passToClient( ByteBuffer packet, ChannelType type )
	{
		switch ( type ) {
			case Data:
				clientDataSession.write( packet, null );
			case Control:
				clientControlSession.write( packet, null );
		}
	}

	public void passToServer( ByteBuffer packet, ChannelType type )
	{
		switch ( type ) {
			case Data:
				serverDataSession.write( packet, null );
			case Control:
				serverControlSession.write( packet, null );
		}
	}
	
	public int getClientDataPort() 
	{
		return ((InetSocketAddress)clientDataSession.getLocalAddress()).getPort();	
	}
	
	public int getClientControlPort() 
	{
		return ((InetSocketAddress)clientControlSession.getLocalAddress()).getPort();	
	}
	
	public int getServerDataPort() 
	{
		return ((InetSocketAddress)serverDataSession.getLocalAddress()).getPort();	
	}
	
	public int getServerControlPort() 
	{
		return ((InetSocketAddress)serverControlSession.getLocalAddress()).getPort();	
	}

	private IoSession clientDataSession = null;
	private IoSession clientControlSession = null;
	private IoSession serverDataSession = null;
	private IoSession serverControlSession = null;
}
