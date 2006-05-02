
package rtspproxy.jmx.mbeans;

import java.net.InetAddress;

/**
 * Management interface from Track (RtpTrack, RdtTrack) objects. 
 *
 * @author Matteo Merli
 */
public interface TrackFacadeMBean 
{
    public String getUrl();
    
    public void setUrl( String url );

    public InetAddress getClientAddress();
    
    public InetAddress getServerAddress();

    public void close();
}
