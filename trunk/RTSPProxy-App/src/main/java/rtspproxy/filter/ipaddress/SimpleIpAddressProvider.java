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

package rtspproxy.filter.ipaddress;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.Configuration;

import rtspproxy.config.ListParameter;
import rtspproxy.filter.ipaddress.IpAddressPattern.Type;
import rtspproxy.lib.Side;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the IpAddressFilter that is based on a list of XML config
 * elements which contain instruction on "allowed" and "denied" addresses and
 * hosts.
 * 
 * @author Matteo Merli
 */
public class SimpleIpAddressProvider implements IpAddressProvider
{

    private static Logger log = LoggerFactory.getLogger( SimpleIpAddressProvider.class );

    private List<IpAddressPattern> rules = new LinkedList<IpAddressPattern>();

    private Side side = Side.Client;

    private final ListParameter<IpAddressPattern> clientRules = new ListParameter<IpAddressPattern>(
            "filters.ipaddress.client-rules.rule", // name
            false, // mutable
            IpAddressPattern.class, // parameter class
            "Client-side IP address filter rules." );

    private final ListParameter<IpAddressPattern> serverRules = new ListParameter<IpAddressPattern>(
            "filters.ipaddress.server-rules.rule", // name
            false, // mutable
            IpAddressPattern.class, // parameter class
            "Server-side IP address filter rules." );

    public void setSide( Side side )
    {
        this.side = side;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.GenericProvider#start()
     */
    public void start() throws Exception
    {
        switch ( side )
        {
        case Client:
            rules.addAll( clientRules.getElementsList() );
            break;

        case Server:
            rules.addAll( serverRules.getElementsList() );
            break;
            
        default:
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.GenericProvider#stop()
     */
    public void stop()
    {
        rules.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.auth.IpAddressProvider#isBlocked(java.net.InetAddress)
     */
    public boolean isBlocked( InetAddress address )
    {
        boolean blocked = true; // by default the address is blocked
        String[] hostip = address.toString().split( "/" );
        String host = hostip[0];
        String ip = hostip[1];

        for ( IpAddressPattern rule : rules ) {
            if ( blocked && rule.getType() == Type.Deny )
                // Don't need to check, up to now this IP is already
                // blocked
                continue;

            if ( rule.getPattern().matcher( ip ).matches()
                    || rule.getPattern().matcher( host ).matches() )
                // the address matches the pattern
                // check if it's allow or deny
                blocked = (rule.getType() == Type.Allow) ? false : true;
       }

        return blocked;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.GenericProvider#configure(org.apache.commons.configuration.Configuration)
     */
    public void configure( Configuration configuration ) throws Exception
    {
        clientRules.readConfiguration( configuration );
        serverRules.readConfiguration( configuration );

	log.debug( "clientRules: {}", clientRules.getElementsList() );
	log.debug( "serverRules: {}", serverRules.getElementsList() );
    }

}
