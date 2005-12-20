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
import rtspproxy.proxy.ClientRdtPacketHandler;

/**
 * @author Matteo Merli
 */
public class RdtClientService extends ProxyService
{

	private IoHandler clientRdtPacketHandler = new ClientRdtPacketHandler();

	public static final String NAME = "RdtClientService";

	private static RdtClientService instance;

	public RdtClientService()
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
		return clientRdtPacketHandler;
	}

	@Override
	public String getNetworkInterface()
	{
		return Config.proxyClientInterface.getValue();
	}

	@Override
	public int[] getBindPorts()
	{
		int port = Config.proxyClientRdtPort.getValue();
		return new int[] { port };
	}

	public static RdtClientService getInstance()
	{
		return instance;
	}

}
