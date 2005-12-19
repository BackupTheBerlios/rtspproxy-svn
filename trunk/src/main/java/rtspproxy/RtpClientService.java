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

import rtspproxy.proxy.ClientRtpPacketHandler;

/**
 * @author Matteo Merli
 */
public class RtpClientService extends ProxyService
{
	private IoHandler clientRtpPacketHandler = new ClientRtpPacketHandler();

	public static final String NAME = "RtpClientService";
	
	private static RtpClientService instance;

	public RtpClientService()
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
		return clientRtpPacketHandler;
	}

	@Override
	public String getNetworkInterface()
	{
		return Config.get( "proxy.client.interface", null );
	}

	@Override
	public int[] getBindPorts()
	{
		int port = Config.getInt( "proxy.client.rtp.port", 8002 );
		return new int[] { port };
	}
	
	public static RtpClientService getInstance()
	{
		return instance;
	}

}
