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
import rtspproxy.proxy.ClientRtpPacketHandler;

/**
 * @author Matteo Merli
 */
public final class RtpClientService extends ProxyService
{
	private IoHandler clientRtpPacketHandler = new ClientRtpPacketHandler();

	private static final String NAME = "RtpClientService";

	public RtpClientService()
	{
		super();

		// Subscribe to parameter changes
		Config.proxyClientInterface.addObserver( this );
		Config.proxyClientRtpPort.addObserver( this );
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
		return Config.proxyClientInterface.getValue();
	}

	@Override
	public String getNetworkAddress()
	{
		return Config.proxyClientAddress.getValue();
	}

	@Override
	public int getBindPort()
	{
		return Config.proxyClientRtpPort.getValue();
	}

	public static RtpClientService getInstance()
	{
		return (RtpClientService) Singleton.getInstance( RtpClientService.class );
	}
	
	@Override
	public Parameter getNetworkInterfaceParameter()
	{
		return Config.proxyClientInterface;
	}
	
	@Override
	public Parameter getPortParameter()
	{
		return Config.proxyClientRtpPort;
	}

}
