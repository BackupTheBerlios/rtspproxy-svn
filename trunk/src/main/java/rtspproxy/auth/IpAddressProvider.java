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

package rtspproxy.auth;

import java.net.InetAddress;

/**
 * @author Matteo Merli
 */
public interface IpAddressProvider
{

	public void init() throws Exception;

	public void shutdown() throws Exception;
	
	public boolean isBlocked( InetAddress address );
	
}
