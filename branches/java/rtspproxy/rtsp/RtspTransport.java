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

/**
 * Parse the RTSP Transport header field.
 */
public class RtspTransport
{

	enum TransportProtocol {
		None, RTP, RDT
	}
	enum Profile {
		None, AVP
	}
	enum LowerTransport {
		None, TCP, UDP
	}
	enum DeliveryType {
		None, unicast, multicast
	}

	TransportProtocol transportProtocol;
	Profile profile;
	LowerTransport lowerTransport;
	DeliveryType deliveryType;

	String destination;
	String interleaved;
	int layers;
	boolean append;
	int ttl;
	short[] port = new short[2];
	short[] client_port = new short[2];
	short[] server_port = new short[2];
	String ssrc;
	String mode;

	/**
	 * Constructor. Creates a RtspTransport object from a transport header
	 * string.
	 */
	public RtspTransport( String transport )
	{
		transportProtocol = TransportProtocol.None;
		profile = Profile.None;
		lowerTransport = LowerTransport.None;
		deliveryType = DeliveryType.None;
		destination = null;
		interleaved = null;
		layers = 0;
		append = false;
		ttl = 0;
		port[0] = 0;
		port[1] = 0;
		client_port[0] = 0;
		client_port[1] = 0;
		server_port[0] = 0;
		server_port[1] = 0;
		ssrc = null;
		mode = null;

		parseTransport( transport );
	}

	private void parseTransport( String transport )
	{
		for ( String tok : transport.split( ";" ) ) {

			// First check for the transport protocol
			if ( tok.startsWith( "RTP" ) || tok.startsWith( "RDT" ) ) {
				String[] tpl = tok.split( "/" );
				transportProtocol = TransportProtocol.valueOf( tpl[0] );
				if ( tpl.length > 1 )
					profile = Profile.valueOf( tpl[1] );
				if ( tpl.length > 2 )
					lowerTransport = LowerTransport.valueOf( tpl[2] );
				continue;
			}

			if ( tok.toLowerCase() == "unicast" )
				deliveryType = DeliveryType.unicast;
			else if ( tok.toLowerCase() == "multicast" )
				deliveryType = DeliveryType.multicast;
			else if ( tok.startsWith( "destination" ) )
				setDestination( _getStrValue( tok ) );
			else if ( tok.startsWith( "interleaved" ) )
				setInterleaved( _getStrValue( tok ) );
			else if ( tok.startsWith( "append" ) )
				setAppend( true );
			else if ( tok.startsWith( "layers" ) )
				setLayers( Integer.valueOf( _getStrValue( tok ) ) );
			else if ( tok.startsWith( "ttl" ) )
				setTTL( Integer.valueOf( _getStrValue( tok ) ) );
			else if ( tok.startsWith( "port" ) )
				setPort( _getPairValue( tok ) );
			else if ( tok.startsWith( "client_port" ) )
				setClientPort( _getPairValue( tok ) );
			else if ( tok.startsWith( "server_port" ) )
				setServerPort( _getPairValue( tok ) );
			else if ( tok.startsWith( "ssrc" ) )
				setSSRC( _getStrValue( tok ) );
			else if ( tok.startsWith( "mode" ) )
				setMode( _getStrValue( tok ) );
		}
	}

	public String toString()
	{
		String s = "";
		s += transportProtocol;
		if ( profile != Profile.None ) {
			s += "/" + profile;
			if ( lowerTransport != LowerTransport.None )
				s += "/" + lowerTransport;
		}
		if ( deliveryType != DeliveryType.None )
			s += ";" + deliveryType;
		if ( destination != null )
			s += ";destination=" + destination;
		if ( interleaved != null )
			s += ";interleaved=" + interleaved;
		if ( append )
			s += ";append";
		if ( layers > 0 )
			s += ";layers=" + layers;
		if ( ttl > 0 )
			s += ";ttl=" + ttl;
		if ( port[0] > 0 )
			s += ";port=" + port[0] + "-" + port[1];
		if ( client_port[0] > 0 )
			s += ";client_port=" + client_port[0] + "-" + client_port[1];
		if ( server_port[0] > 0 )
			s += ";server_port=" + server_port[0] + "-" + server_port[1];
		if ( ssrc != null )
			s += ";ssrc=" + ssrc;
		if ( mode != null )
			s += ";mode=" + mode;
		return s;
	}

	/**
	 * @return Returns the append.
	 */
	public boolean isAppend()
	{
		return append;
	}

	/**
	 * @param append
	 *        The append to set.
	 */
	public void setAppend( boolean append )
	{
		this.append = append;
	}

	/**
	 * @return Returns the client_port.
	 */
	public short[] getClientPort()
	{
		return client_port;
	}

	/**
	 * @param client_port
	 *        The client_port to set.
	 */
	public void setClientPort( short[] client_port )
	{
		this.client_port = client_port;
	}

	/**
	 * @return Returns the deliveryType.
	 */
	public DeliveryType getDeliveryType()
	{
		return deliveryType;
	}

	/**
	 * @param deliveryType
	 *        The deliveryType to set.
	 */
	public void setDeliveryType( DeliveryType deliveryType )
	{
		this.deliveryType = deliveryType;
	}

	/**
	 * @return Returns the destination.
	 */
	public String getDestination()
	{
		return destination;
	}

	/**
	 * @param destination
	 *        The destination to set.
	 */
	public void setDestination( String destination )
	{
		this.destination = destination;
	}

	/**
	 * @return Returns the interleaved.
	 */
	public String getInterleaved()
	{
		return interleaved;
	}

	/**
	 * @param interleaved
	 *        The interleaved to set.
	 */
	public void setInterleaved( String interleaved )
	{
		this.interleaved = interleaved;
	}

	/**
	 * @return Returns the layers.
	 */
	public int getLayers()
	{
		return layers;
	}

	/**
	 * @param layers
	 *        The layers to set.
	 */
	public void setLayers( int layers )
	{
		this.layers = layers;
	}

	/**
	 * @return Returns the lowerTransport.
	 */
	public LowerTransport getLowerTransport()
	{
		return lowerTransport;
	}

	/**
	 * @param lowerTransport
	 *        The lowerTransport to set.
	 */
	public void setLowerTransport( LowerTransport lowerTransport )
	{
		this.lowerTransport = lowerTransport;
	}

	/**
	 * @return Returns the mode.
	 */
	public String getMode()
	{
		return mode;
	}

	/**
	 * @param mode
	 *        The mode to set.
	 */
	public void setMode( String mode )
	{
		this.mode = mode;
	}

	/**
	 * @return Returns the port.
	 */
	public short[] getPort()
	{
		return port;
	}

	/**
	 * @param port
	 *        The port to set.
	 */
	public void setPort( short[] port )
	{
		this.port = port;
	}

	/**
	 * @return Returns the profile.
	 */
	public Profile getProfile()
	{
		return profile;
	}

	/**
	 * @param profile
	 *        The profile to set.
	 */
	public void setProfile( Profile profile )
	{
		this.profile = profile;
	}

	/**
	 * @return Returns the server_port.
	 */
	public short[] getServerPort()
	{
		return server_port;
	}

	/**
	 * @param server_port
	 *        The server_port to set.
	 */
	public void setServerPort( short[] server_port )
	{
		this.server_port = server_port;
	}

	/**
	 * @return Returns the ssrc.
	 */
	public String getSSRC()
	{
		return ssrc;
	}

	/**
	 * @param ssrc
	 *        The ssrc to set.
	 */
	public void setSSRC( String ssrc )
	{
		this.ssrc = ssrc;
	}

	/**
	 * @return Returns the transportProtocol.
	 */
	public TransportProtocol getTransportProtocol()
	{
		return transportProtocol;
	}

	/**
	 * @param transportProtocol
	 *        The transportProtocol to set.
	 */
	public void setTransportProtocol( TransportProtocol transportProtocol )
	{
		this.transportProtocol = transportProtocol;
	}

	/**
	 * @return Returns the ttl.
	 */
	public int getTTL()
	{
		return ttl;
	}

	/**
	 * @param ttl
	 *        The ttl to set.
	 */
	public void setTTL( int ttl )
	{
		this.ttl = ttl;
	}

	/**
	 * Get the value part in a string like:
	 * 
	 * <pre>
	 * key=value
	 * </pre>
	 * 
	 * @param str
	 *        the content string
	 * @return a String containing only the value
	 */
	private static String _getStrValue( String str )
	{
		String[] list = str.split( "=" );
		if ( list.length != 2 )
			return null;

		return list[1];
	}

	/**
	 * Get the value part in a string like:
	 * 
	 * <pre>
	 * key=6344-6345
	 * </pre>
	 * 
	 * @param str
	 *        the content string
	 * @return a short[] containing only the value
	 */
	private static short[] _getPairValue( String str )
	{
		short[] pair = { 0, 0 };
		String[] list = str.split( "=" );
		if ( list.length != 2 )
			return pair;

		try {
			pair[0] = Integer.valueOf( list[1].split( "-" )[0] ).shortValue();
			pair[1] = Integer.valueOf( list[1].split( "-" )[1] ).shortValue();

		} catch ( Exception e ) {
			return pair;
		}
		return pair;
	}
}