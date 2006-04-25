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

import org.apache.mina.common.IoFilter;
import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import rtspproxy.ProxyServiceRegistry;
import rtspproxy.filter.accounting.AccountingFilter;
import rtspproxy.filter.authentication.AuthenticationFilter;
import rtspproxy.filter.ipaddress.IpAddressFilter;
import rtspproxy.filter.rewrite.UrlRewritingFilter;
import rtspproxy.lib.Side;
import rtspproxy.rtsp.RtspDecoder;
import rtspproxy.rtsp.RtspEncoder;

/**
 * Base class for filter chains based on configuration settings.
 * 
 * @author Matteo Merli
 */
public abstract class RtspFilters implements IoFilterChainBuilder
{

    private static ProtocolCodecFactory codecFactory = new ProtocolCodecFactory()
    {

        // Decoders can be shared
        private final ProtocolEncoder rtspEncoder = new RtspEncoder();

        private final ProtocolDecoder rtspDecoder = new RtspDecoder();

        public ProtocolEncoder getEncoder()
        {
            return rtspEncoder;
        }

        public ProtocolDecoder getDecoder()
        {
            return rtspDecoder;
        }
    };

    private static final IoFilter codecFilter = new ProtocolCodecFilter( codecFactory );

    private static final String rtspCodecNAME = "rtspCodec";

    /**
     * IP Address filter.
     * <p>
     * This needs to be the first filter in the chain to block blacklisted host
     * in the early stage of the connection, preventing network and computation
     * load from unwanted hosts.
     */
    protected void addIpAddressFilter( IoFilterChain chain, Side side )
    {
        IpAddressFilter filter;
	if ( side == Side.Client )
		filter = FilterRegistry.getInstance().getClientAddressFilter();
	else
		filter = FilterRegistry.getInstance().getServerAddressFilter();

	if ( filter == null )
		return;

	chain.addAfter( ProxyServiceRegistry.threadPoolFilterNAME, filter.getChainName(),
                filter );
    }

    /**
     * The RTSP codec filter is always present. Translates the incoming streams
     * into RTSP messages.
     */
    protected void addRtspCodecFilter( IoFilterChain chain )
    {
        chain.addLast( rtspCodecNAME, codecFilter );
    }

    /**
     * Authentication filter.
     */
    protected void addAuthenticationFilter( IoFilterChain chain )
    {
        AuthenticationFilter filter = FilterRegistry.getInstance()
                .getAuthenticationFilter();

	if ( filter == null )
		return;

        chain.addAfter( rtspCodecNAME, filter.getChainName(), filter );
    }

    protected void addAccountingFilter( IoFilterChain chain )
    {
	AccountingFilter filter = FilterRegistry.getInstance().getAccountingFilter();
    
	if ( filter == null )
		return;
    
	chain.addAfter( rtspCodecNAME, filter.getChainName(), filter ); 
    }

    protected void addRewriteFilter( IoFilterChain chain, Side side )
    {
	UrlRewritingFilter filter;
	
	if ( side == Side.Client )
		filter = FilterRegistry.getInstance().getClientRewritingFilter();
	else
		filter = FilterRegistry.getInstance().getServerRewritingFilter();
	
	if ( filter == null )
		return;
          
         chain.addAfter( rtspCodecNAME, filter.getChainName(), filter ); 
    }

    protected void addControlFilter( IoFilterChain chain )
    {
        // XXX: disabled
        /*
         * if ( side == Side.Client ) { List<ClientControlFilter> filters =
         * FilterRegistry.getInstance() .getClientControlFilters();
         * 
         * for ( ControlFilter controlFilter : filters ) { chain.addAfter(
         * rtspCodecNAME, controlFilter.getChainName(), controlFilter ); } }
         * else { List<ServerControlFilter> filters =
         * FilterRegistry.getInstance() .getServerControlFilters();
         * 
         * for ( ControlFilter controlFilter : filters ) { chain.addAfter(
         * rtspCodecNAME, controlFilter.getChainName(), controlFilter ); } }
         */
    }
}
