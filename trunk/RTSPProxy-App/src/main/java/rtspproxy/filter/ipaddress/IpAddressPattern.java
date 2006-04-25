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
package rtspproxy.filter.ipaddress;

import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.config.ListElementParameter;

/**
 * @author Matteo Merli
 * 
 */
public class IpAddressPattern implements ListElementParameter
{

    private static Logger log = LoggerFactory.getLogger( IpAddressPattern.class );

    public enum Type {
        Allow, Deny
    }

    private Type type;

    private Pattern pattern;

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.config.ListElementParameter#readConfiguration(org.apache.commons.configuration.Configuration,
     *      java.lang.String)
     */
    public boolean readConfiguration( Configuration configuration, String prefix )
    {
        String tmpType = configuration.getString( prefix + "[@type]" );
        if ( tmpType == null )
            return false;

        if ( tmpType.equalsIgnoreCase( "allow" ) )
            type = Type.Allow;
        else if ( tmpType.equalsIgnoreCase( "deny" ) )
            type = Type.Deny;
        else
            throw new IllegalArgumentException( "Invalid rule type: " + tmpType + ")" );

        String tmpPattern = configuration.getString( prefix + "[@pattern]" );
        if ( tmpPattern == null )
            throw new IllegalArgumentException( "Missing pattern." );

        log.debug( "Rule: {} {}", type, tmpPattern );

        // Transform the patterns escaping "." and "*" characters
        tmpPattern = tmpPattern.replaceAll( "\\.", "\\\\." );
        tmpPattern = tmpPattern.replaceAll( "\\*", ".*" );
        pattern = Pattern.compile( tmpPattern );
        return true;
    }

    /**
     * @return the pattern
     */
    public Pattern getPattern()
    {
        return pattern;
    }

    /**
     * @param pattern
     *            the pattern to set
     */
    public void setPattern( Pattern pattern )
    {
        this.pattern = pattern;
    }

    /**
     * @return the type
     */
    public Type getType()
    {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType( Type type )
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "IpAddressPattern( " ).append( type );
        sb.append( ' ' ).append( pattern ).append( "  )" );
        return sb.toString();
    }

}
