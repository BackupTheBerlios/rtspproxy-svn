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
package rtspproxy.filter.rewrite;

import org.apache.commons.configuration.Configuration;

import rtspproxy.config.ListElementParameter;

/**
 * @author Matteo Merli
 */
public class RewriteMapping implements ListElementParameter
{

    private String from = null;

    private String to = null;

    public boolean readConfiguration( Configuration configuration, String prefix )
    {
        String tmpFrom = configuration.getString( prefix + ".from" );
        if ( tmpFrom == null )
            // Value not found
            return false;

        from = tmpFrom;
        to = configuration.getString( prefix + ".to" );

        return true;
    }

    /**
     * @return the from
     */
    public String getFrom()
    {
        return from;
    }

    /**
     * @param from
     *            the from to set
     */
    public void setFrom( String from )
    {
        this.from = from;
    }

    /**
     * @return the to
     */
    public String getTo()
    {
        return to;
    }

    /**
     * @param to
     *            the to to set
     */
    public void setTo( String to )
    {
        this.to = to;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "Mapping(from='" ).append( from );
        sb.append( "' to='" ).append( to ).append( "')" );
        return sb.toString();
    }

}
