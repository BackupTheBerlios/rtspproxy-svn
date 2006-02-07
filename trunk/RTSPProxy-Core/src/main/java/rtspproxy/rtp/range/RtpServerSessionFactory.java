/**
 * 
 */
package rtspproxy.rtp.range;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.BitSet;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.pool.PoolableObjectFactory;

import rtspproxy.config.Config;
import rtspproxy.config.Parameter;
import rtspproxy.jmx.JmxAgent;
import rtspproxy.proxy.ServerRtcpPacketHandler;
import rtspproxy.proxy.ServerRtpPacketHandler;
import rtspproxy.transport.socket.nio.DatagramAcceptor;
import sun.security.krb5.internal.s;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 */
class RtpServerSessionFactory implements PoolableObjectFactory, Observer {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(RtpServerSessionFactory.class);

	// local bind address
	private InetAddress localAddress;
	
	// mark used connections
	private BitSet usedConnections;
	
	// base port (RTP)
	private int basePort;
	
	// max connections
	private int maxConnections;
	
	// RTP acceptor
	private DatagramAcceptor rtpAcceptor = new DatagramAcceptor();
	
	// RTCP acceptor
	private DatagramAcceptor rtcpAcceptor = new DatagramAcceptor();

	// Filter chain builder
	private RtpRtcpFilterChainBuilder chainBuilder = new RtpRtcpFilterChainBuilder();
	
	private long idleTimeout;

	/**
	 * constructor
	 * @param address 
	 * @param maxConn 
	 */
	RtpServerSessionFactory(InetAddress address, int basePort, int maxConn, long idleTimeout, int threadPoolSize) {
		this.localAddress = address;
		this.usedConnections = new BitSet(maxConn);
		this.maxConnections = maxConn;
		this.basePort = basePort;
		this.idleTimeout = idleTimeout;

		this.chainBuilder.setPoolSize(threadPoolSize);
		this.rtpAcceptor.setFilterChainBuilder(this.chainBuilder);
		this.rtcpAcceptor.setFilterChainBuilder(this.chainBuilder);
		
		Config.proxyServerRtpThreadPoolSize.addObserver(this);
		Config.proxyServerRtpIdleTimeout.addObserver(this);
	}

	public Object makeObject() throws Exception {
		PortrangeRtpServerSession serverSession = null;
		int nextCon = 0;

		do {
			nextCon = this.usedConnections.nextClearBit(0);
			logger.debug("found next free slot at " + nextCon);
			
			if(nextCon >= this.maxConnections) {
				logger.debug("failed to allocate a free slot");
				throw new IOException("no local ports available");
			}
			int rtpPort = this.basePort + 2*nextCon;
			
			this.usedConnections.set(nextCon);
			serverSession = new PortrangeRtpServerSession(nextCon);
			
			// try to bind local ports
			InetSocketAddress rtpSockAddr = new InetSocketAddress(this.localAddress, rtpPort);
			InetSocketAddress rtcpSockAddr = new InetSocketAddress(this.localAddress, rtpPort+1);
			
			boolean rtpBound = false;
			boolean rtcpBound = false;
			
			try {
				this.rtpAcceptor.bind(rtpSockAddr, new ServerRtpPacketHandler());
				rtpBound = true;
			} catch(IOException ie) {
				logger.info("failed to bind RTP socket " + rtpSockAddr, ie);
			}
			try {
				this.rtcpAcceptor.bind(rtcpSockAddr, new ServerRtcpPacketHandler());
				rtcpBound = true;
			} catch(IOException ie) {
				logger.info("failed to bind RTCP socket " + rtpSockAddr, ie);
			}
			if(rtpBound == false || rtcpBound == false) {
				logger.debug("failed to allocate RTP/RTCP port port");
				
				serverSession = null;
				if(rtpBound) {
					this.rtpAcceptor.unbind(rtpSockAddr);
				}
				if(rtcpBound) {
					this.rtcpAcceptor.unbind(rtcpSockAddr);
				}
			} else {
				logger.debug("allocated local port pair");
				
				serverSession.setLocalBinding(this.rtpAcceptor, rtpSockAddr, this.rtcpAcceptor, rtcpSockAddr);
			}
			
		} while(serverSession == null);
		
		JmxAgent.getInstance().registerPortrangeRtpServerSession(serverSession);
		
		return serverSession;
	}

	public void destroyObject(Object arg0) throws Exception {
		PortrangeRtpServerSession serverSession = (PortrangeRtpServerSession)arg0;
		int conNumber = serverSession.getConnectionNumber();
		
		logger.debug("destroying connection " + conNumber);
		serverSession.unbind();
		JmxAgent.getInstance().unregisterPortrangeRtpServerSession(serverSession);
		this.usedConnections.clear(conNumber);
	}

	public boolean validateObject(Object arg0) {
		boolean valid = true;
		PortrangeRtpServerSession serverSession = (PortrangeRtpServerSession)arg0;
		long checkPoint = System.currentTimeMillis();
		
		logger.debug("checking validity for connection " + serverSession.getConnectionNumber());
		if(!serverSession.isActive() 
				&& (checkPoint - serverSession.getLastPassiveCheckpoint()) > (1000*this.idleTimeout)) {
			logger.debug("connection " + serverSession.getConnectionNumber() + " timed out");
			valid = false;
		}
		
		return valid;
	}

	public void activateObject(Object arg0) throws Exception {
		PortrangeRtpServerSession serverSession = (PortrangeRtpServerSession)arg0;
		
		serverSession.setActive(true);
	}

	public void passivateObject(Object arg0) throws Exception {
		PortrangeRtpServerSession serverSession = (PortrangeRtpServerSession)arg0;
		
		serverSession.closeOpenSessions();
		serverSession.setActive(false);
	}

	public void update(Observable o, Object arg) {
		if(o instanceof Parameter) {
			Parameter p = (Parameter)o;
			
			if(p.equals(Config.proxyServerRtpThreadPoolSize))
				this.chainBuilder.setPoolSize(Config.proxyServerRtpThreadPoolSize.getValue());
			else if(p.equals(Config.proxyServerRtpIdleTimeout))
				this.idleTimeout = Config.proxyServerRtpIdleTimeout.getValue();
		}
	}
}
