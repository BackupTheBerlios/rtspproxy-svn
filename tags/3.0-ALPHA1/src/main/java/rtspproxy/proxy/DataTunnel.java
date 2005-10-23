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
/*
	private Channel serverChannel = null;
	private List<Channel> clientChannelList = Collections.synchronizedList( new ArrayList<Channel>() );

	public void setServerChannel( Channel channel )
	{
		serverChannel = channel;
	}

	public void addClientChannel( Channel channel )
	{
		clientChannelList.add( channel );
	}

	public void removeClientChannel( Channel channel )
	{
		clientChannelList.remove( channel );
	}

	public void passToClient( ByteBuffer packet, PacketType type )
	{
		for ( Channel channel : clientChannelList ) {
			channel.sendPacket( packet, type );
		}
	}

	public void passToServer( ByteBuffer packet, PacketType type )
	{
		serverChannel.sendPacket( packet, type );
	}
*/
}
