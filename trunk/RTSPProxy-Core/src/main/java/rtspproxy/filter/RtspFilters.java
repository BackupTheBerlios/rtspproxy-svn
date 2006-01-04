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
import rtspproxy.Reactor;
import rtspproxy.config.Config;
import rtspproxy.filter.accounting.AccountingFilter;
import rtspproxy.filter.authentication.AuthenticationFilter;
import rtspproxy.filter.ipaddress.IpAddressFilter;
import rtspproxy.filter.rewrite.RequestUrlRewritingImpl;
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
		private ProtocolEncoder rtspEncoder = new RtspEncoder();

		private ProtocolDecoder rtspDecoder = new RtspDecoder();

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

	// These filters are instanciated only one time, when requested
	private static IpAddressFilter ipAddressFilter = null;

	private static AuthenticationFilter authenticationFilter = null;

	private static AccountingFilter accountingFilter = null;

	public static final String rtspCodecNAME = "rtspCodec";

	public static final String ipAddressFilterNAME = "ipAddressFilter";

	public static final String authenticationFilterNAME = "authenticationFilter";

	public static final String accountingFilterNAME = "accountingFilter";

	public static final String rewriteFilterNAME = "rewriteFilter";

	/**
	 * IP Address filter.
	 * <p>
	 * This needs to be the first filter in the chain to block blacklisted host
	 * in the early stage of the connection, preventing network and computation
	 * load from unwanted hosts.
	 */
	protected void addIpAddressFilter( IoFilterChain chain )
	{
		boolean enableIpAddressFilter = Config.proxyFilterIpaddressEnable.getValue();

		if ( enableIpAddressFilter ) {
			if ( ipAddressFilter == null )
				ipAddressFilter = new IpAddressFilter();

			chain.addAfter( ProxyServiceRegistry.threadPoolFilterNAME,
					ipAddressFilterNAME, ipAddressFilter );
		}
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
		boolean enableAuthenticationFilter = Config.proxyFilterAuthenticationEnable
				.getValue();

		if ( enableAuthenticationFilter ) {
			if ( authenticationFilter == null )
				authenticationFilter = new AuthenticationFilter();
			chain
					.addAfter( rtspCodecNAME, authenticationFilterNAME,
							authenticationFilter );
		}
	}

	protected void addAccountingFilter( IoFilterChain chain )
	{
		boolean enableAccountingFilter = Config.proxyFilterAccountingEnable.getValue();

		if ( enableAccountingFilter ) {
			if ( accountingFilter == null ) {
				accountingFilter = new AccountingFilter();
			}
			if ( chain.contains( authenticationFilterNAME ) ) {
				/*
				 * If we have the authentication filter in the chain, it's
				 * preferable to have the accounting after that, to see the user
				 * identity if authenticated.
				 */
				chain.addAfter( authenticationFilterNAME, accountingFilterNAME,
						accountingFilter );
			} else {
				/*
				 * At least we want to have it after the RTSP codec, because it
				 * deals with already parsed RTSP messages.
				 */
				chain.addAfter( rtspCodecNAME, accountingFilterNAME, accountingFilter );
			}
		}
	}

	protected void addRewriteFilter( IoFilterChain chain )
	{
		// TODO: use different parameters..
		String rewritingFilter = null; // Config.get(
		// "filter.requestUrlRewriting.implementationClass", null );

		try {
			if ( rewritingFilter != null ) {
				/*
				 * The rewrite filter will be placed after the codec filter
				 * because it deals with already formed RTSP messages.
				 */
				chain.addAfter( rtspCodecNAME, rewriteFilterNAME,
						new RequestUrlRewritingImpl( rewritingFilter ) );
			}
		} catch ( Exception e ) {
			// already logged
			Reactor.stop();
		}
	}
}
