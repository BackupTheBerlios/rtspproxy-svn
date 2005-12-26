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
package rtspproxy.jmx;

import java.util.Date;

import rtspproxy.config.Config;

public class Info implements InfoMBean
{

	public String getName()
	{
		return Config.getName();
	}

	public String getVersion()
	{
		return Config.getVersion();
	}

	public Date getStartDate()
	{
		return Config.getStartDate();
	}

	public String getOSInfo()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( System.getProperty( "os.name" ) );
		sb.append( " / " ).append( System.getProperty( "os.version" ) );
		sb.append( " / " ).append( System.getProperty( "os.arch" ) );
		return sb.toString();
	}

	public String getJVMInfo()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( System.getProperty( "java.vm.vendor" ) );
		sb.append( " / " );
		sb.append( System.getProperty( "java.vm.version" ) );
		return sb.toString();
	}

	public double getFreeMemoryMB()
	{
		double mb = (double) Runtime.getRuntime().freeMemory() / (1024 * 1024);
		return mb;
		// Formatter f = new Formatter();
		// return f.format( "%1$.2f MB", mb ).toString();
	}

	public double getTotalMemoryMB()
	{
		double mb = (double) Runtime.getRuntime().totalMemory() / (1024 * 1024);
		return mb;
		//Formatter f = new Formatter();
		//return f.format( "%1$.2f MB", mb ).toString();
	}

	public void runGarbageCollector()
	{
		Runtime.getRuntime().gc();
	}

	public int getActiveThreadsNumber()
	{
		return Thread.activeCount();
	}

}
