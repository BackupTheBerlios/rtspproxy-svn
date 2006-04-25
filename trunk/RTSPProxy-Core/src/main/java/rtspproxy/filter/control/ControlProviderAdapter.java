/**
 * 
 */
package rtspproxy.filter.control;

import org.apache.mina.common.IoSession;

import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;

/**
 * Default implementation of the ControlProvider interface. All method bodies
 * are empty and do nothing.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 * 
 */
public abstract class ControlProviderAdapter implements ControlProvider
{

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.control.ControlProvider#processRequest(org.apache.mina.common.IoSession,
     *      rtspproxy.rtsp.RtspRequest)
     */
    public void processRequest( IoSession session, RtspRequest request )
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.control.ControlProvider#processResponse(org.apache.mina.common.IoSession,
     *      rtspproxy.rtsp.RtspResponse)
     */
    public void processResponse( IoSession session, RtspResponse response )
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.control.ControlProvider#sessionOpened(org.apache.mina.common.IoSession)
     */
    public void sessionOpened( IoSession session )
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.control.ControlProvider#sessionClosed(org.apache.mina.common.IoSession)
     */
    public void sessionClosed( IoSession session )
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.GenericProvider#init()
     */
    public void start() throws Exception
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.GenericProvider#shutdown()
     */
    public void stop()
    {
    }

}
