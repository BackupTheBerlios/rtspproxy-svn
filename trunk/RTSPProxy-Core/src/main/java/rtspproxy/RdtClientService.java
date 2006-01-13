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
import rtspproxy.proxy.ClientRdtPacketHandler;
import rtspproxy.rdt.RdtFilterChainBuilder;

/**
 * ProxyService that manages the RDT packets incoming from clients.
 * 
 * @author Matteo Merli
 */
public final class RdtClientService extends ProxyService
{

	private IoHandler clientRdtPacketHandler = new ClientRdtPacketHandler();
	
	private RdtFilterChainBuilder filterChainBuilder = new RdtFilterChainBuilder();

	/** Service name */
	private static final String NAME = "RdtClientService";

	public RdtClientService()
	{
		super();

		// Subscribe to parameter changes
		Config.proxyClientInterface.addObserver( this );
		Config.proxyClientRdtPort.addObserver( this );
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
	public int getBindPort()
	{
		return Config.proxyClientRdtPort.getValue();
	}

	/**
	 * @return a reference to the (unique) instance of this class
	 */
	public static RdtClientService getInstance()
	{
		return (RdtClientService) Singleton.getInstance( RdtClientService.class );
	}

	@Override
	public Parameter getNetworkInterfaceParameter()
	{
		return Config.proxyClientInterface;
	}

	@Override
	public Parameter getPortParameter()
	{
		return Config.proxyClientRdtPort;
	}

	/* (non-Javadoc)
	 * @see rtspproxy.ProxyService#getFilterChainBuilder()
	 */
	@Override
	public IoFilterChainBuilder getFilterChainBuilder() {
		return this.filterChainBuilder;
	}

	/* (non-Javadoc)
	 * @see rtspproxy.ProxyService#getThreadPoolFilter()
	 */
	@Override
	public ThreadPoolFilter getThreadPoolFilter() {
		ThreadPoolFilter filter = new ThreadPoolFilter("rdtClientThreadPoolFilter");
		
		filter.setMaximumPoolSize(5);
		return filter;
	}

	/**
	 * flag if the service wants to use a ThreadPoolFilter at all
	 */
	public boolean wantThreadPoolFilter() {
		return false;
	}

}
