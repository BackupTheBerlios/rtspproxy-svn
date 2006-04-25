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

import org.apache.mina.common.IoFilterChain;

import rtspproxy.lib.Side;

/**
 * Builds the filter chain used when connecting to a RTSP server.
 * 
 * @author Matteo Merli
 */
public class RtspServerFilters extends RtspFilters
{

	public void buildFilterChain( IoFilterChain chain ) throws Exception
	{
		addIpAddressFilter( chain, Side.Server );
		addRtspCodecFilter( chain );
		addRewriteFilter( chain, Side.Server );
		addAccountingFilter( chain );
		addControlFilter ( chain );
	}

}
