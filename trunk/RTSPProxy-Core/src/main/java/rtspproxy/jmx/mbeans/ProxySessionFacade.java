/**
 * 
 */
package rtspproxy.jmx.mbeans;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import rtspproxy.jmx.JmxAgent;
import rtspproxy.proxy.ProxySession;

/**
 * Management implementation of proxy sessions.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class ProxySessionFacade implements ProxySessionFacadeMBean {

	// reference to proxy session
	private ProxySession session = null;
	
	/**
	 * 
	 */
	public ProxySessionFacade(ProxySession session) {
		this.session = session;
	}

	public boolean isClosed() {
		return session.isClosed();
	}

	public ObjectName getClientSession() {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectName getServerSession() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * build the object name
	 * @throws NullPointerException 
	 * @throws MalformedObjectNameException 
	 */
	public ObjectName buildName() throws MalformedObjectNameException, NullPointerException {
		Hashtable<String, String> parts = new Hashtable<String, String>();
		
		parts.put("clientID", this.session.getClientSessionId());
		parts.put("serverID", this.session.getServerSessionId());
		
		return ObjectName.getInstance(JmxAgent.PROXY_SESSION_DOMAIN, parts);
	}
	
}
