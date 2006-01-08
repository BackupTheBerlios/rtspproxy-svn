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
import java.util.List;

import org.apache.mina.common.IoSession;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.filter.FilterBase;

/**
 * @author Matteo Merli
 *
 */
public class IpAddressFilter extends FilterBase
{

	private static Logger log = LoggerFactory.getLogger( IpAddressFilter.class );
	
	public static final String FilterNAME = "ipAddressFilter";

	private IpAddressProvider provider;

	public IpAddressFilter(String className, List<Element> configElements)
	{
		super(FilterNAME, className, "ipaddress");
		
		this.provider = (IpAddressProvider)loadConfigInitProvider(className, IpAddressProvider.class,
				configElements);
	}

	@Override
	public void messageReceived( NextFilter nextFilter, IoSession session, Object message )
			throws Exception
	{
		if(!isRunning()) {
			// forward because filter is suspended
			nextFilter.messageReceived( session, message );			
		} else if ( !provider.isBlocked( ( (InetSocketAddress) session.getRemoteAddress() ).getAddress() ) ) {
			// forward if not blocked
			nextFilter.messageReceived( session, message );
		} else {
			blockSession( session );
		}
	}

	@Override
	public void sessionCreated( NextFilter nextFilter, IoSession session )
			throws Exception
	{
		if(!isRunning()) {
			// forward because filter is suspended
			nextFilter.sessionCreated( session );
		} else if ( !provider.isBlocked( ( (InetSocketAddress) session.getRemoteAddress() ).getAddress() ) ) {
			// forward if not blocked
			nextFilter.sessionCreated( session );
		} else {
			blockSession( session );
		}
	}

	protected void blockSession( IoSession session )
	{
		log.info( "Blocked connection from : " + session.getRemoteAddress() );
		session.close();
	}
}
