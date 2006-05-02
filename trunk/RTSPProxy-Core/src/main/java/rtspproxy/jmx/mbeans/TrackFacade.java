
package rtspproxy.jmx.mbeans;

import java.net.InetAddress;

import rtspproxy.proxy.track.Track;

/**
 * Management facade from Track (RtpTrack, RdtTrack) objects. 
 *
 * @author Matteo Merli
 */
public class TrackFacade implements TrackFacadeMBean 
{
    private Track track;
    
    public TrackFacade( Track track )
    {
        this.track = track;
    }

    public String getUrl()
    {
        return track.getUrl();
    }
    
    public void setUrl( String url ) 
    {
        track.setUrl( url );
    }        

    public InetAddress getClientAddress()
    {
        return track.getClientAddress();
    }

    public InetAddress getServerAddress()
    {
        return track.getServerAddress();
    }

    public void close()
    {
        track.close();
    }
    
}
