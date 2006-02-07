/**
 * 
 */
package rtspproxy.jmx.mbeans;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import rtspproxy.jmx.JmxAgent;
import rtspproxy.rtp.range.PortrangeRtpServerSession;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 * @see rtspproxy.rtp.range.PortrangeRtpServerSessionFactory
 */
public class PortrangeRtpSession implements PortrangeRtpSessionMBean {

	private PortrangeRtpServerSession session;
	
	public PortrangeRtpSession(PortrangeRtpServerSession session) {
		this.session = session;
	}
	
	/**
	 * build the object name
	 * @throws NullPointerException 
	 * @throws MalformedObjectNameException 
	 */
	public ObjectName buildName() throws MalformedObjectNameException, NullPointerException {
		Hashtable<String, String> parts = new Hashtable<String, String>();
		
		parts.put("type", "server");
		parts.put("connection", String.valueOf(this.session.getConnectionNumber()));
		
		return ObjectName.getInstance(JmxAgent.RTP_DYNAMIC_SESSION_DOMAIN, parts);
	}
	
	/* (non-Javadoc)
	 * @see rtspproxy.jmx.mbeans.PortrangeRtpSessionMBean#getRtpPort()
	 */
	public int getRtpPort() {
		return this.session.getRtpPort();
	}

	/* (non-Javadoc)
	 * @see rtspproxy.jmx.mbeans.PortrangeRtpSessionMBean#getRtcpPort()
	 */
	public int getRtcpPort() {
		return this.session.getRtcpPort();
	}

	/* (non-Javadoc)
	 * @see rtspproxy.jmx.mbeans.PortrangeRtpSessionMBean#isActive()
	 */
	public boolean isActive() {
		return this.session.isActive();
	}

	/* (non-Javadoc)
	 * @see rtspproxy.jmx.mbeans.PortrangeRtpSessionMBean#idleTime()
	 */
	public long getIdleTime() {
		long idleTime = 0;
		
		if(!this.session.isActive())
			idleTime = (System.currentTimeMillis() - this.session.getLastPassiveCheckpoint()) / 1000;

		return idleTime;
	}

	/* (non-Javadoc)
	 * @see rtspproxy.jmx.mbeans.PortrangeRtpSessionMBean#getNumOpenSessions()
	 */
	public int getNumOpenSessions() {
		return this.session.getNumOpenSessions();
	}

}
