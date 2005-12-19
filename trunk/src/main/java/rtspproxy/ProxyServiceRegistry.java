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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.TransportType;
import org.apache.mina.filter.ThreadPoolFilter;
import org.apache.mina.transport.socket.nio.DatagramAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.vmpipe.VmPipeAcceptor;

/**
 * Custom implementation of the ServiceRegistry interface. Creates an acceptor
 * for every service.
 * 
 * @author Matteo Merli
 */
public class ProxyServiceRegistry
{

	/**
	 * Thread pool instance that will be added to all acceptors.
	 */
	protected final ThreadPoolFilter threadPoolFilter = new ThreadPoolFilter();

	/** All the services, mapped by name. */
	private final ConcurrentMap<String, ProxyService> services = new ConcurrentHashMap<String, ProxyService>();

	/** Map a ProxyService to all its bound addresses. */
	private final ConcurrentMap<ProxyService, Set<SocketAddress>> addresses = new ConcurrentHashMap<ProxyService, Set<SocketAddress>>();

	private final ConcurrentMap<ProxyService, IoAcceptor> acceptors = new ConcurrentHashMap<ProxyService, IoAcceptor>();

	public void bind( ProxyService service, IoHandler ioHandler, InetSocketAddress address )
			throws IOException
	{
		bind( service, ioHandler, address, null );
	}

	public void bind( ProxyService service, IoHandler ioHandler,
			InetSocketAddress address, IoFilterChainBuilder filterChainBuilder )
			throws IOException
	{
		IoAcceptor acceptor = newAcceptor( service );
		if ( filterChainBuilder == null ) {
			filterChainBuilder = IoFilterChainBuilder.NOOP;
		}
		acceptor.bind( address, ioHandler, new IoFilterChainBuilderWrapper( service,
				filterChainBuilder ) );

		services.put( service.getName(), service );

		if ( addresses.get( service ) == null )
			addresses.put( service, new HashSet<SocketAddress>() );
		addresses.get( service ).add( address );
	}

	public synchronized void unbind( ProxyService service ) throws Exception
	{
		IoAcceptor acceptor = acceptors.get( service );
		for ( SocketAddress address : addresses.get( service ) ) {
			try {
				acceptor.unbind( address );
			} catch ( Exception e ) {
				// ignore
			}
		}

		if ( service.isRunning() ) {
			service.stop();
		}

		services.remove( service.getName() );
		acceptors.remove( service );
		addresses.remove( service );
	}

	public synchronized void unbindAll() throws Exception
	{
		Set<ProxyService> serviceList = new HashSet<ProxyService>( services.values() );
		for ( ProxyService service : serviceList ) {
			unbind( service );
		}
	}

	public synchronized Set getAllServices()
	{
		return new HashSet<ProxyService>( services.values() );
	}

	public ProxyService getService( String name )
	{
		return services.get( name );
	}

	public IoAcceptor getAcceptor( String serviceName )
	{
		ProxyService service = services.get( serviceName );
		if ( service == null )
			return null;
		else
			return acceptors.get( service );
	}

	public IoAcceptor getAcceptor( ProxyService service )
	{
		return acceptors.get( service );
	}

	private IoAcceptor newAcceptor( ProxyService service )
	{
		// First check if there's already an acceptor
		IoAcceptor acceptor = acceptors.get( service );
		if ( acceptor != null )
			return acceptor;

		// Create a new one
		TransportType transportType = service.getTransportType();
		if ( transportType == TransportType.SOCKET )
			acceptor = new SocketAcceptor();
		else if ( transportType == TransportType.DATAGRAM )
			acceptor = new DatagramAcceptor();
		else if ( transportType == TransportType.VM_PIPE )
			acceptor = new VmPipeAcceptor();
		else
			acceptor = null;

		// Save the acceptor
		acceptors.put( service, acceptor );
		return acceptor;
	}

	private class IoFilterChainBuilderWrapper implements IoFilterChainBuilder
	{

		private final ProxyService service;

		private final IoFilterChainBuilder originalBuilder;

		private IoFilterChainBuilderWrapper( ProxyService service,
				IoFilterChainBuilder originalBuilder )
		{
			this.service = service;
			this.originalBuilder = originalBuilder;
		}

		public void buildFilterChain( IoFilterChain chain ) throws Exception
		{
			chain.getSession().setAttribute( ProxyService.SERVICE, service );

			try {
				originalBuilder.buildFilterChain( chain );
			} finally {
				chain.addFirst( "threadPool", threadPoolFilter );
			}
		}
	}

}
