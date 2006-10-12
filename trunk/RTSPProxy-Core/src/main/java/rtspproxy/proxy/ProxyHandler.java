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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoFuture;
import org.apache.mina.common.IoFutureListener;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.TrafficMask;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.filter.executor.ExecutorExecutor;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.ProxyServiceRegistry;
import rtspproxy.RdtClientService;
import rtspproxy.RdtServerService;
import rtspproxy.RtcpClientService;
import rtspproxy.RtcpServerService;
import rtspproxy.RtpClientService;
import rtspproxy.RtpServerService;
import rtspproxy.config.Config;
import rtspproxy.filter.RtspServerFilters;
import rtspproxy.proxy.track.RdtTrack;
import rtspproxy.proxy.track.RtpTrack;
import rtspproxy.rtp.range.PortrangeRtpServerSession;
import rtspproxy.rtp.range.PortrangeRtpServerSessionFactory;
import rtspproxy.rtsp.RtspCode;
import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;
import rtspproxy.rtsp.RtspTransport;
import rtspproxy.rtsp.RtspTransportList;
import rtspproxy.rtsp.RtspTransport.LowerTransport;
import rtspproxy.rtsp.RtspTransport.TransportProtocol;

/**
 * @author Matteo Merli
 */
public class ProxyHandler implements IoFutureListener
{
    
    final static Logger log = LoggerFactory.getLogger( ProxyHandler.class );
    
    /** Used to save a reference to this handler in the IoSession */
    protected static final String ATTR = ProxyHandler.class.toString() + "Attr";
    
    protected static final String setupUrlATTR =
            ProxyHandler.class.toString() + "setupUrlATTR";
    
    protected static final String clientPortsATTR =
            ProxyHandler.class.toString() + "clientPortsATTR";
    
    protected static final String clientRdtPortATTR =
            ProxyHandler.class.toString() + "clientRdtPortATTR";
    
    private IoSession clientSession = null;
    
    private IoSession serverSession = null;
    
    private ConcurrentHashMap<String, Object> sharedSessionObjects =
            new ConcurrentHashMap<String, Object>();
    
    private Queue<RtspMessage> outgoingMessages;
    
    /**
     * Creates a new ProxyHandler from a client side protocol session.
     * 
     * @param clientSession
     */
    public ProxyHandler( IoSession clientSession )
    {
        outgoingMessages = new ConcurrentLinkedQueue<RtspMessage>();
        this.clientSession = clientSession;
        this.clientSession.setAttribute(
                                         ProxyConstants.RSTP_SHARED_SESSION_ATTRIBUTE,
                                         sharedSessionObjects );
    }
    
    public void passToServer( RtspMessage message )
    {
        log.debug( "Pass to server" );
        if ( message.hasHeader( "Session" ) )
        {
            ProxySession proxySession =
                    ProxySession.getByClientSessionID( message.getHeader( "Session" ) );
            if ( proxySession == null )
            {
                // Error. The client specified a session ID but it's
                // not valid
                sendError( clientSession, RtspCode.SessionNotFound );
                return;
            }
            
            // Session is Ok
            message.setHeader( "Session", proxySession.getServerSessionId() );
        }
        
        if ( serverSession == null
                && message.getType() == RtspMessage.Type.TypeResponse )
        {
            log.error( "We can't send a response message to "
                    + "an uninitialized serverSide" );
            return;
        }
        else if ( serverSession == null )
        {
            RtspRequest request = (RtspRequest) message;
            try
            {
                connectServerSide( request );
                
            } catch ( IOException e )
            {
                log.error( "I/O exception", e );
                // closeAll();
            } finally
            {
                log.debug( "Server session != null: {}", serverSession != null );
                if ( serverSession == null ) return;
            }
        }
        
        log.debug( "The server should be already connected..." );
        
        switch ( message.getType() )
        {
        case TypeRequest:
            serverSession.setAttribute( RtspMessage.lastRequestVerbATTR,
                                        ((RtspRequest) message).getVerb() );
            sendMessage( serverSession, (RtspRequest) message );
            break;
        
        case TypeResponse:
            sendMessage( serverSession, (RtspResponse) message );
            break;
        
        default:
            log.error( "Message type not valid: " + message.getType() );
        }
    }
    
    public void passToClient( RtspMessage message )
    {
        log.debug( "Pass to client" );
        if ( message.getHeader( "Session" ) != null )
        {
            ProxySession proxySession =
                    ProxySession.getByServerSessionID( message.getHeader( "Session" ) );
            
            if ( proxySession == null )
            {
                if ( message.getType() == RtspMessage.Type.TypeResponse )
                {
                    // create a proxy session on the fly if message is a
                    // response. Certain mobile handset clients
                    // tend to start a RSTP session without its own session id
                    // and wait for the session object from the
                    // remote server
                    proxySession = new ProxySession();
                    
                    proxySession.setServerSessionId( message.getHeader( "Session" ) );
                    message.setHeader( "Session",
                                       proxySession.getClientSessionId() );
                    log.debug( "Created a new proxy session on-the-fly." );
                }
                else
                {
                    // Error. The client specified a session ID but it's
                    // not valid
                    sendError( clientSession, RtspCode.SessionNotFound );
                    return;
                }
            }
            
            // Session is Ok
            message.setHeader( "Session", proxySession.getClientSessionId() );
        }
        
        switch ( message.getType() )
        {
        case TypeRequest:
            clientSession.setAttribute( RtspMessage.lastRequestVerbATTR,
                                        ((RtspRequest) message).getVerb() );
            sendMessage( clientSession, (RtspRequest) message );
            break;
        
        case TypeResponse:
            sendMessage( clientSession, (RtspResponse) message );
            break;
        
        default:
            log.error( "Message type not valid: " + message.getType() );
        }
    }
    
    /**
     * A SETUP request should treated more carefully tha other RTSP requests.
     * The proxy will perform some hijacking on the communication between client
     * and server, such as modifying RTP/RTCP port.
     * 
     * @param request
     *            SETUP request message
     */
    public void passSetupRequestToServer( RtspRequest request )
    {
        ProxySession proxySession = null;
        PortrangeRtpServerSession portrangeRtpSession = null;
        
        if ( request.getHeader( "Session" ) != null )
        {
            // The client already specified a session ID.
            // Let's validate it
            proxySession =
                    ProxySession.getByClientSessionID( request.getHeader( "Session" ) );
            if ( proxySession != null )
            {
                // Session ID is ok
                request.setHeader( "Session", proxySession.getServerSessionId() );
            }
            else
            {
                // Error. The client specified a session ID but it's
                // not valid
                log.debug( "Invalid sessionId: {}",
                           request.getHeader( "Session" ) );
                sendError( clientSession, RtspCode.SessionNotFound );
                return;
            }
        }
        
        if ( serverSession == null )
        {
            /**
             * A mobile handset client may start the RTSP dialogue directly with
             * a SETUP request if it has discovered the streaming media
             * characteristics through any other mechanism. --> Make sure a
             * server-side session exists in this case.
             */
            try
            {
                connectServerSide( request );
            } catch ( IOException e )
            {
                log.error( "I/O exception", e );
                // closeAll();
            } finally
            {
                if ( serverSession == null ) return;
            }
        }
        serverSession.setAttribute( RtspMessage.lastRequestVerbATTR,
                                    request.getVerb() );
        
        log.debug( "Client Transport:{}", request.getHeader( "Transport" ) );
        
        RtspTransportList rtspTransportList =
                new RtspTransportList( request.getHeader( "Transport" ) );
        log.debug( "Parsed: [{}]", rtspTransportList );
        
        if ( rtspTransportList.count() == 0 )
        {
            /**
             * If no one of the client specified transports is acceptable by the
             * proxy, direct reply with an unsupported transport error. Then the
             * client will have the chance to reformule the request with another
             * transports set.
             */
            log.debug( "No supported transport was found." );
            sendError( clientSession, RtspCode.UnsupportedTransport );
            return;
        }
        
        // I'm saving the client Transport header before modifying it,
        // because I will need to know which port the client will
        // use for RTP/RTCP connections.
        clientSession.setAttribute( setupUrlATTR, request.getUrl().toString() );
        
        for ( RtspTransport transport : rtspTransportList.getList() )
        {
            log.debug( "Transport: {}", transport );
            
            if ( transport.getLowerTransport() == LowerTransport.TCP )
            {
                log.debug( "Transport is TCP based." );
            }
            else
            {
                if ( transport.getTransportProtocol() == TransportProtocol.RTP )
                {
                    
                    clientSession.setAttribute( clientPortsATTR,
                                                transport.getClientPort() );
                    
                    int proxyRtpPort = RtpServerService.getInstance().getPort();
                    int proxyRtcpPort =
                            RtcpServerService.getInstance().getPort();
                    
                    if ( Config.proxyServerRtpMultiplePorts.getValue() )
                    {
                        log.debug( "using RTP port range" );
                        
                        try
                        {
                            portrangeRtpSession =
                                    PortrangeRtpServerSessionFactory.getInstance().getSession();
                            
                            proxyRtpPort = portrangeRtpSession.getRtpPort();
                            proxyRtcpPort = portrangeRtpSession.getRtcpPort();
                            
                            log.debug(
                                       "setting local server RTP/RTCP ports to {}/{}",
                                       proxyRtpPort, proxyRtcpPort );
                        } catch ( IOException ioe )
                        {
                            log.info(
                                      "failed to allocate local RTP/RTCP ports",
                                      ioe );
                            
                            sendError( clientSession,
                                       RtspCode.InternalServerError );
                            return;
                        }
                    }
                    transport.setClientPort( new int[] { proxyRtpPort,
                                                        proxyRtcpPort } );
                    
                    // offer a distinguished SSRC to the remote server
                    if ( Config.proxyRtspOfferSsrcToServer.getValue() )
                    {
                        String ssrc =
                                ProxySession.newServerSessionID().toHexString();
                        log.debug(
                                   "offering generated SSRC to remote server, ssrc={}",
                                   ssrc );
                        
                        transport.setSSRC( ssrc );
                    }
                    
                }
                else if ( transport.getTransportProtocol() == TransportProtocol.RDT )
                {
                    clientSession.setAttribute( clientRdtPortATTR, new Integer(
                            transport.getClientPort()[0] ) );
                    
                    int proxyRdtPort = RdtServerService.getInstance().getPort();
                    transport.setClientPort( proxyRdtPort );
                }
                log.debug( "Transport Rewritten: {}", transport );
            }
        }
        
        if ( proxySession == null )
        {
            proxySession = new ProxySession();
            clientSession.setAttribute( ProxySession.ATTR, proxySession );
        }
        if ( portrangeRtpSession != null )
            proxySession.setPortrangeRtpServerSession( portrangeRtpSession );
        
        request.setHeader( "Transport", rtspTransportList.toString() );
        
        log.debug( "Sending SETUP request: \n{}", request );
        
        sendMessage( serverSession, request );
    }
    
    /**
     * Forward a RTSP SETUP response message to client.
     * 
     * @param response
     *            Setup response message
     */
    public void passSetupResponseToClient( RtspResponse response )
    {
        // If there isn't yet a proxySession, create a new one
        ProxySession proxySession =
                ProxySession.getByServerSessionID( response.getHeader( "Session" ) );
        if ( proxySession == null )
        {
            proxySession =
                    (ProxySession) clientSession.getAttribute( ProxySession.ATTR );
            if ( proxySession == null )
            {
                proxySession = new ProxySession();
                clientSession.setAttribute( ProxySession.ATTR, proxySession );
            }
        }
        
        if ( proxySession.getServerSessionId() == null )
        {
            proxySession.setServerSessionId( response.getHeader( "Session" ) );
        }
        
        // Modify transport parameters for the client.
        RtspTransportList rtspTransportList =
                new RtspTransportList( response.getHeader( "Transport" ) );
        
        RtspTransport transport = rtspTransportList.getList().get( 0 );
        log.debug( "Using Transport: {}", transport );
        
        if ( transport.getTransportProtocol() == TransportProtocol.RTP )
        {
            
            // Create a new Track object
            RtpTrack track =
                    proxySession.addRtpTrack(
                                              (String) clientSession.getAttribute( setupUrlATTR ),
                                              transport.getSSRC() );
            
            // Setting client and server info on the track
            InetAddress serverAddress = null;
            if ( transport.getSource() != null )
            {
                try
                {
                    serverAddress =
                            InetAddress.getByName( transport.getSource() );
                } catch ( UnknownHostException e )
                {
                    log.warn( "Unknown host: " + transport.getSource() );
                }
            }
            else
            {
                serverAddress =
                        ((InetSocketAddress) serverSession.getRemoteAddress()).getAddress();
            }
            int[] serverPorts = transport.getServerPort();
            track.setServerAddress( serverAddress, serverPorts[0],
                                    serverPorts[1] );
            
            InetAddress clientAddress = null;
            try
            {
                clientAddress =
                        Inet4Address.getByName( ((InetSocketAddress) clientSession.getRemoteAddress()).getHostName() );
            } catch ( UnknownHostException e )
            {
                log.warn( "Unknown host: " + clientSession.getRemoteAddress() );
            }
            int clientPorts[] =
                    (int[]) clientSession.getAttribute( clientPortsATTR );
            track.setClientAddress( clientAddress, clientPorts[0],
                                    clientPorts[1] );
            
            if ( transport.getLowerTransport() == RtspTransport.LowerTransport.TCP )
            {
                log.debug( "Transport is TCP based." );
            }
            else
            {
                transport.setSSRC( track.getProxySSRC().toHexString() );
                int rtpPort = RtpClientService.getInstance().getPort();
                int rtcpPort = RtcpClientService.getInstance().getPort();
                transport.setServerPort( new int[] { rtpPort, rtcpPort } );
                transport.setSource( RtpClientService.getInstance().getAddress().getHostAddress() );
                
                // Obtaing client specified ports
                int ports[] =
                        (int[]) clientSession.getAttribute( clientPortsATTR );
                transport.setClientPort( ports );
                
                log.debug( "Transport Rewritten: {}", transport );
            }
            
        }
        else if ( transport.getTransportProtocol() == TransportProtocol.RDT )
        {
            
            // Create a new Track object
            RdtTrack track =
                    proxySession.addRdtTrack( (String) clientSession.getAttribute( setupUrlATTR ) );
            // Setting client and server info on the track
            InetAddress serverAddress = null;
            if ( transport.getSource() != null )
            {
                try
                {
                    serverAddress =
                            InetAddress.getByName( transport.getSource() );
                } catch ( UnknownHostException e )
                {
                    log.warn( "Unknown host: " + transport.getSource() );
                }
            }
            else
            {
                serverAddress =
                        ((InetSocketAddress) serverSession.getRemoteAddress()).getAddress();
            }
            int[] serverPorts = transport.getServerPort();
            track.setServerAddress( serverAddress, serverPorts[0] );
            
            InetAddress clientAddress = null;
            try
            {
                clientAddress =
                        Inet4Address.getByName( ((InetSocketAddress) clientSession.getRemoteAddress()).getHostName() );
            } catch ( UnknownHostException e )
            {
                log.warn( "Unknown host: " + clientSession.getRemoteAddress() );
            }
            int clientRdtPort =
                    ((Integer) clientSession.getAttribute( clientRdtPortATTR )).intValue();
            track.setClientAddress( clientAddress, clientRdtPort );
            
            if ( transport.getLowerTransport() == RtspTransport.LowerTransport.TCP )
            {
                log.debug( "Transport is TCP based." );
            }
            else
            {
                int rdtPort = RdtClientService.getInstance().getPort();
                transport.setServerPort( rdtPort );
                // transport.setSource(
                // RdtClientService.getInstance().getAddress().getHostAddress()
                // );
                
                // Obtaing client specified ports
                int port =
                        ((Integer) clientSession.getAttribute( clientRdtPortATTR )).intValue();
                transport.setClientPort( port );
                
                log.debug( "Transport Rewritten: {}", transport );
            }
            
        }
        else
        {
            sendError( clientSession, RtspCode.UnsupportedTransport );
            return;
        }
        
        response.setHeader( "Session", proxySession.getClientSessionId() );
        response.setHeader( "Transport", transport.toString() );
        
        log.debug( "SENDING RESPONSE TO CLIENT:\n{}", response );
        
        sendMessage( clientSession, response );
    }
    
    /**
     * Tries to connect to remote RTSP server.
     * 
     * @param url
     *            the URI of the server
     * @throws IOException
     */
    private void connectServerSide( RtspRequest request ) throws IOException
    {
        URL url = request.getUrl();
        outgoingMessages.add( request );
        
        log.debug( "Connect to Server url: {}", url );
        String host = url.getHost();
        int port = url.getPort();
        if ( port == -1 ) port = url.getDefaultPort();
        
        // Create TCP/IP connector.
        ExecutorExecutor executor =
                new ExecutorExecutor(
                        ProxyServiceRegistry.getInstance().getExecutor() );
        SocketConnector connector = new SocketConnector( 1, executor );
        connector.setFilterChainBuilder( new RtspServerFilters() );
        
        // Start communication.
        InetSocketAddress addr = new InetSocketAddress( host, port );
        if ( addr.isUnresolved() )
        {
            log.warn( "Cannot resolve hostname: {}", host );
            sendError( clientSession, RtspCode.DestinationUnreachable );
            clientSession.close();
            return;
        }
        
        log.debug( "Trying to connect to '{}' {}", addr.getAddress(), port );
        
        // Set the traffic mask to none to avoid reception of
        // messages from the client.
        clientSession.setTrafficMask( TrafficMask.NONE );
        
        ConnectFuture future = connector.connect( addr, new ServerSide() );
        future.addListener( this );
        log.debug( "The operation shouldn't be completed..." );
    }
    
    public void operationComplete( IoFuture future )
    {
        ConnectFuture connectFuture = (ConnectFuture) future;
        log.debug( "operation completed" );
        clientSession.setTrafficMask( TrafficMask.ALL );
        
        if ( !connectFuture.isConnected() )
        {
            log.warn( "Destination unreachable: {}",
                      connectFuture.getSession().getRemoteAddress() );
            sendError( clientSession, RtspCode.DestinationUnreachable );
            clientSession.close();
            return;
        }
        
        log.debug( "Connected!" );
        
        serverSession = connectFuture.getSession();
        
        if ( Config.proxyRtspKeepAlive.getValue() )
        {
            log.debug( "Using keep-alive trick." );
            SocketSessionConfig config =
                    (SocketSessionConfig) serverSession.getConfig();
            config.setKeepAlive( true );
        }
        
        // Save current ProxyHandler into the ProtocolSession
        serverSession.setAttribute( ProxyHandler.ATTR, this );
        
        serverSession.setAttribute(
                                    ProxyConstants.RSTP_SHARED_SESSION_ATTRIBUTE,
                                    sharedSessionObjects );
        
        log.debug( "Server session: {}", serverSession.getAttributeKeys() );
        
        // Send pending outgoing messages
        while ( ! outgoingMessages.isEmpty() )
        {
            RtspMessage message = outgoingMessages.poll();
            if (message.getType() == RtspMessage.Type.TypeRequest )
            {
                RtspRequest request = (RtspRequest)message;
                if ( request.getVerb() == RtspRequest.Verb.SETUP )
                {
                    passSetupRequestToServer( request );
                    return;
                }   
            }
            
            passToServer( message );
        }
    }
    
    /**
     * set an object in the shared objects map
     */
    @SuppressWarnings("unchecked")
    public static void setSharedSessionAttribute( IoSession session,
            String name, Object value )
    {
        ConcurrentHashMap<String, Object> map =
                (ConcurrentHashMap<String, Object>) session.getAttribute( ProxyConstants.RSTP_SHARED_SESSION_ATTRIBUTE );
        
        map.put( name, value );
    }
    
    public static Object getSharedSessionAttribute( IoSession session,
            String name )
    {
        ConcurrentHashMap map =
                (ConcurrentHashMap) session.getAttribute( ProxyConstants.RSTP_SHARED_SESSION_ATTRIBUTE );
        return map.get( name );
    }
    
    public static final boolean containsSharedSessionAttribute(
            IoSession session, String name )
    {
        HashMap map =
                (HashMap) session.getAttribute( ProxyConstants.RSTP_SHARED_SESSION_ATTRIBUTE );
        return map.containsKey( name );
    }
    
    /**
     * Closes both sides of communication.
     */
    public synchronized void closeAll()
    {
        if ( clientSession != null && clientSession.isConnected() )
            clientSession.close();
        if ( serverSession != null && serverSession.isConnected() )
            serverSession.close();
        
        // Remove ProxySession and Track instances
        if ( clientSession != null )
        {
            ProxySession proxySession =
                    (ProxySession) clientSession.getAttribute( ProxySession.ATTR );
            if ( proxySession != null ) proxySession.close();
        }
    }
    
    /**
     * Sends an RTSP message
     * 
     * @param session
     *            current IoSession
     * @param request
     *            the message
     */
    private void sendMessage( IoSession session, RtspMessage message )
    {
        message.setCommonHeaders();
        try
        {
            session.write( message );
        } catch ( Exception e )
        {
            log.error( "exception sending RTSP message", e.getCause() );
        }
    }
    
    /**
     * Sends an RTSP error message response
     * 
     * @param session
     *            current IoSession
     * @param errorCode
     *            the message
     */
    private void sendError( IoSession session, RtspCode errorCode )
    {
        try
        {
            WriteFuture future =
                    session.write( RtspResponse.errorResponse( errorCode ) );
            future.addListener( CLOSE );
        } catch ( Exception e )
        {
            log.error( "exception sending RTSP error message", e.getCause() );
        }
    }
    
}
