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

package server;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * Entrance point of the RTSP server
 */
public class Main
{

	static Logger log = Logger.getRootLogger();

	/**
	 * Main
	 */
	public static void main( String[] args )
	{
		BasicConfigurator.configure();
		new Reactor();
	}

}
