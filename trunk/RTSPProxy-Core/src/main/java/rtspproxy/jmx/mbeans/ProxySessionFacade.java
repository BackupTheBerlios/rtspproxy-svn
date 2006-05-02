/**
 * 
 */
package rtspproxy.jmx.mbeans;

import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import rtspproxy.jmx.JmxAgent;
import rtspproxy.proxy.ProxySession;
import rtspproxy.proxy.track.Track;

/**
 * Management implementation of proxy sessions.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 * @author Matteo Merli
 *
 */
public class ProxySessionFacade implements ProxySessionFacadeMBean {

    // reference to proxy session
    private ProxySession session;
	
    public ProxySessionFacade( ProxySession session ) 
    {        
        this.session = session;
    }

    public Date getStartDate()
    {
        return session.getStartDate();
    }

    public String getClientSessionId() 
    {
        return session.getClientSessionId();
    }

    public String getServerSessionId() 
    {
        return session.getServerSessionId();
    }

    public Collection<Track> getTrackList() 
    {
        return session.getTrackList();
    }

    public void close()
    {
        session.close();
    }

    /**
     * build the object name
     * @throws NullPointerException 
     * @throws MalformedObjectNameException 
     */
    public ObjectName buildName() 
        throws MalformedObjectNameException, NullPointerException 
    {
        Hashtable<String, String> parts = new Hashtable<String, String>();
        
        parts.put("clientID", this.session.getClientSessionId());
        
        String serverID = this.session.getServerSessionId();
        int ind = serverID.indexOf(';');
        
        if(ind > 0)
            serverID = serverID.substring(0, ind);
        parts.put("serverID", serverID);
        
        return ObjectName.getInstance(JmxAgent.PROXY_SESSION_DOMAIN, parts);
    }

}
