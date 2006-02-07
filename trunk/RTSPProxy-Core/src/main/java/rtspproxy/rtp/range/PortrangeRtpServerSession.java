/**
 * 
 */
package rtspproxy.rtp.range;

import java.net.InetSocketAddress;
import java.util.LinkedList;

import javax.management.ObjectName;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.transport.socket.nio.DatagramAcceptor;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class PortrangeRtpServerSession {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(PortrangeRtpServerSession.class);
		
	// session attribute name
	public static final String ATTR = PortrangeRtpServerSession.class.getName() + ".ATTR";
	
	// connection number
	private int connectionNumber;
	
	// flag if session is passive
	private boolean activeIfTrue;
	
	// time when object was passivated
	private long lastPassiveCheckpoint; 
	
	// RTP acceptor
	private DatagramAcceptor rtpAcceptor;
	
	// RTP socket address
	private InetSocketAddress rtpSockAddr;
	
	// RCTP acceptor
	private DatagramAcceptor rtcpAcceptor;
	
	// RTCP socket address
	private InetSocketAddress rtcpSockAddr;
	
	// opened sessions
	private LinkedList<IoSession> openSessions = new LinkedList<IoSession>();
	
	// MBean name
	private ObjectName objectName;
	
	/**
	 * create a sever session
	 */
	PortrangeRtpServerSession(int conNum) {
		this.connectionNumber = conNum;
	}

	public void setLocalBinding(DatagramAcceptor rtpAcceptor, InetSocketAddress rtpSockAddr, 
			DatagramAcceptor rtcpAcceptor, InetSocketAddress rtcpSockAddr) {
		this.rtpAcceptor = rtpAcceptor;
		this.rtpSockAddr = rtpSockAddr;
		this.rtcpAcceptor = rtcpAcceptor;
		this.rtcpSockAddr = rtcpSockAddr;
	}
	
	/**
	 * get the RTP port number
	 */
	public int getRtpPort() {
		return this.rtpSockAddr.getPort();
	}

	/**
	 * get the RTP port number
	 */
	public int getRtcpPort() {
		return this.rtcpSockAddr.getPort();
	}

	/**
	 * get the connection number
	 */
	public int getConnectionNumber() {
		return this.connectionNumber;
	}

	void unbind() {
		this.rtpAcceptor.unbind(this.rtpSockAddr);
		this.rtcpAcceptor.unbind(this.rtcpSockAddr);
	}

	void closeOpenSessions() {
			for (IoSession session : this.openSessions)
			session.close();
		this.openSessions.clear();
	}
	
	public IoSession newRtpSession(InetSocketAddress remote) {
		IoSession session = this.rtpAcceptor.newSession(remote, this.rtpSockAddr);
		
		logger.debug("opened new RTP session to " + remote);
		this.openSessions.add(session);
		
		return session;
	}

	public IoSession newRtcpSession(InetSocketAddress remote) {
		IoSession session = this.rtcpAcceptor.newSession(remote, this.rtcpSockAddr);

		logger.debug("opened new RTCP session to " + remote);
		this.openSessions.add(session);
		
		return session;
	}

	void setActive(boolean state) {
		this.activeIfTrue = state;
		
		if(!this.activeIfTrue)
			this.lastPassiveCheckpoint = System.currentTimeMillis();
	}

	/**
	 * @return Returns the lastPassiveCheckpoint.
	 */
	public long getLastPassiveCheckpoint() {
		return lastPassiveCheckpoint;
	}

	public boolean isActive() {
		return this.activeIfTrue;
	}

	/**
	 * @return Returns the rtpSockAddr.
	 */
	public InetSocketAddress getRtpSocketAddress() {
		return rtpSockAddr;
	}

	/**
	 * @return Returns the rtpSockAddr.
	 */
	public InetSocketAddress getRtcpSocketAddress() {
		return rtcpSockAddr;
	}

	/**
	 * @return Returns the objectName.
	 */
	public ObjectName getObjectName() {
		return objectName;
	}

	/**
	 * @param objectName The objectName to set.
	 */
	public void setObjectName(ObjectName objectName) {
		this.objectName = objectName;
	}

	public int getNumOpenSessions() {
		return this.openSessions.size();
	}

}
