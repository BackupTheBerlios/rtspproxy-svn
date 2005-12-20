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

import rtspproxy.config.Config;
import rtspproxy.proxy.ClientRtcpPacketHandler;

/**
 * @author Matteo Merli
 */
public class RtcpClientService extends ProxyService
{
	private IoHandler clientRtcpPacketHandler = new ClientRtcpPacketHandler();

	public static final String NAME = "RtcpClientService";
	
	private static RtcpClientService instance;

	public RtcpClientService()
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
		return clientRtcpPacketHandler;
	}

	@Override
	public String getNetworkInterface()
	{
		return Config.proxyClientInterface.getValue();
	}

	@Override
	public int[] getBindPorts()
	{
		int port = Config.proxyClientRtcpPort.getValue();
		return new int[] { port };
	}
	
	public static RtcpClientService getInstance()
	{
		return instance;
	}

}
