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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.config.Config;

/**
 * Parse the RTSP Transport header field. Reference Grammar:
 * 
 * <pre>
 *                        Transport           =    &quot;Transport&quot; &quot;:&quot;
 *                                                 1\#transport-spec
 *                        transport-spec      =    transport-protocol/profile[/lower-transport]
 *                                                 *parameter
 *                        transport-protocol  =    &quot;RTP&quot;
 *                        profile             =    &quot;AVP&quot;
 *                        lower-transport     =    &quot;TCP&quot; | &quot;UDP&quot;
 *                        parameter           =    ( &quot;unicast&quot; | &quot;multicast&quot; )
 *                                            |    &quot;;&quot; &quot;destination&quot; [ &quot;=&quot; address ]
 *                                            |    &quot;;&quot; &quot;interleaved&quot; &quot;=&quot; channel [ &quot;-&quot; channel ]
 *                                            |    &quot;;&quot; &quot;append&quot;
 *                                            |    &quot;;&quot; &quot;ttl&quot; &quot;=&quot; ttl
 *                                            |    &quot;;&quot; &quot;layers&quot; &quot;=&quot; 1*DIGIT
 *                                            |    &quot;;&quot; &quot;port&quot; &quot;=&quot; port [ &quot;-&quot; port ]
 *                                            |    &quot;;&quot; &quot;client_port&quot; &quot;=&quot; port [ &quot;-&quot; port ]
 *                                            |    &quot;;&quot; &quot;server_port&quot; &quot;=&quot; port [ &quot;-&quot; port ]
 *                                            |    &quot;;&quot; &quot;ssrc&quot; &quot;=&quot; ssrc
 *                                            |    &quot;;&quot; &quot;mode&quot; = &lt;&quot;&gt; 1\#mode &lt;&quot;&gt;
 *                        ttl                 =    1*3(DIGIT)
 *                        port                =    1*5(DIGIT)
 *                        ssrc                =    8*8(HEX)
 *                        channel             =    1*3(DIGIT)
 *                        address             =    host
 *                        mode                =    &lt;&quot;&gt; *Method &lt;&quot;&gt; | Method
 *                     
 *                     
 *                        Example:
 *                          Transport: RTP/AVP;multicast;ttl=127;mode=&quot;PLAY&quot;,
 *                                     RTP/AVP;unicast;client_port=3456-3457;mode=&quot;PLAY&quot;
 * </pre>
 */
public class RtspTransport
{

    private static Logger log = LoggerFactory.getLogger( RtspTransport.class );

    /** Transport Protocol */
    public enum TransportProtocol {
        None,
        /** Real Time Protocol */
        RTP,
        /** RDT: RealNetworks transport protocol */
        RDT, RAW;

        public static TransportProtocol fromString( String transportName )
        {
            if ( "RTP".equalsIgnoreCase( transportName ) )
                return RTP;
            else if ( "RDT".equalsIgnoreCase( transportName )
                    || "x-real-rdt".equalsIgnoreCase( transportName ) )
                return RDT;
            else
                return None;
        }
    }

    /** Profile of the streamed data */
    public enum Profile {
        None,
        /** Audio-Video Profile */
        AVP;

        public static Profile fromString( String profile )
        {
            if ( "AVP".equalsIgnoreCase( profile ) )
                return AVP;

            return None;
        }
    }

    /** Underlying transport protocol */
    public enum LowerTransport {
        None, TCP, UDP;

        public static LowerTransport fromString( String transportName )
        {
            if ( "TCP".equalsIgnoreCase( transportName ) )
                return TCP;
            else if ( "UDP".equalsIgnoreCase( transportName ) )
                return UDP;
            else
                return None;
        }
    }

    /** Delivery method */
    public enum DeliveryType {
        None, unicast, multicast
    }

    /** mode */
    public enum Mode {
        None, PLAY, RECORD;

        public static Mode fromString( String modeName )
        {
            if ( "PLAY".equalsIgnoreCase( modeName ) )
                return PLAY;
            else if ( "RECORD".equalsIgnoreCase( modeName ) )
                return RECORD;
            else {
                log.debug( "unknown mode string passed (ignored): {}", modeName );
                return None;
            }
        }
    }

    TransportProtocol transportProtocol = null;

    Profile profile = null;

    LowerTransport lowerTransport = null;

    DeliveryType deliveryType = null;

    String destination;

    String interleaved;

    int layers;

    boolean append;

    int ttl;

    int[] port = new int[2];

    int[] client_port = new int[2];

    int[] server_port = new int[2];

    String ssrc;

    Mode mode = Mode.None;

    String source;

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
        mode = Mode.None;
        source = null;

        parseTransport( transport );
    }

    private void parseTransport( String transport )
    {
        for ( String tok : transport.split( ";" ) ) {

            // First check for the transport protocol
            if ( tok.startsWith( "RTP" ) || tok.startsWith( "RDT" )
                    || tok.startsWith( "x-real-rdt" ) ) {
                String[] tpl = tok.split( "/" );
                transportProtocol = TransportProtocol.fromString( tpl[0] );
                if ( tpl.length > 1 )
                    try {
                        profile = Profile.valueOf( tpl[1] );
                    } catch ( Exception e ) {
                        profile = Profile.None;
                    }

                if ( profile == Profile.None ) {
                    // Maybe this is a lower transport definition
                    lowerTransport = LowerTransport.fromString( tpl[1] );
                }

                if ( tpl.length > 2 )
                    lowerTransport = LowerTransport.valueOf( tpl[2] );
                continue;
            }

            if ( tok.compareToIgnoreCase( "unicast" ) == 0 )
                deliveryType = DeliveryType.unicast;
            else if ( tok.compareToIgnoreCase( "multicast" ) == 0 )
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
                setMode( Mode.fromString( _getStrValue( tok ) ) );
            else if ( tok.startsWith( "source" ) )
                setSource( _getStrValue( tok ) );
        }

        if ( transportProtocol == TransportProtocol.RTP
                && lowerTransport == LowerTransport.None )
            // If it's not specified, let's assume UDP
            setLowerTransport( LowerTransport.UDP );

        if ( transportProtocol == TransportProtocol.RTP
                && deliveryType == DeliveryType.None )
            // If it's not specified, let's assume unicast
            setDeliveryType( DeliveryType.unicast );

        if ( transportProtocol == TransportProtocol.RDT
                && deliveryType == DeliveryType.None )
            // If it's not specified, let's assume unicast
            setDeliveryType( DeliveryType.unicast );

    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        if ( transportProtocol == TransportProtocol.RDT ) {
            // RDT is a little bit "special"
            sb.append( "x-real-rdt" );

            if ( lowerTransport != LowerTransport.None )
                sb.append( '/' ).append( lowerTransport.toString().toLowerCase() );

            if ( deliveryType == DeliveryType.multicast )
                sb.append( "/mcast" );

        } else {
            sb.append( transportProtocol );
            if ( profile != Profile.None ) {
                sb.append( '/' ).append( profile );
                if ( !Config.proxyLowerTransportSuppress.getValue()
                        && lowerTransport != LowerTransport.None )
                    sb.append( '/' ).append( lowerTransport );
            }
            if ( deliveryType != DeliveryType.None )
                sb.append( ';' ).append( deliveryType );
        }
        if ( destination != null )
            sb.append( ";destination=" ).append( destination );
        if ( interleaved != null )
            sb.append( ";interleaved=" ).append( interleaved );
        if ( append )
            sb.append( ";append" );
        if ( layers > 0 )
            sb.append( ";layers=" ).append( layers );
        if ( ttl > 0 )
            sb.append( ";ttl=" ).append( ttl );
        if ( port[0] > 0 ) {
            sb.append( ";port=" ).append( port[0] );
            if ( port[1] > 0 )
                sb.append( '-' ).append( port[1] );
        }
        if ( client_port[0] > 0 ) {
            sb.append( ";client_port=" ).append( client_port[0] );
            if ( client_port[1] > 0 )
                sb.append( '-' ).append( client_port[1] );
        }
        if ( server_port[0] > 0 ) {
            sb.append( ";server_port=" ).append( server_port[0] );
            if ( server_port[1] > 0 )
                sb.append( '-' ).append( server_port[1] );
        }

        if ( !Config.proxyRtspTransportSsrcDisable.getValue() )
            if ( ssrc != null )
                sb.append( ";ssrc=" ).append( ssrc );
        if ( !Config.proxyRtspTransportSourceDisable.getValue() )
            if ( source != null )
                sb.append( ";source=" ).append( source );

        if ( mode != Mode.None )
            sb.append( ";mode=\"" ).append( mode ).append( '"' );
        return sb.toString();
    }

    /**
     * Test if the specified transport can be used by the proxy.
     * 
     * @return
     */
    public boolean isSupportedByProxy()
    {
        /*
         * At now, the only transport supported by the server is
         * "RTP/AVP/UDP;unicast"
         */
        if ( Config.proxyTransportRtpEnable.getValue()
                && transportProtocol == TransportProtocol.RTP && profile == Profile.AVP
                && lowerTransport == LowerTransport.UDP
                && deliveryType == DeliveryType.unicast )
            return true;
        else if ( Config.proxyTransportRdtEnable.getValue()
                && transportProtocol == TransportProtocol.RDT
                && lowerTransport == LowerTransport.UDP
                && deliveryType == DeliveryType.unicast )
            return true;
        else
            return false;
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
     *            The append to set.
     */
    public void setAppend( boolean append )
    {
        this.append = append;
    }

    /**
     * @return Returns the client_port.
     */
    public int[] getClientPort()
    {
        return client_port;
    }

    /**
     * @param client_port
     *            The client_port to set.
     */
    public void setClientPort( int[] client_port )
    {
        this.client_port = client_port;
    }

    /**
     * @param client_port
     *            The client_port to set.
     */
    public void setClientPort( int client_port )
    {
        this.client_port = new int[] { client_port, 0 };
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
     *            The deliveryType to set.
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
     *            The destination to set.
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
     *            The interleaved to set.
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
     *            The layers to set.
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
     *            The lowerTransport to set.
     */
    public void setLowerTransport( LowerTransport lowerTransport )
    {
        this.lowerTransport = lowerTransport;
    }

    /**
     * @return Returns the mode.
     */
    public Mode getMode()
    {
        return mode;
    }

    /**
     * Set the mode. The
     * 
     * @param mode
     *            The mode to set.
     */
    public void setMode( Mode mode )
    {
        this.mode = mode;
    }

    /**
     * @return Returns the port.
     */
    public int[] getPort()
    {
        return port;
    }

    /**
     * @param port
     *            The port to set.
     */
    public void setPort( int[] port )
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
     *            The profile to set.
     */
    public void setProfile( Profile profile )
    {
        this.profile = profile;
    }

    /**
     * @return Returns the server_port.
     */
    public int[] getServerPort()
    {
        return server_port;
    }

    /**
     * @param server_port
     *            The server_port to set.
     */
    public void setServerPort( int[] server_port )
    {
        this.server_port = server_port;
    }

    /**
     * @param server_port
     *            The server_port to set.
     */
    public void setServerPort( int server_port )
    {
        this.server_port = new int[] { server_port, 0 };
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
     *            The ssrc to set.
     */
    public void setSSRC( String ssrc )
    {
        this.ssrc = ssrc;
    }

    /**
     * @param ssrc
     *            The ssrc to set.
     */
    public void setSSRC( long ssrc )
    {
        this.ssrc = Long.toHexString( ssrc & 0xFFFFFFFFL ).toUpperCase();
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
     *            The transportProtocol to set.
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
     *            The ttl to set.
     */
    public void setTTL( int ttl )
    {
        this.ttl = ttl;
    }

    public void setSource( String source )
    {
        this.source = source;
    }

    public String getSource()
    {
        return source;
    }

    /**
     * Get the value part in a string like:
     * 
     * <pre>
     * key = value
     * </pre>
     * 
     * @param str
     *            the content string
     * @return a String containing only the value
     */
    private static String _getStrValue( String str )
    {
        String val = null;

        String[] list = str.split( "=" );
        if ( list.length != 2 )
            return null;

        val = list[1];
        if ( val.startsWith( "\"" ) && val.endsWith( "\"" ) )
            val = val.substring( 1, val.length() - 2 );

        return val;
    }

    /**
     * Get the value part in a string like:
     * 
     * <pre>
     * key = 6344 - 6345
     * </pre>
     * 
     * @param str
     *            the content string
     * @return a int[2] containing only the value
     */
    private static int[] _getPairValue( String str )
    {
        int[] pair = { 0, 0 };
        String[] list = str.split( "=" );
        if ( list.length != 2 )
            return pair;

        try {
            pair[0] = Integer.parseInt( list[1].split( "-" )[0] );
            pair[1] = Integer.parseInt( list[1].split( "-" )[1] );

            // log.debug("Client ports: {}", 1);
            // Integers.parse();

        } catch ( Exception e ) {
            return pair;
        }
        return pair;
    }
}
