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

package rtspproxy.rtsp;

import java.util.LinkedList;
import java.util.List;

/**
 * Represent a list of transport headers.
 */
public class RtspTransportList
{

	List<RtspTransport> transportList;

	/**
	 * Constructor. Creates a list of transport type.
	 */
	public RtspTransportList( String transportHeader )
	{
		transportList = new LinkedList<RtspTransport>();

		for ( String transport : transportHeader.split( "," ) ) {
			transportList.add( new RtspTransport( transport ) );
		}
	}

	/**
	 * @return The number of transport defined.
	 */
	public int count()
	{
		return transportList.size();
	}

	public String toString()
	{
		String s = "";
		for ( RtspTransport t : transportList ) {
			s += t.toString();
		}
		return s;
	}

}
