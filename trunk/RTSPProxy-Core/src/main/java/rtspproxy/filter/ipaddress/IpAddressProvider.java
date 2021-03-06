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

import java.net.InetAddress;

import rtspproxy.filter.GenericProvider;
import rtspproxy.lib.Side;

/**
 * @author Matteo Merli
 */
public interface IpAddressProvider extends GenericProvider
{
    
    public void setSide( Side side );

	public boolean isBlocked( InetAddress address );
	
}
