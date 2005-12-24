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
import rtspproxy.proxy.ClientRtcpPacketHandler;

/**
 * ProxyService that manages the RTCP packets incoming from clients.
 * 
 * @author Matteo Merli
 */
public final class RtcpClientService extends ProxyService
{
	private IoHandler clientRtcpPacketHandler = new ClientRtcpPacketHandler();

	/** The name of this service */
	private static final String NAME = "RtcpClientService";

	public RtcpClientService()
	{
		super();

		// Subscribe to parameter changes
		Config.proxyClientInterface.addObserver( this );
		Config.proxyClientRtcpPort.addObserver( this );
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
	public int getBindPort()
	{
		return Config.proxyClientRtcpPort.getValue();
	}

	/**
	 * @return a reference to the (unique) instance of this class
	 */
	public static RtcpClientService getInstance()
	{
		return (RtcpClientService) Singleton.getInstance( RtcpClientService.class );
	}

	@Override
	public Parameter getNetworkInterfaceParameter()
	{
		return Config.proxyClientInterface;
	}

	@Override
	public Parameter getPortParameter()
	{
		return Config.proxyClientRtcpPort;
	}

}
