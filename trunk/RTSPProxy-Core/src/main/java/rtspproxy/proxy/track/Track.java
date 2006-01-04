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

package rtspproxy.proxy.track;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Track is a part of a RTSP session. A typical RTSP session for a video
 * stream trasmission is composed of 2 tracks: a track for video data and
 * another track for audio data.
 * <p>
 * These two stream are independent and usually are activated by the same
 * <code>PLAY</code> and <code>TEARDOWN</code> requests.
 * 
 * @author Matteo Merli
 */
public abstract class Track
{

	protected static final String ATTR = Track.class.toString() + "Attr";

	/** Maps a client address to a Track */
	protected static Map<InetSocketAddress, Track> clientAddressMap = new ConcurrentHashMap<InetSocketAddress, Track>();

	/** Maps a server address to a Track */
	protected static Map<InetSocketAddress, Track> serverAddressMap = new ConcurrentHashMap<InetSocketAddress, Track>();

	/**
	 * Control Url of the track. This is the url handle given by the server to
	 * control different tracks in a RTSP session.
	 */
	protected String url;

	/**
	 * IP address of client and server.
	 * <p>
	 * TODO: When using reflection, there will be more than one connected client
	 * at a time to the same Track. So the track should keep a list of connected
	 * clients and forward packets to each of them.
	 */
	protected InetAddress clientAddress;

	protected InetAddress serverAddress;

	/**
	 * Construct a new Track.
	 * 
	 * @param url
	 *            the control name for this track.
	 */
	public Track( String url )
	{
		this.url = url;
	}

	/**
	 * Get the track by looking at client socket address.
	 * 
	 * @return a Track instance if a matching pair is found or null
	 */
	public static Track getByClientAddress( InetSocketAddress clientAddress )
	{
		return clientAddressMap.get( clientAddress );
	}

	/**
	 * Get the track by looking at server socket address.
	 * <p>
	 * Used as a workaround for streaming servers which do not hand out a ssrc
	 * in the setup handshake.
	 * 
	 * @return a Track instance if a matching pair is found or null
	 */
	public static Track getByServerAddress( InetSocketAddress serverAddress )
	{
		return serverAddressMap.get( serverAddress );
	}

	// /// Member methods

	public String getUrl()
	{
		return url;
	}

	public void setUrl( String url )
	{
		this.url = url;
	}

	public abstract void close();

	public String toString()
	{
		return "Track(url=\"" + url + "\"";
	}
}
