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

import rtspproxy.IReactor;
import rtspproxy.config.Config;
import rtspproxy.config.XMLConfigReader;
import rtspproxy.filter.accounting.AccountingFilter;
import rtspproxy.filter.authentication.AuthenticationFilter;
import rtspproxy.filter.ipaddress.IpAddressFilter;
import rtspproxy.filter.rewrite.UrlRewritingFilter;
import rtspproxy.jmx.JmxAgent;
import rtspproxy.lib.Side;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Filter registry. This registry is populated from the configuration on reactor
 * startup
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 */
@Singleton
public class FilterRegistry implements IFilterRegistry
{
    
    private static Logger log = LoggerFactory.getLogger( FilterRegistry.class );
    
    private IpAddressFilter clientAddressFilter = null;
    
    private IpAddressFilter serverAddressFilter = null;
    
    private AuthenticationFilter authenticationFilter = null;
    
    private AccountingFilter accountingFilter = null;
    
    private UrlRewritingFilter clientRewritingFilter = null;
    
    private UrlRewritingFilter serverRewritingFilter = null;
    
    @Inject
    private IReactor reactor;
    
    // flag to determine if already populated
    private boolean populated = false;
    
    /* (non-Javadoc)
     * @see rtspproxy.filter.IFilterRegistry#populateRegistry()
     */
    public void populateRegistry()
    {
        log.debug( "Populate filter registry." );
        
        if ( populated )
        {
            log.debug( "Filter registry already populated." );
            return;
        }
        
        Configuration config = XMLConfigReader.getConfiguration();
        
        try
        {
            
            authenticationFilter = new AuthenticationFilter();
            authenticationFilter.configure( config );
            registerFilterMBean( authenticationFilter );
            if ( Config.filtersAuthenticationEnable.getValue() )
            {
                authenticationFilter.resume();
            }
            
            clientAddressFilter = new IpAddressFilter( Side.Client );
            clientAddressFilter.configure( config );
            registerFilterMBean( clientAddressFilter );
            
            serverAddressFilter = new IpAddressFilter( Side.Server );
            serverAddressFilter.configure( config );
            registerFilterMBean( serverAddressFilter );
            if ( Config.filtersIpAddressEnable.getValue() )
            {
                clientAddressFilter.resume();
                serverAddressFilter.resume();
            }
            
            clientRewritingFilter = new UrlRewritingFilter( Side.Client );
            clientRewritingFilter.configure( config );
            registerFilterMBean( clientRewritingFilter );
            
            serverRewritingFilter = new UrlRewritingFilter( Side.Server );
            serverRewritingFilter.configure( config );
            registerFilterMBean( serverRewritingFilter );
            if ( Config.filtersRewriteEnable.getValue() )
            {
                clientRewritingFilter.resume();
                serverRewritingFilter.resume();
            }
            
            accountingFilter = new AccountingFilter();
            accountingFilter.configure( config );
            registerFilterMBean( accountingFilter );
            if ( Config.filtersAccountingEnable.getValue() )
            {
                accountingFilter.resume();
            }
            
        } catch ( Throwable t )
        {
            log.error( "Failed to populate filter registry", t );
            reactor.stop();
        }
        
        populated = true;
    }
    
    private void registerFilterMBean( FilterBase filter )
    {
        if ( Config.jmxEnable.getValue() )
            JmxAgent.getInstance().registerFilter( filter );
    }
    
    /* (non-Javadoc)
     * @see rtspproxy.filter.IFilterRegistry#getAccountingFilter()
     */
    public AccountingFilter getAccountingFilter()
    {
        return accountingFilter;
    }
    
    /* (non-Javadoc)
     * @see rtspproxy.filter.IFilterRegistry#getClientAddressFilter()
     */
    public IpAddressFilter getClientAddressFilter()
    {
        return clientAddressFilter;
    }
    
    /* (non-Javadoc)
     * @see rtspproxy.filter.IFilterRegistry#getServerAddressFilter()
     */
    public IpAddressFilter getServerAddressFilter()
    {
        return serverAddressFilter;
    }
    
    /* (non-Javadoc)
     * @see rtspproxy.filter.IFilterRegistry#getAuthenticationFilter()
     */
    public AuthenticationFilter getAuthenticationFilter()
    {
        return authenticationFilter;
    }
    
    /* (non-Javadoc)
     * @see rtspproxy.filter.IFilterRegistry#getClientRewritingFilter()
     */
    public UrlRewritingFilter getClientRewritingFilter()
    {
        return clientRewritingFilter;
    }
    
    /* (non-Javadoc)
     * @see rtspproxy.filter.IFilterRegistry#getServerRewritingFilter()
     */
    public UrlRewritingFilter getServerRewritingFilter()
    {
        return serverRewritingFilter;
    }
    
}
