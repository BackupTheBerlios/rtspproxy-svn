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

package rtspproxy;

import org.apache.mina.common.IoHandler;
import org.apache.mina.common.TransportType;

import rtspproxy.proxy.ServerRtcpPacketHandler;

/**
 * @author Matteo Merli
 */
public class RtcpServerService extends ProxyService
{
	private IoHandler serverRtcpPacketHandler = new ServerRtcpPacketHandler();

	public static final String NAME = "RtcpServerService";
	
	private static RtcpServerService instance;

	public RtcpServerService()
	{
		super();
		instance = this;
	}

	@Override
	public TransportType getTransportType()
	{
		return TransportType.DATAGRAM;
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public IoHandler getIoHandler()
	{
		return serverRtcpPacketHandler;
	}

	@Override
	public String getNetworkInterface()
	{
		return Config.get( "proxy.server.interface", null );
	}

	@Override
	public int[] getBindPorts()
	{
		int port = Config.getInt( "proxy.server.rtcp.port", 8001 );
		return new int[] { port };
	}
	
	public static RtcpServerService getInstance()
	{
		return instance;
	}

}
