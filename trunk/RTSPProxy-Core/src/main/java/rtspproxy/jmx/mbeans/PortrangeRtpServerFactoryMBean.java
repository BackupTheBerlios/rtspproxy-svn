/**
 * 
 */
package rtspproxy.jmx.mbeans;

/**
 * Management interface to the PortrangeRtpServerSessionFactory instance
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 * @see rtspproxy.rtp.range.PortrangeRtpServerSessionFactory
 */
public interface PortrangeRtpServerFactoryMBean {
	/**
	 * get the maximum number of connections 
	 */
	public int getMaxConnections();
	
	/**
	 * get the current number of idle connections
	 */
	public int getCurrentIdleConnections();
	
	/**
	 * get the current number of active connections
	 */
	public int getCurrentActiveConnections();
}
