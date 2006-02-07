/**
 * 
 */
package rtspproxy.jmx.mbeans;

/**
 * management interface to PortrangeRtpServerSession instances
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 * @see rtspproxy.rtp.range.PortrangeRtpServerSession
 */
public interface PortrangeRtpSessionMBean {
	/**
	 * query RTP bind port
	 */
	public int getRtpPort();
	
	/**
	 * query RTCP bind port
	 */
	public int getRtcpPort();
	
	/**
	 * get the state
	 */
	public boolean isActive();
	
	/**
	 * query idle time
	 */
	public long getIdleTime();
	
	/**
	 * 	query the number of IoSession open
	 */
	public int getNumOpenSessions();
}
