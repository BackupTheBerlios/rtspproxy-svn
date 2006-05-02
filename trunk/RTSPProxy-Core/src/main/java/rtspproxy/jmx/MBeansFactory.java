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

import javax.management.MBeanAttributeInfo;

import rtspproxy.config.Parameter;
import rtspproxy.jmx.mbeans.ProxySessionFacade;
import rtspproxy.proxy.ProxySession;

/**
 * 
 * @author Matteo Merli
 */
public class MBeansFactory
{

    /** 
     * Creates Attribute Info that wraps Config parameters.
     * @param parameter 
     * @return
     */
    public static MBeanAttributeInfo createAttribute( Parameter parameter )
    {
        return new MBeanAttributeInfo( //
            parameter.getName(), // name
            parameter.getType(), // type
            parameter.getDescription(), // description
            true, // readable
            parameter.isMutable(), // writable
            false // isIs
            );
    }
    
    public static MBeanAttributeInfo createAttribute( ProxySession session )
    {
        ProxySessionFacade facade = new ProxySessionFacade( session );
        
        return new MBeanAttributeInfo( //
            "TODO: ProxySession", // name
            ProxySessionFacade.class.getName(), // type 
            "TODO: description", // description
            true, // readable
            false, // writable
            false // isIs
            );
        
    }

}
