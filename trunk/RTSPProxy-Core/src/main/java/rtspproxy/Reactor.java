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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import rtspproxy.config.Config;
import rtspproxy.filter.FilterRegistry;
import rtspproxy.filter.IFilterRegistry;
import rtspproxy.jmx.JmxAgent;
import rtspproxy.lib.Exceptions;
import rtspproxy.rtp.range.PortrangeRtpServerSessionFactory;

/**
 * Main reactor of RTSP proxy. This reactor assembles all required services.
 * The reactor expects a valid configuration before startup eg the global configuration must
 * have been filled before starting the reactor.
 */
@Singleton
public class Reactor implements IReactor
{

    private static Logger log = LoggerFactory.getLogger( Reactor.class );

    @Inject
    private IProxyServiceRegistry serviceRegistry;

    private JmxAgent jmxAgent;
    
    @Inject
    private IFilterRegistry filterRegistry;
    
    private boolean isStandalone = false;
    
    /* (non-Javadoc)
     * @see rtspproxy.IReactor#setStandalone(boolean)
     */
    public void setStandalone( boolean standalone )
    {
        isStandalone = standalone;
    }

    /* (non-Javadoc)
     * @see rtspproxy.IReactor#start()
     */
    public void start() throws Exception
    {
        log.info( "Starting " + Config.getName() + " " + Config.getVersion() );

        // Register the "rtsp://" protocol scheme
        System.setProperty( "java.protocol.handler.pkgs", "rtspproxy" );

        ProxyService rtspService = new RtspService();
        rtspService.start();
        
        ProxyService rtpClientService = new RtpClientService();
        rtpClientService.start();
        
        ProxyService rtcpClientService = new RtcpClientService();
        rtcpClientService.start();

        ProxyService rtpServerService = new RtpServerService();
        rtpServerService.start();
                
        ProxyService rtcpServerService = new RtcpServerService();
        rtcpServerService.start();

        ProxyService rdtClientService = new RdtClientService();
        rdtClientService.start();

        ProxyService rdtServerService = new RdtServerService();
        rdtServerService.start();

        boolean enableJmx = Config.jmxEnable.getValue();
         if ( enableJmx )
             jmxAgent = new JmxAgent();

        filterRegistry = new FilterRegistry();
        filterRegistry.populateRegistry();		
		
        PortrangeRtpServerSessionFactory portrangeFactory = new PortrangeRtpServerSessionFactory();
        portrangeFactory.setLocalAddress(rtpServerService.getAddress());
        portrangeFactory.start();
        
    }

    /* (non-Javadoc)
     * @see rtspproxy.IReactor#stop()
     */
    public void stop()
    {
        try {
            // TODO: check why null pointer exception
            // PortrangeRtpServerSessionFactory.getInstance().stop();
            
            if ( jmxAgent != null )
                jmxAgent.stop();
            
            if ( serviceRegistry != null )
                serviceRegistry.unbindAll();
            
            log.info( "Shutdown completed" );

        } catch ( Exception e ) {
            log.warn( "Error shutting down: {}", (Object)e );
            Exceptions.logStackTrace( e );
        }

        if ( isStandalone )
            Runtime.getRuntime().halt( 0 );
    }

}
