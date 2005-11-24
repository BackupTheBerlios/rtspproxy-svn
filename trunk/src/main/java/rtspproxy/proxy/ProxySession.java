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

package rtspproxy.proxy;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 * @author Matteo Merli
 */
public class ProxySession
{

	private static Logger log = Logger.getLogger( ProxySession.class );

	/** Map IDs for RTSP session with servers to ProxySession objects. */
	private static Map<String, ProxySession> serverSessionIds = Collections.synchronizedMap( new HashMap<String, ProxySession>() );
	
	/** Map IDs for RTSP session with clients to ProxySession objects. */
	private static Map<String, ProxySession> clientSessionIds = Collections.synchronizedMap( new HashMap<String, ProxySession>() );

	/**
	 * 
	 * @param clientSessionId a string containing the RTSP session ID
	 * @return a ProxySession
	 */
	public static ProxySession getByClientSessionID( String clientSessionId )
	{
		return clientSessionIds.get( clientSessionId );
	}

	public static ProxySession getByServerSessionID( String serverSessionId )
	{
		return serverSessionIds.get( serverSessionId );
	}

	private static Random random = new Random();

	/**
	 * Creates a unique session ID
	 * 
	 * @return the session ID
	 */
	private static String newSessionID()
	{
		String id;
		while ( true ) {
			// Create a 64 bit random number
			id = new BigInteger( 64, random ).toString();

			if ( clientSessionIds.get( id ) == null ) {
				// Ok, the id is unique
				return id;
			}
			// try with another id
		}
	}

	/**
	 * This is the session ID generated by the proxy and used for the
	 * communication with the client.
	 */
	private String clientSessionId = null;

	/**
	 * This is the session ID assigned by the server. RTSP messages with the
	 * server must use this ID.
	 */
	private String serverSessionId = null;

	private Map<String, Track> trackList = Collections.synchronizedMap( new HashMap<String, Track>() );

	public ProxySession()
	{
		setClientSessionId( newSessionID() );
	}

	// Session ID generation

	public Track addTrack( String url, String serverSsrc )
	{
		Track track = new Track( url );
		track.setServerSSRC( serverSsrc );
		trackList.put( url, track );
		return track;
	}

	public String getClientSessionIdString()
	{
		return clientSessionId;
	}

	public String getServerSessionId()
	{
		return serverSessionId;
	}

	public void setClientSessionId( String clientSessionId )
	{
		this.clientSessionId = clientSessionId;
		clientSessionIds.put( clientSessionId, this );
	}

	public void setServerSessionId( String serverSessionId )
	{
		this.serverSessionId = serverSessionId;
		serverSessionIds.put( serverSessionId, this );
	}

}
