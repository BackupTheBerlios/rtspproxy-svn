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

import java.util.Observable;

import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.TransportType;

import rtspproxy.config.Config;
import rtspproxy.config.Parameter;
import rtspproxy.filter.RtspClientFilters;
import rtspproxy.lib.Singleton;
import rtspproxy.proxy.ClientSide;

/**
 * @author Matteo Merli
 */
public final class RtspService extends ProxyService
{
	
	private final IoHandler rtspHandler = new ClientSide();

	private final IoFilterChainBuilder filterChainBuilder = new RtspClientFilters();

	private static final String NAME = "RtspService";

	public RtspService()
	{
		super();

		// Subscribe to parameter changes
		Config.proxyClientInterface.addObserver( this );
        Config.proxyClientAddress.addObserver( this );
		Config.proxyRtspPort.addObserver( this );
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
	public String getNetworkAddress()
	{
		return Config.proxyClientAddress.getValue();
	}

	@Override
	public int getBindPort()
	{
		return Config.proxyRtspPort.getValue();
	}

	public static RtspService getInstance()
	{
		return (RtspService) Singleton.getInstance( RtspService.class );
	}

	@Override
	public Parameter getNetworkInterfaceParameter()
	{
		return Config.proxyClientInterface;
	}

	@Override
	public Parameter getPortParameter()
	{
		return Config.proxyRtspPort;
	}

	@Override
	public void update( Observable o, Object arg )
	{
		if ( !(o instanceof Parameter) )
			throw new IllegalArgumentException( "Only observe parameters" );

		/*
		 * Other parameters are observed by base class
		 */
		super.update( o, arg );
	}
}
