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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.TransportType;

import rtspproxy.lib.NetworkInterface;
import rtspproxy.lib.Singleton;

/**
 * @author Matteo Merli
 */
public abstract class ProxyService extends Singleton implements Observer
{

	private static Logger log = Logger.getLogger( ProxyService.class );

	private InetSocketAddress socketAddress = null;

	private volatile boolean isRunning = false;

	public static final String SERVICE = ProxyService.class.getName();

	/**
	 * Starts the service.
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception
	{
		if ( isRunning ) {
			log.warn( getName() + " is already running." );
			return;
		}

		String netInterface = getNetworkInterface();
		int[] ports = getBindPorts();

		try {

			Set<InetAddress> addressSet = NetworkInterface.getAddresses( netInterface );

			for ( InetAddress inetAddress : addressSet ) {
				// Bind to all addresses

				for ( int port : ports ) {
					// Bind to all the specified ports
					socketAddress = new InetSocketAddress( inetAddress, port );

					Reactor.getRegistry().bind( this, getIoHandler(), socketAddress,
							getFilterChainBuilder() );
				}
			}

			// Choose a bind address
			InetAddress inetAddress = NetworkInterface.getBindAddress( addressSet );
			socketAddress = new InetSocketAddress( inetAddress, ports[0] );

			log.info( getName() + " Started - Listening on: " + socketAddress );

		} catch ( IOException e ) {
			log.fatal( "Can't start " + getName() + " " + e );
			throw e;
		}

		isRunning = true;
	}

	/**
	 * Stops the service
	 * 
	 * @throws Exception
	 */
	protected void stop() throws Exception
	{
		if ( !isRunning ) {
			log.warn( getName() + " is not running." );
			return;
		}

		log.info( getName() + " Stopped" );
		isRunning = false;
	}

	protected void restart() throws Exception
	{
		if ( !isRunning ) {
			log.warn( getName() + " is not running." );
			return;
		}

		log.info( "Restarting " + getName() );
		stop();
		start();
	}

	public boolean isRunning()
	{
		return isRunning;
	}

	/**
	 * @return the transport type used by this service
	 */
	public abstract TransportType getTransportType();

	/**
	 * @return an instance to the IoHandler object that will receive all the
	 *         messages.
	 */
	public abstract IoHandler getIoHandler();

	public IoFilterChainBuilder getFilterChainBuilder()
	{
		// By default there's no filter chain
		return null;
	}

	/**
	 * Return the name of the service.
	 * 
	 * @return the human readable name
	 */
	public abstract String getName();

	public abstract String getNetworkInterface();

	public abstract int[] getBindPorts();

	public InetAddress getAddress()
	{
		return socketAddress.getAddress();
	}

	public int getPort()
	{
		return socketAddress.getPort();
	}

	public SocketAddress getSocketAddress()
	{
		return socketAddress;
	}

	public IoSession newSession( SocketAddress remoteAddress )
	{
		return Reactor.getRegistry().getAcceptor( this ).newSession( remoteAddress,
				socketAddress );
	}

	/**
	 * Update the ProxyService state. A proxy service will likely subscribe to
	 * some parameter changes notifications. When a change is notified the
	 * service will be restarted.
	 * <p>
	 * <i>NOTE:</i>
	 * <ul>
	 * <li>if the service is TCP based, all connected clients will be
	 * disconnected! </li>
	 * <li>If the service is UDP based some packets may be missed when the
	 * service is down. </li>
	 * </ul>
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update( Observable o, Object arg )
	{
		try {
			restart();
		} catch ( Exception e ) {
			log.error( "Error restarting " + getName(), e );
		}
	}
}
