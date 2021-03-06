package rtspproxy.proxy.track;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.RdtClientService;
import rtspproxy.RdtServerService;
import rtspproxy.rdt.RdtPacket;

public class RdtTrack extends Track
{

    private static Logger log = LoggerFactory.getLogger( RdtTrack.class );

    /**
     * Cached references to IoSession objects used to send packets to server and
     * client.
     */
    private IoSession rdtClientSession = null;

    private IoSession rdtServerSession = null;

    private int clientRdtPort;

    private int serverRdtPort;

    /**
     * Construct a new Track.
     * 
     * @param url
     *            the control name for this track.
     */
    public RdtTrack( String url )
    {
        super( url );
    }

    /**
     * Forwards a RDT packet to client. The packet will be set to the address
     * indicated by the client at RDT port.
     * <p>
     * TODO: This will be changed to support multiple clients connected to the
     * same (live) track.
     * 
     * @param packet
     *            a buffer containing a RDT packet
     */
    public void forwardRdtToClient( RdtPacket packet )
    {
        // modify the SSRC for the client
        // packet.setSsrc( proxySSRC );

        if ( rdtClientSession == null ) {
            rdtClientSession = RdtClientService.getInstance().newSession(
                    new InetSocketAddress( clientAddress, clientRdtPort ) );
        }

        log.debug( "Packet: {}", packet );
        rdtClientSession.write( packet );
    }

    /**
     * Forwards a RDT packet to server. The packet will be set to the address
     * indicated by the server at RDT port.
     * 
     * @param packet
     *            a RDT packet
     */
    public void forwardRdtToServer( RdtPacket packet )
    {
        if ( rdtServerSession == null || !rdtServerSession.isConnected() ) {
            InetSocketAddress remoteAddress = new InetSocketAddress( serverAddress,
                    serverRdtPort );
            log.debug( "Creating RDT session to: {}", remoteAddress );
            rdtServerSession = RdtServerService.getInstance().newSession( remoteAddress );
        }

        // log.debug( "Packet: {}", packet );
        // packet.reset();
        // log.debug("Packet: {}", packet );
        // log.debug( "Written bytes1: {}", rdtServerSession.getWrittenBytes() );

        rdtServerSession.write( packet );
        // log.debug( "Written bytes2: {}", rdtServerSession.getWrittenBytes() );
    }

    /**
     * Set the address of the server associated with this track.
     * <p>
     * TODO: This will be changed to support multiple clients connected to the
     * same (live) track.
     * 
     * @param serverHost
     *            The serverHost to set.
     * @param rdtpPort
     *            the port number used for RDT packets
     */
    public synchronized void setClientAddress( InetAddress clientAddress, int rdtPort )
    {
        this.clientAddress = clientAddress;
        this.clientRdtPort = rdtPort;

        InetSocketAddress clientAddr = new InetSocketAddress( clientAddress, rdtPort );

        /*
         * log.debug( "Creating RDT server session to: {}", clientAddr );
         * rdtClientSession = RdtClientService.getInstance().newSession(
         * clientAddr );
         */

        clientAddressMap.put( clientAddr, this );
    }

    /**
     * Set the address of the server associated with this track.
     * 
     * @param serverHost
     *            The serverHost to set.
     * @param rdtPort
     *            the port number used for RDT packets
     */
    public synchronized void setServerAddress( InetAddress serverAddress, int rdtPort )
    {
        this.serverAddress = serverAddress;
        this.serverRdtPort = rdtPort;

        InetSocketAddress remoteAddress = new InetSocketAddress( serverAddress,
                serverRdtPort );

        /*
         * log.debug( "Creating RDT server session to: {}", remoteAddress );
         * rdtServerSession = RdtServerService.getInstance().newSession(
         * remoteAddress );
         */

        serverAddressMap.put( remoteAddress, this );
    }

    @Override
    public synchronized void close()
    {
        serverAddressMap.remove( new InetSocketAddress( serverAddress, serverRdtPort ) );
        clientAddressMap.remove( new InetSocketAddress( clientAddress, clientRdtPort ) );

        log.debug( "Closed track {}", url );
    }
}
