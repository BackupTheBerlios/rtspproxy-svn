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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.util.AvailablePortFinder;

/**
 * The PortManager will keep a list of reserved ports
 */
public class PortManager
{

	static Logger log = Logger.getLogger( PortManager.class );

	private static final int minUdpPort = 6790;
	private static Set<Integer> reservedPorts = Collections.synchronizedSet( new HashSet<Integer>() );

	public static void reservePort( int port )
	{
		reservedPorts.add( port );
	}

	public static void removePort( int port )
	{
		reservedPorts.remove( port );
	}

	/**
	 * @param port
	 *        To port to be tested
	 * @return true if the port is already reserved, false if the port can be
	 *         used.
	 */
	public static boolean isReserved( int port )
	{
		return reservedPorts.contains( port );
	}

	public static int[] findAvailablePorts( int nPorts )
	{
		int dataPort;
		int controlPort;

		while ( true ) {
			dataPort = AvailablePortFinder.getNextAvailable( minUdpPort );

			if ( isReserved( dataPort ) )
				continue;

			if ( nPorts == 1 ) {
				// There is only the data port
				int[] a = { dataPort };
				log.debug( "DataPort: " + dataPort );
				return a;

			} else if ( nPorts == 2 ) {
				// We have to find 2 consequents free UDP ports.
				// also: dataPort must be an even number
				if ( ( dataPort % 2 ) != 0 ) {
					continue;

				} else {
					controlPort = AvailablePortFinder.getNextAvailable( dataPort + 1 );

					if ( controlPort != ( dataPort + 1 ) ) {
						// port are not consequents
						continue;
					} else if ( isReserved( controlPort ) ) {
						continue;

					} else {
						int[] a = { dataPort, controlPort };
						log.debug( "DataPort: " + dataPort + " - ControlPort: "
								+ controlPort );
						return a;
					}
				}
			}
		}
	}
}
