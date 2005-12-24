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
import rtspproxy.proxy.ServerRtcpPacketHandler;

/**
 * ProxyService that manages the RTCP packets incoming from servers.
 * 
 * @author Matteo Merli
 */
public final class RtcpServerService extends ProxyService
{
	private IoHandler serverRtcpPacketHandler = new ServerRtcpPacketHandler();

	/** The name of this service */
	private static final String NAME = "RtcpServerService";

	public RtcpServerService()
	{
		super();

		// Subscribe to parameter changes
		Config.proxyServerInterface.addObserver( this );
		Config.proxyServerRtcpPort.addObserver( this );
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
		return Config.proxyServerInterface.getValue();
	}

	@Override
	public int getBindPort()
	{
		return Config.proxyServerRtcpPort.getValue();
	}

	/**
	 * @return a reference to the (unique) instance of this class
	 */
	public static RtcpServerService getInstance()
	{
		return (RtcpServerService) Singleton.getInstance( RtcpServerService.class );
	}

	@Override
	public Parameter getNetworkInterfaceParameter()
	{
		return Config.proxyServerInterface;
	}

	@Override
	public Parameter getPortParameter()
	{
		return Config.proxyServerRtcpPort;
	}

}
