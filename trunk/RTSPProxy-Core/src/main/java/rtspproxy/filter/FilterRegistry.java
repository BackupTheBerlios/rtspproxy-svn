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

package rtspproxy.filter;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.Reactor;
import rtspproxy.config.Config;
import rtspproxy.config.XMLConfigReader;
import rtspproxy.filter.accounting.AccountingFilter;
import rtspproxy.filter.authentication.AuthenticationFilter;
import rtspproxy.filter.ipaddress.IpAddressFilter;
import rtspproxy.filter.rewrite.UrlRewritingFilter;
import rtspproxy.jmx.JmxAgent;
import rtspproxy.lib.Singleton;
import rtspproxy.lib.Side;

/**
 * Filter registry. This registry is populated from the configuration on reactor
 * startup
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 */
public class FilterRegistry extends Singleton
{

    private static Logger log = LoggerFactory.getLogger( FilterRegistry.class );

    private IpAddressFilter clientAddressFilter = null;

    private IpAddressFilter serverAddressFilter = null;

    private AuthenticationFilter authenticationFilter = null;

    private AccountingFilter accountingFilter = null;

    private UrlRewritingFilter clientRewritingFilter = null;

    private UrlRewritingFilter serverRewritingFilter = null;

    /**
     * Get the active registry instance
     */
    public static FilterRegistry getInstance()
    {
        return (FilterRegistry) Singleton.getInstance( FilterRegistry.class );
    }

    // flag to determine if already populated
    private boolean populated = false;

    /**
     * populate from configuration
     */
    public void populateRegistry()
    {
        log.debug( "Populate filter registry." );

        if ( populated ) {
            log.debug( "Filter registry already populated." );
            return;
        }

        Configuration config = XMLConfigReader.getConfiguration();

        if ( Config.filtersAuthenticationEnable.getValue() ) {
            authenticationFilter = new AuthenticationFilter();
            authenticationFilter.configure( config );
            registerFilterMBean( authenticationFilter );
        }

        if ( Config.filtersIpAddressEnable.getValue() ) {
            clientAddressFilter = new IpAddressFilter( Side.Client );
            clientAddressFilter.configure( config );
            registerFilterMBean( clientAddressFilter );

            serverAddressFilter = new IpAddressFilter( Side.Server );
            serverAddressFilter.configure( config );
            registerFilterMBean( serverAddressFilter );         
        }

        if ( Config.filtersRewriteEnable.getValue() ) {
            clientRewritingFilter = new UrlRewritingFilter( Side.Client );
            clientRewritingFilter.configure( config );
            registerFilterMBean( clientRewritingFilter );

            serverRewritingFilter = new UrlRewritingFilter( Side.Server );
            serverRewritingFilter.configure( config );
            registerFilterMBean( serverRewritingFilter );
        }

        try {
            // TODO: XXXXX

        } catch ( Throwable t ) {
            log.error( "Failed to populate filter registry", t );

            Reactor.stop();
        }

        this.populated = true;
    }

    private void registerFilterMBean( FilterBase filter )
    {
        if ( Config.jmxEnable.getValue() )
            JmxAgent.getInstance().registerFilter( filter );
    }

    /**
     * @return the accountingFilter
     */
    public AccountingFilter getAccountingFilter()
    {
        return accountingFilter;
    }

    /**
     * @return the addressFilter
     */
    public IpAddressFilter getClientAddressFilter()
    {
        return clientAddressFilter;
    }

    /**
     * @return the server address filter
     */
    public IpAddressFilter getServerAddressFilter()
    {
        return serverAddressFilter;
    }

    /**
     * @return the authenticationFilter
     */
    public AuthenticationFilter getAuthenticationFilter()
    {
        return authenticationFilter;
    }

    /**
     * @return the rewritingFilter
     */
    public UrlRewritingFilter getClientRewritingFilter()
    {
        return clientRewritingFilter;
    }

    /**
     * @return the rewritingFilter
     */
    public UrlRewritingFilter getServerRewritingFilter()
    {
        return serverRewritingFilter;
    }

}
