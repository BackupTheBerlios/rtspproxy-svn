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

package rtspproxy.filter.ipaddress;

import java.net.InetSocketAddress;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.config.Config;
import rtspproxy.filter.FilterBase;
import rtspproxy.lib.Side;

/**
 * @author Matteo Merli
 * 
 */
public class IpAddressFilter extends FilterBase<IpAddressProvider>
{

    private static Logger log = LoggerFactory.getLogger( IpAddressFilter.class );

    private static final String FilterNAME = "ipAddressFilter";

    private IpAddressProvider provider = null;

    private Side side = Side.Client;

    public IpAddressFilter( Side side )
    {
        this.side = side;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.FilterBase#getName()
     */
    @Override
    public String getName()
    {
        return FilterNAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.FilterBase#getProviderClassName()
     */
    @Override
    public String getProviderClassName()
    {
        return Config.filtersIpAddressImplClass.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.FilterBase#getProviderInterface()
     */
    @Override
    protected Class<IpAddressProvider> getProviderInterface()
    {
        return IpAddressProvider.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.FilterBase#setProvider(rtspproxy.filter.GenericProvider)
     */
    @Override
    protected void setProvider( IpAddressProvider provider )
    {
        this.provider = provider;
        this.provider.setSide( side );
    }

    @Override
    public void messageReceived( NextFilter nextFilter, IoSession session, Object message )
            throws Exception
    {
        log.debug( "Testing address: {}", session.getRemoteAddress() );

        if ( !isRunning() ) {
            // forward because filter is suspended
            nextFilter.messageReceived( session, message );

        } else if ( !provider.isBlocked( ((InetSocketAddress) session.getRemoteAddress())
                .getAddress() ) ) {
            // forward if not blocked
            nextFilter.messageReceived( session, message );

        } else {
            blockSession( session );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.common.IoFilterAdapter#sessionCreated(org.apache.mina.common.IoFilter.NextFilter,
     *      org.apache.mina.common.IoSession)
     */
    @Override
    public void sessionCreated( NextFilter nextFilter, IoSession session )
            throws Exception
    {
        if ( !isRunning() ) {
            // forward because filter is suspended
            nextFilter.sessionCreated( session );

        } else if ( !provider.isBlocked( ((InetSocketAddress) session.getRemoteAddress())
                .getAddress() ) ) {
            // forward if not blocked
            nextFilter.sessionCreated( session );
        } else {
            blockSession( session );
        }
    }

    protected void blockSession( IoSession session )
    {
        log.info( "Blocked connection from : {}", session.getRemoteAddress() );
        session.close();
    }
}
