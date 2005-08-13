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

package rtspproxy.rtsp;

import java.util.HashMap;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 * RTSP is primarly a connection-less protocol, that means that RTSP request can
 * be made over multiples TCP connections. To identify such a "session", a
 * 64-bit identifier is used.
 */
public class RtspSession
{

	static Logger log = Logger.getLogger( RtspSession.class );

	// Static access

	private static HashMap<Long, RtspSession> sessions = new HashMap<Long, RtspSession>();

	/**
	 * Creates a new empty RtspSession and stores it.
	 * 
	 * @param id
	 *        Session identifier
	 * @return The newly created session
	 */
	static public RtspSession create( String id )
	{
		long key = Long.valueOf( id );
		RtspSession session = new RtspSession( key );
		if ( sessions.get( key ) != null ) {
			log.error( "Session key conflit!!" );
			return null;
		}
		sessions.put( key, session );
		log.debug( "New session created - id=" + key );
		return session;
	}

	static public RtspSession create()
	{
		return create( Long.toString( newSessionID() ) );
	}

	/**
	 * Access an opened session.
	 * 
	 * @param id
	 *        Session identifier
	 * @return The RtspSession identified by \a id or null if not present
	 */
	static public RtspSession get( String id )
	{
		if ( id == null )
			return null;

		long key = Long.valueOf( id );
		return sessions.get( key );
	}

	/**
	 * Close a session and remove resources.
	 * 
	 * @param id
	 *        Session identifier
	 */
	static public void close( String id )
	{
		long key = Long.valueOf( id );
		close( key );
	}

	static public void close( long id )
	{
		sessions.remove( id );
	}

	// Members
	private long id;

	private HashMap<String, Track> tracks = new HashMap<String, Track>();

	private RtspSession( long id )
	{
		this.id = id;
	}

	public long getId()
	{
		return id;
	}

	public Track getTrack( String control )
	{
		return tracks.get( control );
	}

	public int getTracksCount()
	{
		return tracks.size();
	}

	public void addTrack( Track track )
	{
		String control = track.getControl();
		tracks.put( control, track );
	}

	// / Session ID generation

	private static Random random = new Random();

	private static long newSessionID()
	{
		long id = random.nextLong();
		if ( sessions.get( id ) != null ) {
			log.error( "Duplicate session id " );
			return random.nextLong();
		}
		return id;
	}
}
