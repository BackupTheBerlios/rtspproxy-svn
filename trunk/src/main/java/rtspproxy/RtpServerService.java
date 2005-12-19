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

import rtspproxy.proxy.ServerRtpPacketHandler;

/**
 * @author Matteo Merli
 */
public class RtpServerService extends ProxyService
{
	private IoHandler serverRtpPacketHandler = new ServerRtpPacketHandler();

	public static final String NAME = "RtpServerService";
	
	private static RtpServerService instance;

	public RtpServerService()
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
		return serverRtpPacketHandler;
	}

	@Override
	public String getNetworkInterface()
	{
		return Config.get( "proxy.server.interface", null );
	}

	@Override
	public int[] getBindPorts()
	{
		int port = Config.getInt( "proxy.server.rtp.port", 8000 );
		return new int[] { port };
	}
	
	public static RtpServerService getInstance()
	{
		return instance;
	}

}
