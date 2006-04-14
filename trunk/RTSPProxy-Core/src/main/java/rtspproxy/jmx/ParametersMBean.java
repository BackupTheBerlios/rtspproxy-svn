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

import java.lang.reflect.Constructor;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

import rtspproxy.config.Config;
import rtspproxy.config.Parameter;

/**
 * MBeans that lets monitor and adjust the application parameters that can be
 * found on <code>rtspproxy.config.Config</code>
 * 
 * @author Matteo Merli
 */
public class ParametersMBean implements DynamicMBean
{

	private static final String DESCRIPTION = "MBeans that lets monitor and adjust the application parameters "
			+ "that can befound on rtspproxy.config.Config";

	private MBeanInfo mbeanInfo;

	public ParametersMBean()
	{
		// Attributes
		int size = Config.getAllParameters().size();
		MBeanAttributeInfo[] attributeInfo = new MBeanAttributeInfo[size];
		int i = 0;
		for ( Parameter parameter : Config.getAllParameters() ) {
			attributeInfo[i++] = MBeansFactory.createAttribute( parameter );
		}

		/* Constructors. */
		Constructor[] constructors = this.getClass().getConstructors();
		MBeanConstructorInfo[] constructorInfo = new MBeanConstructorInfo[constructors.length];
		for ( i = 0; i < constructors.length; i++ ) {
			constructorInfo[i] = new MBeanConstructorInfo( this.getClass().getName(),
					constructors[i] );
		}

		/* Generate the MBean description. */
		mbeanInfo = new MBeanInfo( this.getClass().getName(), // name
				DESCRIPTION, //
				attributeInfo, // parameters
				constructorInfo, // constructors
				null, // operationInfo,
				null // notificationInfo
		);
	}

	/*
	 * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
	 */
	public Object getAttribute( String attributeName ) throws AttributeNotFoundException,
			MBeanException, ReflectionException
	{
		Parameter parameter = Config.getParameter( attributeName );
		if ( parameter == null )
			throw new AttributeNotFoundException();

		return parameter.getObjectValue();
	}

	/*
	 * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
	 */
	public AttributeList getAttributes( String[] attributes )
	{
		AttributeList results = new AttributeList( attributes.length );
		Attribute attr;
		Parameter parameter;

		for ( String name : attributes ) {
			parameter = Config.getParameter( name );
			if ( parameter == null )
				continue;

			attr = new Attribute( parameter.getName(), parameter.getObjectValue() );
			results.add( attr );
		}
		return results;
	}

	/*
	 * @see javax.management.DynamicMBean#getMBeanInfo()
	 */
	public MBeanInfo getMBeanInfo()
	{
		return mbeanInfo;
	}

	/*
	 * @see javax.management.DynamicMBean#invoke(java.lang.String,
	 *      java.lang.Object[], java.lang.String[])
	 */
	public Object invoke( String actionName, Object[] params, String[] signature )
			throws MBeanException, ReflectionException
	{
		throw new ReflectionException( new NoSuchMethodException( "Method not found: "
				+ actionName ) );
	}

	/*
	 * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
	 */
	public void setAttribute( Attribute attribute ) throws AttributeNotFoundException,
			InvalidAttributeValueException, MBeanException, ReflectionException
	{
		Parameter parameter = Config.getParameter( attribute.getName() );
		if ( parameter == null )
			throw new AttributeNotFoundException();

		try {
			parameter.setObjectValue( attribute.getValue() );
		} catch ( IllegalArgumentException e ) {
			throw new InvalidAttributeValueException( e.getMessage() );
		}
	}

	/*
	 * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
	 */
	public AttributeList setAttributes( AttributeList attributes )
	{
		AttributeList results = new AttributeList();
		Parameter parameter;
		Attribute attr;

		for ( Object obj : attributes ) {

			attr = (Attribute) obj;
			parameter = Config.getParameter( attr.getName() );
			if ( parameter == null )
				continue;

			try {
				parameter.setObjectValue( attr.getValue() );
			} catch ( IllegalArgumentException e ) {
				continue;
			}

			attr = new Attribute( parameter.getName(), parameter.getObjectValue() );
			results.add( attr );
		}

		return results;
	}
}
