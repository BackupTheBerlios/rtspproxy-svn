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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.TransportType;

import rtspproxy.ProxyServiceRegistry.IoFilterChainBuilderWrapper;
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

	private static Logger log = LoggerFactory.getLogger( RtspService.class );

	private IoHandler rtspHandler = new ClientSide();

	private final IoFilterChainBuilder filterChainBuilder = new RtspClientFilters();

	private static final String NAME = "RtspService";

	public RtspService()
	{
		super();

		// Subscribe to parameter changes
		Config.proxyClientInterface.addObserver( this );
		Config.proxyRtspPort.addObserver( this );

		// Subscribe to filter chain changes notification
		Config.proxyFilterAuthenticationEnable.addObserver( this );
		Config.proxyFilterIpaddressEnable.addObserver( this );
		Config.proxyFilterAccountingEnable.addObserver( this );
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

		if ( o == Config.proxyFilterAuthenticationEnable
				|| o == Config.proxyFilterIpaddressEnable
				|| o == Config.proxyFilterAccountingEnable ) {

			/*
			 * Change the filter chain builder to reflect new parameters
			 * directives.
			 */
			IoAcceptor acceptor = ProxyServiceRegistry.getInstance().getAcceptor( this );
			acceptor.setFilterChainBuilder( new IoFilterChainBuilderWrapper( this,
					new RtspClientFilters() ) );

			/*
			 * Print a meaningful info message
			 */
			if ( o == Config.proxyFilterAuthenticationEnable ) {
				if ( Config.proxyFilterAuthenticationEnable.getValue() == true )
					log.info( "Activated the Authentication filter." );
				else
					log.info( "Disabled the Authentication filter." );
			}
			if ( o == Config.proxyFilterIpaddressEnable ) {
				if ( Config.proxyFilterIpaddressEnable.getValue() == true )
					log.info( "Activated the IP address filter." );
				else
					log.info( "Disabled the IP address filter." );
			}
			if ( o == Config.proxyFilterAccountingEnable ) {
				if ( Config.proxyFilterAccountingEnable.getValue() == true )
					log.info( "Activated the Accounting filter." );
				else
					log.info( "Disabled the Accounting filter." );
			}

		} else {
			/*
			 * Other parameters are observed by base class
			 */
			super.update( o, arg );
		}
	}
}
