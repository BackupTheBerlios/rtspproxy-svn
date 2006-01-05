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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;

import rtspproxy.Reactor;
import rtspproxy.config.Config;

/**
 * @author Matteo Merli
 *
 */
public class IpAddressFilter extends IoFilterAdapter
{

	private static Logger log = LoggerFactory.getLogger( IpAddressFilter.class );

	private IpAddressProvider provider;

	public IpAddressFilter()
	{
		// Check which backend implementation to use
		// Default is plain-text implementation
		String className = Config.proxyFilterIpaddressImplementationClass.getValue();

		Class providerClass;
		try {
			providerClass = Class.forName( className );

		} catch ( ClassNotFoundException e ) {
			log.error( "Invalid IpAddressProvider class: " + className );
			Reactor.stop();
			return;
		}

		// Check if the class implements the IpAddressProvider interfaces
		boolean found = false;
		for ( Class interFace : providerClass.getInterfaces() ) {
			if ( IpAddressProvider.class.equals( interFace ) ) {
				found = true;
				break;
			}
		}

		if ( !found ) {
			log.error( "Class (" + provider
					+ ") does not implement the IpAddressProvider interface." );
			Reactor.stop();
			return;
		}

		try {
			provider = (IpAddressProvider) providerClass.newInstance();
			provider.init();
		} catch ( Exception e ) {
			log.error( "Error starting IpAddressProvider: " + e );
			Reactor.stop();
			return;
		}

		log.info( "Using IpAddressFilter (" + className + ")" );
	}

	@Override
	public void messageReceived( NextFilter nextFilter, IoSession session, Object message )
			throws Exception
	{
		if ( !provider.isBlocked( ( (InetSocketAddress) session.getRemoteAddress() ).getAddress() ) ) {
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
		if ( !provider.isBlocked( ( (InetSocketAddress) session.getRemoteAddress() ).getAddress() ) ) {
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
