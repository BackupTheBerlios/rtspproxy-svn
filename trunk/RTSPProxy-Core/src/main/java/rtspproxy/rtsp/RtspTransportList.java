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

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a list of transport headers.
 */
public class RtspTransportList
{

	private List<RtspTransport> transportList;

	/**
	 * Constructor. Creates a list of transport type.
	 */
	public RtspTransportList( String transportHeader )
	{
		transportList = new ArrayList<RtspTransport>();

		RtspTransport transport;
		for ( String transportString : transportHeader.split( "," ) ) {
			transport = new RtspTransport( transportString );
			if ( transport.isSupportedByProxy() )
				transportList.add( transport );
		}
	}

	public List<RtspTransport> getList()
	{
		return transportList;
	}

	public RtspTransport get( int index )
	{
		return transportList.get( index );
	}

	/**
	 * @return The number of transports defined.
	 */
	public int count()
	{
		return transportList.size();
	}

	@Override
    public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		int i = 0;
		for ( RtspTransport transport : transportList ) {
			if ( i++ != 0 )
				sb.append( ',' );
			sb.append( transport.toString() );
		}
		return sb.toString();
	}
}
