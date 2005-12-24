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
import rtspproxy.config.Parameter;
import rtspproxy.lib.Singleton;
import rtspproxy.proxy.ServerRtpPacketHandler;

/**
 * @author Matteo Merli
 */
public final class RtpServerService extends ProxyService
{
	private IoHandler serverRtpPacketHandler = new ServerRtpPacketHandler();

	private static final String NAME = "RtpServerService";

	public RtpServerService()
	{
		super();

		// Subscribe to parameter changes
		Config.proxyServerInterface.addObserver( this );
		Config.proxyServerRtpPort.addObserver( this );
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
		return Config.proxyServerInterface.getValue();
	}

	@Override
	public int getBindPort()
	{
		return Config.proxyServerRtpPort.getValue();
	}

	public static RtpServerService getInstance()
	{
		return (RtpServerService) Singleton.getInstance( RtpServerService.class );
	}

	@Override
	public Parameter getNetworkInterfaceParameter()
	{
		return Config.proxyServerInterface;
	}
	
	@Override
	public Parameter getPortParameter()
	{
		return Config.proxyServerRtpPort;
	}
	
}
