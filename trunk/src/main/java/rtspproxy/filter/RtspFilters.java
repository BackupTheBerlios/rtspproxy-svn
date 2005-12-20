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

import rtspproxy.Reactor;
import rtspproxy.config.Config;
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

	private static IoFilter codecFilter = new ProtocolCodecFilter( codecFactory );

	// These filters are instanciated only one time, when requested
	private static IpAddressFilter ipAddressFilter = null;

	private static AuthenticationFilter authenticationFilter = null;

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
			chain.addLast( "ipAddressFilter", ipAddressFilter );
		}
	}

	/**
	 * The RTSP codec filter is always present. Translates the incoming streams
	 * into RTSP messages.
	 */
	protected void addRtspCodecFilter( IoFilterChain chain )
	{
		chain.addLast( "codec", codecFilter );
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
			chain.addLast( "authentication", authenticationFilter );
		}
	}

	protected void addRewriteFilter( IoFilterChain chain )
	{
		// TODO: use different parameters..
		String rewritingFilter = null; //Config.get(
				//"filter.requestUrlRewriting.implementationClass", null );

		try {
			if ( rewritingFilter != null )
				chain.addLast( "requestUrlRewriting", new RequestUrlRewritingImpl(
						rewritingFilter ) );
		} catch ( Exception e ) {
			// already logged
			Reactor.stop();
		}
	}
}
