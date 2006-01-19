/**
 * 
 */
package rtspproxy.jmx.mbeans;

import javax.management.ObjectName;

/**
 * Management interface to proxy session.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public interface ProxySessionFacadeMBean {
	/**
	 * query if the session is closed
	 */
	public boolean isClosed();
	
	/**
	 * get reference to client session
	 */
	public ObjectName getClientSession();
	
	/**
	 * get reference to server session
	 */
	public ObjectName getServerSession();
}
