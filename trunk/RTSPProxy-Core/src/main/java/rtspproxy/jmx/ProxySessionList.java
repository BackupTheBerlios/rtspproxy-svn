/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   Copyright (C) 2006 - Matteo Merli - matteo.merli@gmail.com            *
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
import java.util.Set;

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

import rtspproxy.proxy.ProxySession;

/**
 * MBeans that lets monitor and adjust the application parameters that can be
 * found on <code>rtspproxy.config.Config</code>
 * 
 * @author Matteo Merli
 */
public class ProxySessionList implements DynamicMBean
{

    private static final String DESCRIPTION = "MBeans that list all the active RTSP proxy sessions.";
    
    private String name;
    
    private MBeanInfo mbeanInfo;
    
    public ProxySessionList()
    {
        name = this.getClass().getSimpleName();
    }

    /*
     * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
     */
    public Object getAttribute( String attributeName ) throws AttributeNotFoundException,
			MBeanException, ReflectionException
    {
        //Parameter parameter = Config.getParameter( attributeName );
        //if ( parameter == null )
        //    throw new AttributeNotFoundException();
        
        return null; // parameter.getObjectValue();
    }

    /*
     * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
     */
    public AttributeList getAttributes( String[] attributes )
    {
        AttributeList results = new AttributeList( attributes.length );
        Attribute attr;
        
        for ( String name : attributes ) {
            // parameter = Config.getParameter( name );
            // if ( parameter == null )
            //     continue;
            
            // attr = new Attribute( parameter.getName(), parameter.getObjectValue() );
            // results.add( attr );
        }
        return results;
    }
    
    /*
     * @see javax.management.DynamicMBean#getMBeanInfo()
     */
    public MBeanInfo getMBeanInfo()
    {
        Set<ProxySession> sessions = ProxySession.getActiveSessions();
        
        // Attributes
        int size = sessions.size();
        MBeanAttributeInfo[] attributeInfo = new MBeanAttributeInfo[size];
        int i = 0;
        for ( ProxySession session : sessions ) {
            attributeInfo[i++] = MBeansFactory.createAttribute( session );
        }

        /* Generate the MBean description. */
        MBeanInfo mbeanInfo = new MBeanInfo( name, // name
                                             DESCRIPTION, //
                                             attributeInfo, // parameters
                                             null, // constructors
                                             null, // operationInfo,      
                                             null // notificationInfo
            );
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
        throw new AttributeNotFoundException();
    }

    /*
     * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
     */
    public AttributeList setAttributes( AttributeList attributes )
    {
        AttributeList results = new AttributeList();
        return results;
    }
}
