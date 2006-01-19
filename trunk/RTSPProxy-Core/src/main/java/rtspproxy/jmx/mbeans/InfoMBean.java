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

package rtspproxy.jmx.mbeans;

import java.util.Date;

/**
 * MBean interface for exposing basic proxy informations.
 * 
 * @author Matteo Merli
 */
public interface InfoMBean
{

	/** 
	 * @return the name of the application
	 */
	public String getName();
	public String getVersion();
	public Date getStartDate();
	
	public String getOSInfo();
	public String getJVMInfo();
	
	public double getFreeMemoryMB();
	public double getTotalMemoryMB();
	
	public int getActiveThreadsNumber();
	
	// Actions
	
	public void runGarbageCollector();
	
}
