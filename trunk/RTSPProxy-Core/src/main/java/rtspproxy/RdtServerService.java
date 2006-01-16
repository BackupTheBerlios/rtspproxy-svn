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

import java.net.SocketAddress;

import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.TransportType;
import org.apache.mina.filter.ThreadPoolFilter;

import rtspproxy.config.Config;
import rtspproxy.config.Parameter;
import rtspproxy.lib.Singleton;
import rtspproxy.proxy.ServerRdtPacketHandler;
import rtspproxy.rdt.RdtFilterChainBuilder;

/**
 * ProxyService that manages the RDT packets incoming from servers.
 * 
 * @author Matteo Merli
 */
public final class RdtServerService extends ProxyService
{
	private IoHandler serverRdtPacketHandler = new ServerRdtPacketHandler();

	private static final String NAME = "RdtServerService";

	private RdtFilterChainBuilder filterChainBuilder = new RdtFilterChainBuilder();

	public RdtServerService()
	{
		super();

		// Subscribe to parameter changes
		Config.proxyServerInterface.addObserver( this );
		Config.proxyServerRdtPort.addObserver( this );
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
	public int getBindPort()
	{
		return Config.proxyServerRdtPort.getValue();
	}

	/**
	 * @return a reference to the (unique) instance of this class
	 */
	public static RdtServerService getInstance()
	{
		return (RdtServerService) Singleton.getInstance( RdtServerService.class );
	}

	@Override
	public Parameter getNetworkInterfaceParameter()
	{
		return Config.proxyServerInterface;
	}
	
	@Override
	public Parameter getPortParameter()
	{
		return Config.proxyServerRdtPort;
	}
	/* (non-Javadoc)
	 * @see rtspproxy.ProxyService#getFilterChainBuilder()
	 */
	@Override
	public IoFilterChainBuilder getFilterChainBuilder() {
		return this.filterChainBuilder;
	}
}
