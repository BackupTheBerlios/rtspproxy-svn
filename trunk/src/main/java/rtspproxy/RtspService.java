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

import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.TransportType;

import rtspproxy.config.Config;
import rtspproxy.filter.RtspClientFilters;
import rtspproxy.proxy.ClientSide;

/**
 * @author Matteo Merli
 */
public class RtspService extends ProxyService
{
	private IoHandler rtspHandler = new ClientSide();

	private IoFilterChainBuilder filterChainBuilder = new RtspClientFilters();

	public static final String NAME = "RtspService";
	
	private static RtspService instance;

	public RtspService()
	{
		super();
		instance = this;
	}

	@Override
	public TransportType getTransportType()
	{
		return TransportType.SOCKET;
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public IoHandler getIoHandler()
	{
		return rtspHandler;
	}

	@Override
	public IoFilterChainBuilder getFilterChainBuilder()
	{
		return filterChainBuilder;
	}

	@Override
	public String getNetworkInterface()
	{
		return Config.proxyClientInterface.getValue();
	}

	@Override
	public int[] getBindPorts()
	{
		return Config.proxyRtspPort.getValue();
	}
	
	public static RtspService getInstance()
	{
		return instance;
	}

}
