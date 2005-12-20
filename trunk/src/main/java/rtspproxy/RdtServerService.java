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
import rtspproxy.proxy.ServerRdtPacketHandler;

/**
 * @author Matteo Merli
 */
public class RdtServerService extends ProxyService
{
	private IoHandler serverRdtPacketHandler = new ServerRdtPacketHandler();

	public static final String NAME = "RdtServerService";

	private static RdtServerService instance;

	public RdtServerService()
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
		return serverRdtPacketHandler;
	}

	@Override
	public String getNetworkInterface()
	{
		return Config.proxyServerInterface.getValue();
	}

	@Override
	public int[] getBindPorts()
	{
		int port = Config.proxyServerRdtPort.getValue();
		return new int[] { port };
	}

	public static RdtServerService getInstance()
	{
		return instance;
	}

}
