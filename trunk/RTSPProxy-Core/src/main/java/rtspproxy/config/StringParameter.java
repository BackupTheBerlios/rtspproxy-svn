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
package rtspproxy.config;

import org.apache.commons.configuration.Configuration;

/**
 * @author Matteo Merli
 */
public class StringParameter extends Parameter
{

    private String value = null;

    private String defaultValue;

    public StringParameter( String name, String defaultValue, boolean mutable,
            String description )
    {
        super( name, mutable, description );
        this.defaultValue = defaultValue;
    }

    @Override
    public String getStringValue()
    {
        return getValue();
    }

    @Override
    public String getType()
    {
        return "java.lang.String";
    }

    public String getValue()
    {
        return value == null ? defaultValue : value;
    }

    /**
     * @return Returns the defaultValue.
     */
    public String getDefaultValue()
    {
        return defaultValue;
    }

    @Override
    public Object getObjectValue()
    {
        return getValue();
    }

    @Override
    public void setObjectValue( Object object )
    {
        if ( object == null ) {
            value = null;
            return;
        }

        if ( !(object instanceof String) )
            throw new IllegalArgumentException( "Value for parameter '" + name
                    + "' must be a String" );

        if ( !object.equals( getObjectValue() ) ) {
            // Only notify if the value is different
            this.value = (String) object;
            setChanged();
        }
    }

    @Override
    public void readConfiguration( Configuration configuration )
    {
        String value = configuration.getString( name, defaultValue );
        setObjectValue( value );
    }

}
