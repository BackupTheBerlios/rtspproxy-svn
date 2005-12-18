package rtspproxy.proxy.track;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;

import rtspproxy.RtpClientService;
import rtspproxy.RtpServerService;
import rtspproxy.lib.number.UnsignedInt;
import rtspproxy.rtp.RtpPacket;
import rtspproxy.rtp.rtcp.RtcpPacket;

public class RtpTrack extends Track {
	
	private static Logger log = Logger.getLogger( RtpTrack.class );

	/** Maps a server SSRC id to a Track */
	private static Map<UnsignedInt, RtpTrack> serverSsrcMap = new ConcurrentHashMap<UnsignedInt, RtpTrack>();
	
	/** Keeps track of the SSRC IDs used by the proxy, to avoid collisions. */
	private static Set<UnsignedInt> proxySsrcList = Collections.synchronizedSet( new HashSet<UnsignedInt>() );
	
	
	/**
	 * Get the track by looking at server SSRC id.
	 * 
	 * @return a Track instance if a matching SSRC is found or null
	 */
	public static RtpTrack getByServerSSRC( UnsignedInt serverSsrc )
	{
		return serverSsrcMap.get( serverSsrc );
	}
	
	
	/** SSRC id given by the server */
	private UnsignedInt serverSSRC = new UnsignedInt( 0 );
	/** SSRC id selected by the proxy */
	private UnsignedInt proxySSRC = new UnsignedInt( 0 );
	
	/**
	 * Cached references to IoSession objects used to send packets to server and
	 * client.
	 */
	private IoSession rtpServerSession = null;
	private IoSession rtcpServerSession = null;
	private IoSession rtpClientSession = null;
	private IoSession rtcpClientSession = null;
	
	private int clientRtpPort;
	private int clientRtcpPort;
	private int serverRtpPort;
	private int serverRtcpPort;
	
	/**
	 * Construct a new Track.
	 * 
	 * @param url
	 *        the control name for this track.
	 */
	public RtpTrack( String url )
	{
		super( url );
		setProxySSRC( newSSRC() );
	}
	
	/**
	 * @return the SSRC id used byt the proxy
	 */
	public UnsignedInt getProxySSRC()
	{
		return proxySSRC;
	}

	/**
	 * Sets the proxy SSRC id.
	 * 
	 * @param proxySSRC
	 */
	public void setProxySSRC( String proxySSRC )
	{
		try {
			this.proxySSRC = UnsignedInt.fromString( proxySSRC, 16 );

			proxySsrcList.add( this.proxySSRC );
		} catch ( NumberFormatException nfe ) {
			log.debug( "Cannot convert " + proxySSRC + " to integer." );
			throw nfe;
		}
	}

	/**
	 * @return the server SSRC id
	 */
	public UnsignedInt getServerSSRC()
	{
		return serverSSRC;
	}

	/**
	 * Sets the server SSRC id.
	 * 
	 * @param serverSSRC
	 */
	public void setServerSSRC( String serverSSRC )
	{
		this.serverSSRC = UnsignedInt.fromString( serverSSRC, 16 );
		serverSsrcMap.put( this.serverSSRC, this );
	}

	/**
	 * Sets the server SSRC id.
	 * 
	 * @param serverSSRC
	 */
	public void setServerSSRC( UnsignedInt serverSSRC )
	{
		this.serverSSRC = serverSSRC;
		serverSsrcMap.put( this.serverSSRC, this );
	}

	
	public void setRtcpClientSession( IoSession rtcpClientSession )
	{
		this.rtcpClientSession = rtcpClientSession;
	}

	public void setRtcpServerSession( IoSession rtcpServerSession )
	{
		this.rtcpServerSession = rtcpServerSession;
	}

	public void setRtpClientSession( IoSession rtpClientSession )
	{
		this.rtpClientSession = rtpClientSession;
	}

	public void setRtpServerSession( IoSession rtpServerSession )
	{
		this.rtpServerSession = rtpServerSession;
	}
	
	/**
	 * Forwards a RTP packet to server. The packet will be set to the address
	 * indicated by the server at RTP (even) port.
	 * 
	 * @param packet
	 *        a RTP packet
	 */
	public void forwardRtpToServer( RtpPacket packet )
	{
		// modify the SSRC for the server
		packet.setSsrc( proxySSRC );

		if ( rtpServerSession == null )
			rtpServerSession = RtpServerService.newRtpSession( new InetSocketAddress(
					serverAddress, serverRtpPort ) );

		rtpServerSession.write( packet.toByteBuffer() );
	}

	/**
	 * Forwards a RTCP packet to server. The packet will be set to the address
	 * indicated by the server at RTCP (odd) port.
	 * 
	 * @param packet
	 *        a RTCP packet
	 */
	public void forwardRtcpToServer( RtcpPacket packet )
	{
		// modify the SSRC for the server
		packet.setSsrc( proxySSRC );

		if ( rtcpServerSession == null )
			rtcpServerSession = RtpServerService.newRtcpSession( new InetSocketAddress(
					serverAddress, serverRtcpPort ) );

		rtcpServerSession.write( packet.toByteBuffer() );
	}

	/**
	 * Forwards a RTP packet to client. The packet will be set to the address
	 * indicated by the client at RTP (even) port.
	 * <p>
	 * TODO: This will be changed to support multiple clients connected to the
	 * same (live) track.
	 * 
	 * @param packet
	 *        a RTP packet
	 */
	public void forwardRtpToClient( RtpPacket packet )
	{
		// modify the SSRC for the client
		packet.setSsrc( proxySSRC );

		if ( rtpClientSession == null ) {
			rtpClientSession = RtpClientService.newRtpSession( new InetSocketAddress(
					clientAddress, clientRtpPort ) );

		}

		rtpClientSession.write( packet.toByteBuffer() );
	}

	/**
	 * Forwards a RTCP packet to client. The packet will be set to the address
	 * indicated by the client at RTCP (odd) port.
	 * <p>
	 * TODO: This will be changed to support multiple clients connected to the
	 * same (live) track.
	 * 
	 * @param packet
	 *        a RTCP packet
	 */
	public void forwardRtcpToClient( RtcpPacket packet )
	{
		// modify the SSRC for the client
		packet.setSsrc( proxySSRC );

		if ( rtcpClientSession == null ) {
			rtcpClientSession = RtpClientService.newRtcpSession( new InetSocketAddress(
					clientAddress, clientRtcpPort ) );

		}

		rtcpClientSession.write( packet.toByteBuffer() );
	}
	

	/**
	 * Set the address of the server associated with this track.
	 * <p>
	 * TODO: This will be changed to support multiple clients connected to the
	 * same (live) track.
	 * 
	 * @param serverHost
	 *        The serverHost to set.
	 * @param rtpPort
	 *        the port number used for RTP packets
	 * @param rtcpPort
	 *        the port number used for RTCP packets
	 */
	public synchronized void setClientAddress( InetAddress clientAddress, int rtpPort,
			int rtcpPort )
	{
		this.clientAddress = clientAddress;
		this.clientRtpPort = rtpPort;
		this.clientRtcpPort = rtcpPort;

		clientAddressMap.put( new InetSocketAddress( clientAddress, rtpPort ), this );
		clientAddressMap.put( new InetSocketAddress( clientAddress, rtcpPort ), this );
	}

	/**
	 * Set the address of the server associated with this track.
	 * 
	 * @param serverHost
	 *        The serverHost to set.
	 * @param rtpPort
	 *        the port number used for RTP packets
	 * @param rtcpPort
	 *        the port number used for RTCP packets
	 */
	public synchronized void setServerAddress( InetAddress serverAddress, int rtpPort,
			int rtcpPort )
	{
		this.serverAddress = serverAddress;
		this.serverRtpPort = rtpPort;
		this.serverRtcpPort = rtcpPort;

		serverAddressMap.put( new InetSocketAddress( serverAddress, rtpPort ), this );
		serverAddressMap.put( new InetSocketAddress( serverAddress, rtcpPort ), this );
	}
	
	public synchronized void close()
	{
		if ( serverSSRC != null )
			serverSsrcMap.remove( serverSSRC );
		serverAddressMap.remove( new InetSocketAddress( serverAddress, serverRtpPort ) );
		serverAddressMap.remove( new InetSocketAddress( serverAddress, serverRtcpPort ) );

		clientAddressMap.remove( new InetSocketAddress( clientAddress, clientRtpPort ) );
		clientAddressMap.remove( new InetSocketAddress( clientAddress, clientRtcpPort ) );

		if ( proxySSRC != null )
			proxySsrcList.remove( proxySSRC );
		log.debug( "Closed track " + url );
	}

	
	// ////////////////

	/** Used in SSRC id generation */
	private static Random random = new Random();

	/**
	 * Creates a new SSRC id that is unique in the proxy.
	 * 
	 * @return the session ID
	 */
	private static String newSSRC()
	{
		long id;
		while ( true ) {
			id = random.nextLong() & 0xFFFFFFFFL;

			if ( !proxySsrcList.contains( id ) ) {
				// Ok, the id is unique
				String ids = Long.toString( id, 16 );
				return ids;
			}
			// try with another id
		}
	}
}
