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
import java.util.Collection;
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
import org.apache.mina.registry.Service;
import org.apache.mina.registry.ServiceRegistry;
import org.apache.mina.transport.socket.nio.DatagramAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.vmpipe.VmPipeAcceptor;

/**
 * Custom implementation of the ServiceRegistry interface. Creates an acceptor
 * for every service.
 * 
 * @author Matteo Merli
 */
public class ProxyServiceRegistry implements ServiceRegistry
{

	protected final ThreadPoolFilter threadPoolFilter = new ThreadPoolFilter();

	private final ConcurrentMap<String, IoAcceptor> acceptors = new ConcurrentHashMap<String, IoAcceptor>();
	private final ConcurrentMap<String, Service> services = new ConcurrentHashMap<String, Service>();

	public void bind( Service service, IoHandler ioHandler ) throws IOException
	{
		bind( service, ioHandler, null );
	}

	public void bind( Service service, IoHandler ioHandler,
			IoFilterChainBuilder filterChainBuilder ) throws IOException
	{
		IoAcceptor acceptor = newAcceptor( service );
		if ( filterChainBuilder == null ) {
			filterChainBuilder = IoFilterChainBuilder.NOOP;
		}
		acceptor.bind( service.getAddress(), ioHandler, new IoFilterChainBuilderWrapper(
				service, filterChainBuilder ) );

		services.put( service.getName(), service );
		acceptors.put( service.getName(), acceptor );
	}

	public synchronized void unbind( Service service )
	{
		IoAcceptor acceptor = acceptors.get( service.getName() );
		try {
			acceptor.unbind( service.getAddress() );
		} catch ( Exception e ) {
			// ignore
		}

		services.remove( service.getName() );
		acceptors.remove( service.getName() );
	}

	public void unbind( String serviceName )
	{
		Service service = services.get( serviceName );
		if ( service == null )
			return;
		else
			unbind( service );
	}

	public synchronized void unbindAll()
	{
		Collection<Service> serviceList = services.values();
		for ( Service service : serviceList ) {
			unbind( service );
		}
	}

	public synchronized Set getAllServices()
	{
		return new HashSet<Service>( services.values() );
	}

	public Service getService( String name )
	{
		return services.get( name );
	}

	public Set getServices( String name )
	{
		Set<Service> oneService = new HashSet<Service>();
		Service service = services.get( name );
		if ( service != null )
			oneService.add( service );
		return oneService;
	}

	public Set getServices( TransportType transportType )
	{
		// Not implemented
		return null;
	}

	public Set getServices( int port )
	{
		// Not implemented
		return null;
	}

	public IoAcceptor getAcceptor( TransportType transportType )
	{
		// Not implemented
		return null;
	}

	public IoAcceptor getAcceptor( String serviceName )
	{
		return acceptors.get( serviceName );
	}

	private static IoAcceptor newAcceptor( Service service )
	{
		TransportType transportType = service.getTransportType();
		if ( transportType == TransportType.SOCKET )
			return new SocketAcceptor();
		else
			if ( transportType == TransportType.DATAGRAM )
				return new DatagramAcceptor();
			else
				if ( transportType == TransportType.VM_PIPE )
					return new VmPipeAcceptor();
				else
					return null;
	}

	private class IoFilterChainBuilderWrapper implements IoFilterChainBuilder
	{

		private final Service service;
		private final IoFilterChainBuilder originalBuilder;

		private IoFilterChainBuilderWrapper( Service service,
				IoFilterChainBuilder originalBuilder )
		{
			this.service = service;
			this.originalBuilder = originalBuilder;
		}

		public void buildFilterChain( IoFilterChain chain ) throws Exception
		{
			chain.getSession().setAttribute( SERVICE, service );

			try {
				originalBuilder.buildFilterChain( chain );
			} finally {
				chain.addFirst( "threadPool", threadPoolFilter );
			}
		}
	}

}
