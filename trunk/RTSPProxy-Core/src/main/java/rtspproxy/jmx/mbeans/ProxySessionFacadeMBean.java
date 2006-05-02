/**
 * 
 */
package rtspproxy.jmx.mbeans;

import java.util.Collection;
import java.util.Date;

import rtspproxy.proxy.track.Track;

/**
 * Management interface to proxy session.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 * @author Matteo Merli
 *
 */
public interface ProxySessionFacadeMBean 
{

    public Date getStartDate();
    
    public String getClientSessionId();
    
    public String getServerSessionId();

    public Collection<Track> getTrackList();

    public void close();
    
}
