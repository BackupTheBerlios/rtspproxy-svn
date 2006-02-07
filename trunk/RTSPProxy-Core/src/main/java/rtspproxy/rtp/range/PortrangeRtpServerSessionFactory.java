/**
 * 
 */
package rtspproxy.rtp.range;

import java.io.IOException;
import java.net.InetAddress;
import java.util.NoSuchElementException;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.config.Config;
import rtspproxy.config.Parameter;
import rtspproxy.jmx.JmxAgent;
import rtspproxy.lib.Singleton;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class PortrangeRtpServerSessionFactory extends Singleton implements Observer {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(PortrangeRtpServerSessionFactory.class);
	
	// connection pool
	private GenericObjectPool pool;
	
	// local addr
	private InetAddress localAddress;
	
	/**
	 * constructor 
	 */
	public PortrangeRtpServerSessionFactory() {
		
	}
	
	/**
	 * get the singleton instance
	 */
	public static PortrangeRtpServerSessionFactory getInstance() {
		return (PortrangeRtpServerSessionFactory)Singleton.getInstance(PortrangeRtpServerSessionFactory.class);
	}
	
	/**
	 * get a RTP server session
	 */
	public PortrangeRtpServerSession getSession() throws IOException {
		try {
			return (PortrangeRtpServerSession)this.pool.borrowObject();
		} catch(Exception e) {
			logger.info("failed to obtain RTP server session", e);
			
			throw new IOException("cant obtain RTP server session");
		}
	}
	
	/**
	 * release a server session
	 */
	public void releaseSession(PortrangeRtpServerSession session) {
		try {
			session.closeOpenSessions();
			this.pool.returnObject(session);
		} catch(Exception e) {
			logger.info("failed to release session", e);
		}
	}
	
	/**
	 * initialise the factory
	 */
	public void start() throws Exception {
		if(Config.proxyServerRtpMultiplePorts.getValue()) {
			int minPort = Config.proxyServerRtpMinPort.getValue();
			int maxPort = Config.proxyServerRtpMaxPort.getValue();
			int rtpSessionIdleTimeout = Config.proxyServerRtpIdleTimeout.getValue();
			int poolSize = Config.proxyServerRtpThreadPoolSize.getValue();
			int idleScanInterval = Config.proxyServerRtpIdleScanInterval.getValue() * 1000;
			
			if(minPort <= 0 || minPort >= 65536)
				throw new IllegalArgumentException("RTP min port out of range: " + minPort);
			if(maxPort <= 0 || maxPort >= 65536)
				throw new IllegalArgumentException("RTP max port out of range: " + maxPort);
			
			minPort = minPort + (minPort % 2);
			maxPort = maxPort - (maxPort % 2);
			logger.debug("RTP min port=" + minPort + ", max port=" + maxPort);
			
			if(minPort >= maxPort)
				throw new IllegalArgumentException("RTP min port too high, min=" + minPort + ", max=" + maxPort);
			int maxConn = (maxPort -minPort) / 2;
			
			GenericObjectPool.Config config = new GenericObjectPool.Config();
			
			config.maxActive = maxConn;
			config.maxIdle = maxConn / 2;
			config.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
			config.testOnBorrow = false;
			config.testOnReturn = false;
			config.testWhileIdle = true;
			config.minEvictableIdleTimeMillis = rtpSessionIdleTimeout*1000;
			config.timeBetweenEvictionRunsMillis = idleScanInterval;
			
			this.pool = new GenericObjectPool(new RtpServerSessionFactory(this.localAddress, minPort, maxConn,
					rtpSessionIdleTimeout, poolSize), config);
			
			Config.proxyServerRtpIdleScanInterval.addObserver(this);
			Config.proxyServerRtpIdleTimeout.addObserver(this);
			
			JmxAgent.getInstance().registerPortRangeRtpServerSessionfactory(this);
		}
	}
	
	/**
	 * shutdown the session factory
	 */
	public void stop() {
		if(Config.proxyServerRtpMultiplePorts.getValue()) {
			try {
				this.pool.close();
			} catch(Exception e) {
				logger.info("exception while closing RTP port pool", e);
			}
			
			JmxAgent.getInstance().unregisterPortRangeRtpServerSessionfactory();
		}		
	}

	public void setLocalAddress(InetAddress address) {
		this.localAddress = address;
	}

	public void update(Observable o, Object arg) {
		if(o instanceof Parameter) {
			Parameter p = (Parameter)o;
			
			if(p.equals(Config.proxyServerRtpIdleScanInterval))
				this.pool.setTimeBetweenEvictionRunsMillis(Config.proxyServerRtpIdleScanInterval.getValue());
			else if(p.equals(Config.proxyServerRtpIdleTimeout))
				this.pool.setMinEvictableIdleTimeMillis(Config.proxyServerRtpIdleTimeout.getValue() * 1000);
		}
	}

	public int getMaxConnections() {
		return this.pool.getMaxActive();
	}

	public int getCurrentIdleConnections() {
		return this.pool.getNumIdle();
	}

	public int getCurrentActiveConnections() {
		return this.pool.getNumActive();
	}
}
