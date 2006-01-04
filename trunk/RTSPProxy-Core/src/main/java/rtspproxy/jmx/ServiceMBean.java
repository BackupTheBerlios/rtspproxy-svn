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

import javax.management.MBeanException;

/**
 * @author Matteo Merli
 */
public interface ServiceMBean
{
	/* Attributes */

	public String getNetworkInterface();

	public int getPort();

	public void setPort( int port ) throws MBeanException;

	public boolean isRunning();

	/* Actions */

	public void start() throws MBeanException;

	public void stop() throws MBeanException;

	public void restart() throws MBeanException;

}
