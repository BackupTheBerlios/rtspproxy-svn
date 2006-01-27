/**
 * 
 */
package rtspproxy.filter.control;

import org.apache.mina.common.IoSession;

import rtspproxy.filter.GenericProvider;
import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;

/**
 * definition of a generic message filter applyable on the RTSP filter
 * chain.
 * This filter is intended as a generic way of modifying RTSP requests / responses
 * either in the client- or server-side filter chain. Typical use-cases are
 * adding / modifying header values before passing the message over to the upstream
 * server. 
 * The filter is intentionally not desiged to modify the overall message flow.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public interface ControlProvider extends GenericProvider {

	/**
	 * process a request
	 */
	public void processRequest(IoSession session, RtspRequest request);
	
	/**
	 * process a response
	 */
	public void processResponse(IoSession session, RtspResponse response);
	
	/**
	 * session gets opened
	 */
	public void sessionOpened(IoSession session);
	
	/**
	 * session gets closed
	 */
	public void sessionClosed(IoSession session);
}
