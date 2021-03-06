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

import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.TransportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import rtspproxy.config.Parameter;
import rtspproxy.lib.Exceptions;
import rtspproxy.lib.NetworkInterface;
import rtspproxy.lib.Singleton;

/**
 * ProxyService is the base abstract class for all the "Services" that can be
 * found on the RtspProxy application.
 * 
 * @author Matteo Merli
 */
public abstract class ProxyService extends Singleton implements Observer
{
    
    private static Logger log = LoggerFactory.getLogger( ProxyService.class );
    
    /**
     * Main Socket address used by the service. It can be bound on several
     * different addresses and network interfaces, but it MUST have a default
     * address to be communicated to third parties.
     */
    private InetSocketAddress socketAddress = null;
    
    /**
     * Flag used to keep track of the service status.
     */
    private volatile boolean isRunning = false;
    
    /**
     * Service hook name.
     */
    protected static final String SERVICE = ProxyService.class.getName();
    
    @Inject
    private IProxyServiceRegistry serviceRegistry;
    
    public ProxyService()
    {
        // XXX: Remove the injector!!
        Injector injector = Guice.createInjector();
        serviceRegistry = injector.getInstance( IProxyServiceRegistry.class );
    }
    
    /**
     * Starts the service.
     * 
     * @throws Exception
     */
    public void start() throws Exception
    {
        if ( isRunning )
        {
            log.warn( getName() + " is already running." );
            return;
        }
        
        int port = getBindPort();
        
        log.debug( "Service Bind port: {}", port );
        
        try
        {
            if ( getNetworkAddress() != null )
            {
                socketAddress =
                        new InetSocketAddress( getNetworkAddress(), port );
                log.debug( "binding to specific address: {}", socketAddress );
                
                serviceRegistry.bind( this, getIoHandler(), socketAddress,
                                      getFilterChainBuilder() );
                
            }
            else
            {
                String netInterface = getNetworkInterface();
                
                Set<InetAddress> addressSet =
                        NetworkInterface.getInterfaceAddresses( netInterface );
                
                log.debug( "Network Interface: {} - Addresses: {}",
                           netInterface, addressSet );
                if ( addressSet == null )
                {
                    throw new RuntimeException( "Invalid Network interface: "
                            + netInterface );
                }
                
                for ( InetAddress inetAddress : addressSet )
                {
                    // Bind to all addresses
                    
                    log.debug( "binding to address from set: {}", socketAddress );
                    socketAddress = new InetSocketAddress( inetAddress, port );
                    
                    serviceRegistry.bind( this, getIoHandler(),
                                                socketAddress,
                                                getFilterChainBuilder() );
                    
                }
                
                // Choose a bind address
                InetAddress inetAddress =
                        NetworkInterface.getBindAddress( addressSet );
                socketAddress = new InetSocketAddress( inetAddress, port );
                
            }
        } catch ( IOException e )
        {
            log.error( "Can't start {} ({}): " + e.getMessage(), getName(),
                       socketAddress );
            throw e;
        }
        log.info( "{} Started - Listening on: {}", getName(), socketAddress );
        
        isRunning = true;
    }
    
    /**
     * Stops the service
     * 
     * @throws Exception
     */
    public void stop() throws Exception
    {
        if ( !isRunning )
        {
            log.warn( getName() + " is not running." );
            return;
        }
        
        try
        {
            serviceRegistry.unbind( this, false );
        } catch ( Exception e )
        {
            log.debug( "Exception unbinding service: {}", e.getMessage() );
        }
        
        log.info( getName() + " Stopped" );
        isRunning = false;
    }
    
    /**
     * Restart the service.
     * 
     * @throws Exception
     */
    public void restart() throws Exception
    {
        log.info( "Restarting {}", getName() );
        if ( isRunning ) stop();
        else log.warn( getName() + " is not running." );
        
        start();
    }
    
    /**
     * @return true if the server is running
     */
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
    
    /**
     * @return the filter chain builder to be be used by the IoAcceptor
     *         associated with the service.
     */
    public IoFilterChainBuilder getFilterChainBuilder()
    {
        // By default there's no filter chain
        return IoFilterChainBuilder.NOOP;
    }
    
    /**
     * Return the name of the service.
     * 
     * @return the human readable name
     */
    public abstract String getName();
    
    /**
     * Get the network interface to bind to. This is only used if there is no
     * more specific IP address configured.
     * 
     * @return the network interface to bind this service on, as it appears in
     *         the configuratio registry (Config).
     */
    public abstract String getNetworkInterface();
    
    /**
     * @return the network address to bind this service on, as it appears in the
     *         configuration registry (Config). If null, the network interface
     *         configuration parameter is used.
     */
    public abstract String getNetworkAddress();
    
    /**
     * @return the port to bind on, as it appear in the configuration registry.
     */
    public abstract int getBindPort();
    
    /**
     * @return the Parameter associated with the network interface used by the
     *         service.
     */
    public abstract Parameter getNetworkInterfaceParameter();
    
    /**
     * @return the Parameter associated with the port number used by the
     *         service.
     */
    public abstract Parameter getPortParameter();
    
    /**
     * @return the main IP address where the service is bound.
     */
    public InetAddress getAddress()
    {
        return socketAddress.getAddress();
    }
    
    /**
     * @return the main TCP or UDP port where the service is bound.
     */
    public int getPort()
    {
        return socketAddress.getPort();
    }
    
    /**
     * @return the TCP or UDP address (IP+port) where the service is bound.
     */
    public SocketAddress getSocketAddress()
    {
        return socketAddress;
    }
    
    /**
     * Creates a new connection-less IoSession to a remote address. This is only
     * used to create UDP session.
     * 
     * @param remoteAddress
     *            the address of the remote host to connect to.
     * @return the newly created IoSession
     */
    public synchronized IoSession newSession( SocketAddress remoteAddress )
    {
        IoSession session = null;
        IoAcceptor acceptor = serviceRegistry.getAcceptor( this );
        
        // TODO: ConnectionlessSessionTracker
        // if(acceptor instanceof ConnectionlessSessionTracker)
        // session =
        // ((ConnectionlessSessionTracker)acceptor).getSession(socketAddress,
        // remoteAddress);
        
        if ( session == null )
            session = acceptor.newSession( remoteAddress, socketAddress );
        
        return session;
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
        if ( !(o instanceof Parameter) )
            throw new IllegalArgumentException( "Only observe parameters" );
        
        try
        {
            restart();
        } catch ( Exception e )
        {
            log.error( "Error restarting {}", getName() );
            Exceptions.logStackTrace( e );
            throw new RuntimeException( e );
        }
    }
    
}
