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

import rtspproxy.ProxyService;
import rtspproxy.config.IntegerParameter;
import rtspproxy.config.Parameter;

/**
 * Service MBean implementations
 * 
 * @author Matteo Merli
 */
public class Service implements ServiceMBean
{

	ProxyService proxyService;

	public Service( ProxyService proxyService )
	{
		this.proxyService = proxyService;
	}

	public String getNetworkInterface()
	{
		return proxyService.getNetworkInterfaceParameter().getStringValue();
	}

	public int getPort()
	{
		Parameter parameter = proxyService.getPortParameter();
		return ((IntegerParameter) parameter).getValue();
	}

	public void setPort( int port ) throws MBeanException
	{
		Parameter parameter = proxyService.getPortParameter();
		try {
			parameter.setObjectValue( new Integer( port ) );
		} catch ( Exception e ) {
			throw new MBeanException( e );
		}
	}

	public boolean isRunning()
	{
		return proxyService.isRunning();
	}

	public void start() throws MBeanException
	{
		try {
			proxyService.start();
		} catch ( Exception e ) {
			throw new MBeanException( e );
		}
	}

	public void stop() throws MBeanException
	{
		try {
			proxyService.stop();
		} catch ( Exception e ) {
			throw new MBeanException( e );
		}
	}

	public void restart() throws MBeanException
	{
		try {
			proxyService.restart();
		} catch ( Exception e ) {
			throw new MBeanException( e );
		}
	}

}
