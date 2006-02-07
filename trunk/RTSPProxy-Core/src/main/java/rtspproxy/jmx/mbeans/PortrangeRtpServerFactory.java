/**
 * 
 */
package rtspproxy.jmx.mbeans;

import rtspproxy.rtp.range.PortrangeRtpServerSessionFactory;

/**
 * Management implementation to the PortrangeRtpServerSessionFactory instance
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 * @see rtspproxy.rtp.range.PortrangeRtpServerSessionFactory
 */
public class PortrangeRtpServerFactory implements
		PortrangeRtpServerFactoryMBean {

	private PortrangeRtpServerSessionFactory sessionFactory;
	
	/**
	 * constrcutor
	 * 
	 */
	public PortrangeRtpServerFactory(PortrangeRtpServerSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/* (non-Javadoc)
	 * @see rtspproxy.jmx.mbeans.PortrangeRtpServerFactoryMBean#getMaxConnections()
	 */
	public int getMaxConnections() {
		return this.sessionFactory.getMaxConnections();
	}

	/* (non-Javadoc)
	 * @see rtspproxy.jmx.mbeans.PortrangeRtpServerFactoryMBean#getCurrentIdleConnections()
	 */
	public int getCurrentIdleConnections() {
		return this.sessionFactory.getCurrentIdleConnections();
	}

	/* (non-Javadoc)
	 * @see rtspproxy.jmx.mbeans.PortrangeRtpServerFactoryMBean#getCurrentActiveConnections()
	 */
	public int getCurrentActiveConnections() {
		return this.sessionFactory.getCurrentActiveConnections();
	}

}
