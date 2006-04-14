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
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoFilter;
import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.TransportType;
import org.apache.mina.filter.ThreadPoolFilter;
import org.apache.mina.transport.socket.nio.DatagramAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.config.Config;
import rtspproxy.config.Parameter;
import rtspproxy.lib.Singleton;

/**
 * Custom implementation of the ServiceRegistry interface. Creates an acceptor
 * for every service.
 * 
 * @author Matteo Merli
 */
public final class ProxyServiceRegistry extends Singleton implements Observer
{

	private static Logger log = LoggerFactory.getLogger( ProxyServiceRegistry.class );

	public static final String threadPoolFilterNAME = "threadPoolFilter";

	/** Thread pool instance that will be added to all acceptors. */
	private final ThreadPoolFilter threadPoolFilter = new ThreadPoolFilter("sharedThreadPoolFilter");

	/** All the services, mapped by name. */
	private final ConcurrentMap<String, ProxyService> services = new ConcurrentHashMap<String, ProxyService>();

	/** Map a ProxyService to all its bound addresses. */
	private final ConcurrentMap<ProxyService, Set<SocketAddress>> addresses = new ConcurrentHashMap<ProxyService, Set<SocketAddress>>();

	/** Map a ProxyService to its own IoAcceptor. */
	private final ConcurrentMap<ProxyService, IoAcceptor> acceptors = new ConcurrentHashMap<ProxyService, IoAcceptor>();

	/**
	 * Construct a new ProxyServiceRegistry. This class is a Singleton, so there
	 * can be only one instance.
	 */
	public ProxyServiceRegistry()
	{
		int poolMaxSize = Config.threadPoolSize.getValue();
		threadPoolFilter.setMaximumPoolSize( poolMaxSize );

		// Subscribe to thread pool size changes notification
		Config.threadPoolSize.addObserver( this );
	}

	/**
	 * Bind a Service to a local address and specify the IoHandler that will
	 * manage ingoing and outgoing messages.
	 * 
	 * @param service
	 *            the ProxyService
	 * @param ioHandler
	 *            the IoHandler that will handle the messages
	 * @param address
	 *            the local address to bind on
	 * @throws IOException
	 */
	public void bind( ProxyService service, IoHandler ioHandler, InetSocketAddress address )
			throws IOException
	{
		bind( service, ioHandler, address, null );
	}

	/**
	 * Bind a Service to a local address and specify the IoHandler that will
	 * manage ingoing and outgoing messages.
	 * <p>
	 * In addition it should be specified an IoFilterChainBuilder. This builder
	 * will be associated with the IoAcceptor itself (which is unique per
	 * ProxyService) and not for every IoSession created.
	 * 
	 * @param service
	 *            the ProxyService
	 * @param ioHandler
	 *            the IoHandler that will handle the messages
	 * @param address
	 *            the local address to bind on
	 * @param filterChainBuilder
	 *            the IoFilterChainBuilder instance
	 * @throws IOException
	 */
	public void bind( ProxyService service, IoHandler ioHandler,
			InetSocketAddress address, IoFilterChainBuilder filterChainBuilder )
			throws IOException
	{
		IoAcceptor acceptor = newAcceptor( service );
		
		IoFilterChainBuilder builder = new IoFilterChainBuilderWrapper( service,
				filterChainBuilder );
		acceptor.setFilterChainBuilder( builder );
		acceptor.bind( address, ioHandler );

		services.put( service.getName(), service );

		if ( addresses.get( service ) == null )
			addresses.put( service, new HashSet<SocketAddress>() );
		addresses.get( service ).add( address );
	}

	/**
	 * Unbind the service from all of its bound addresses.
	 * 
	 * @param service
	 *            the ProxyService
	 * @throws Exception
	 */
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

	/**
	 * Unbind all the services registered in the ProxyServiceRegistry, from all
	 * of they bound addresses.
	 * 
	 * @throws Exception
	 */
	public synchronized void unbindAll() throws Exception
	{
		Set<ProxyService> serviceList = new HashSet<ProxyService>( services.values() );
		for ( ProxyService service : serviceList ) {
			unbind( service );
		}
	}

	/**
	 * @return a Set containing all the registered services.
	 */
	public Set<ProxyService> getAllServices()
	{
		return new HashSet<ProxyService>( services.values() );
	}

	/**
	 * Return the instance of a ProxyService.
	 * 
	 * @param name
	 *            the name of the ProxyService
	 * @return the instance of the ProxyService
	 */
	public ProxyService getService( String name )
	{
		return services.get( name );
	}

	/**
	 * Returns a reference to the IoAcceptor used by the specified ProxyService.
	 * 
	 * @param serviceName
	 *            the name of the ProxyService
	 * @return the IoAcceptor associated with the service or null if the
	 *         serviceName is invalid
	 */
	public IoAcceptor getAcceptor( String serviceName )
	{
		ProxyService service = services.get( serviceName );
		if ( service == null )
			return null;
		else
			return acceptors.get( service );
	}

	/**
	 * Returns a reference to the IoAcceptor used by the specified ProxyService.
	 * 
	 * @param service
	 *            the ProxyService
	 * @return the IoAcceptor associated with the service
	 */
	public IoAcceptor getAcceptor( ProxyService service )
	{
		return acceptors.get( service );
	}

	/**
	 * Gets a new IoAcceptor suitable for the specified ProxyService
	 * 
	 * @param service
	 *            the ProxyService
	 * @return a reference to the IoAcceptor
	 */
	private IoAcceptor newAcceptor( ProxyService service )
	{
		// First check if there's already an acceptor
		IoAcceptor acceptor = acceptors.get( service );
		if ( acceptor != null )
			return acceptor;

		// Create a new one
		TransportType transportType = service.getTransportType();
		if ( transportType == TransportType.SOCKET )
			acceptor = new SocketAcceptor(); // socketAcceptor;
		else if ( transportType == TransportType.DATAGRAM )
			acceptor = new DatagramAcceptor(); // datagramAcceptor;
		else
			acceptor = null;

		// Save the acceptor
		acceptors.put( service, acceptor );
		return acceptor;
	}

	/**
	 * Gets notification of changed parameters.
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update( Observable o, Object arg )
	{
		if ( !(o instanceof Parameter) )
			throw new IllegalArgumentException( "Only observe parameters" );

		if ( o == Config.threadPoolSize ) {
			// Update the thread pool size
			threadPoolFilter.setMaximumPoolSize( Config.threadPoolSize.getValue() );
			log.info( "Changed ThreadPool size. New max size: "
					+ threadPoolFilter.getMaximumPoolSize() );
		}
	}

	/**
	 * @param service 
	 * @param service 
	 * @return the shared thread pool filter instance
	 */
	public IoFilter getThreadPoolFilterInstance(ProxyService service)
	{
		ThreadPoolFilter filter = service.getThreadPoolFilter();

		if(filter == null)
			filter = threadPoolFilter;
		return filter;
	}

	/**
	 * @return a reference to the (unique) ProxyServiceRegistry instance
	 */
	public static ProxyServiceRegistry getInstance()
	{
		return (ProxyServiceRegistry) Singleton.getInstance( ProxyServiceRegistry.class );
	}

	/**
	 * Wrapper class for the IoFilterChainBuilder that always add the thread
	 * pool filter as the filter in the chain.
	 * <p>
	 * The thread pool filter will be shared by all the services and acceptors.
	 */
	protected static class IoFilterChainBuilderWrapper implements IoFilterChainBuilder
	{

		private final ProxyService service;

		private final IoFilterChainBuilder originalBuilder;

		public IoFilterChainBuilderWrapper( ProxyService service,
				IoFilterChainBuilder originalBuilder )
		{
			this.service = service;
			this.originalBuilder = originalBuilder;
		}

		public void buildFilterChain( IoFilterChain chain ) throws Exception
		{
			chain.getSession().setAttribute( ProxyService.SERVICE, service );

			if(service.wantThreadPoolFilter()) {
				IoFilter threadPoolFilter = ProxyServiceRegistry.getInstance()
				.getThreadPoolFilterInstance(service);
				chain.addFirst( threadPoolFilterNAME, threadPoolFilter );
			}
			originalBuilder.buildFilterChain( chain );
		}
	}

}
