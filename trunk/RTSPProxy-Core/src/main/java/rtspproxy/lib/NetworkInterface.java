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

package rtspproxy.lib;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility used to get all the addresses of the network interfaces found on the
 * system.
 * 
 * @author Matteo Merli
 */
public class NetworkInterface
{

	private static Logger log = LoggerFactory.getLogger( NetworkInterface.class );

	/**
	 * Return all the addresses associated with the given interface. If the
	 * supplied interface name is null, all the addresses from all interfaces
	 * will be returned.
	 * 
	 * @param interfaceName
	 * @return a set of InetAddress
	 */
	public static Set<InetAddress> getAddresses( String interfaceAddress )
	{
		Set<InetAddress> addresses = new HashSet<InetAddress>();

		if ( interfaceAddress != null ) {
			try {
				InetAddress address = InetAddress.getByName( interfaceAddress );
				java.net.NetworkInterface networkInterface = java.net.NetworkInterface.getByInetAddress( address);
				addresses.addAll( getAddresses( networkInterface ) );

			} catch ( Exception e ) {
				log.error( "Cannot register network interface: " + interfaceAddress, e );
				return null;
			}
		} else {
			// Add all addresses from all interfaces

			Enumeration<java.net.NetworkInterface> interfaces;
			try {
				interfaces = java.net.NetworkInterface.getNetworkInterfaces();
			} catch ( SocketException se ) {
				log.error( "Cannot get the interfaces list." );
				return null;
			}

			while ( interfaces.hasMoreElements() ) {
				addresses.addAll( getAddresses( interfaces.nextElement() ) );
			}
		}

		return addresses;
	}

	/**
	 * Return all the addresses associated with the given interface. If the
	 * supplied interface name is null, all the addresses from all interfaces
	 * will be returned.
	 * 
	 * @param interfaceName
	 * @return a set of InetAddress
	 */
	public static Set<InetAddress> getInterfaceAddresses( String interfaceName )
	{
		Set<InetAddress> addresses = new HashSet<InetAddress>();

		if ( interfaceName != null ) {
			try {
				// InetAddress address = InetAddress.getByName( interfaceName );
				java.net.NetworkInterface networkInterface = java.net.NetworkInterface.getByName(interfaceName);
				addresses.addAll( getAddresses( networkInterface ) );

			} catch ( Exception e ) {
				log.error( "Cannot register network interface: " + interfaceName, e );
				return null;
			}
		} else {
			// Add all addresses from all interfaces

			Enumeration<java.net.NetworkInterface> interfaces;
			try {
				interfaces = java.net.NetworkInterface.getNetworkInterfaces();
			} catch ( SocketException se ) {
				log.error( "Cannot get the interfaces list." );
				return null;
			}

			while ( interfaces.hasMoreElements() ) {
				addresses.addAll( getAddresses( interfaces.nextElement() ) );
			}
		}

		return addresses;
	}

	private static Set<InetAddress> getAddresses(
			java.net.NetworkInterface networkInterface )
	{
		Set<InetAddress> addresses = new HashSet<InetAddress>();
		Enumeration<InetAddress> enumeration = networkInterface.getInetAddresses();
		while ( enumeration.hasMoreElements() ) {
			addresses.add( enumeration.nextElement() );
		}
		return addresses;
	}

	/**
	 * Returns an address between a set of addresses. This is used to select an
	 * address when binding is done on multiple network interface and we need a
	 * network address as a reference.
	 * <p>
	 * For example if we don't specify a particular network interface, the proxy
	 * will bind on both 127.0.0.1 ::1 and 10.0.0.4 addresses.
	 * <p>
	 * The scope of this method is to select the "better" address to be notified
	 * to clients or servers. This is a guess, for a better solution, just
	 * provide the interface to bind in the configuration file.
	 * 
	 * @param addresses
	 * @return
	 */
	public static InetAddress getBindAddress( Set<InetAddress> addresses )
	{
		if ( addresses.size() == 1 )
			return (InetAddress) addresses.toArray()[0];

		/*
		 * The rules are: - Ip4 addresses are preferred over Ip6 - Non-loopback
		 * are preferred over loopback ones
		 */

		for ( InetAddress address : addresses ) {
			if ( isIp6Address( address ) && hasIp4Addresses( addresses ) )
				continue;

			if ( isLoopbackAddress( address ) && hasNonLoopbackAddresses( addresses ) )
				continue;

			// this is the best guess
			return address;
		}

		return null;
	}

	private static boolean isIp4Address( InetAddress address )
	{
		return ( address instanceof Inet4Address );
	}

	private static boolean isIp6Address( InetAddress address )
	{
		return ( address instanceof Inet6Address );
	}

	private static boolean isLoopbackAddress( InetAddress address )
	{
		return ( address.isLoopbackAddress() || address.isLinkLocalAddress() );
	}

	/**
	 * @param addresses
	 *        the address set to test
	 * @return true if there is at least one IPv4 address
	 */
	private static boolean hasIp4Addresses( Set<InetAddress> addresses )
	{
		for ( InetAddress address : addresses ) {
			if ( isIp4Address( address ) )
				return true;
		}
		return false;
	}

	/**
	 * @param addresses
	 *        the address set to test
	 * @return true if there is at least one address which is not on the
	 *         loopback interface
	 */
	private static boolean hasNonLoopbackAddresses( Set<InetAddress> addresses )
	{
		for ( InetAddress address : addresses ) {
			if ( !isLoopbackAddress( address ) )
				return true;
		}
		return false;
	}
}
