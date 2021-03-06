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

package rtspproxy.filter.authentication.scheme;

/**
 * Holds the credentials (username and password) sent by the client.
 * 
 * @author Matteo Merli
 */
public class Credentials
{

    protected String userName;

    protected String password;

    public Credentials()
    {
    }

    public Credentials( String userName, String password )
    {
        this.userName = userName;
        this.password = password;
    }

    /**
     * @return Returns the password.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password
     *            The password to set.
     */
    public void setPassword( String password )
    {
        this.password = password;
    }

    /**
     * @return Returns the userName.
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * @param userName
     *            The userName to set.
     */
    public void setUserName( String userName )
    {
        this.userName = userName;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( '(' ).append( userName ).append( ':' );
        sb.append( password ).append( ')' );
        return sb.toString();
    }
}
