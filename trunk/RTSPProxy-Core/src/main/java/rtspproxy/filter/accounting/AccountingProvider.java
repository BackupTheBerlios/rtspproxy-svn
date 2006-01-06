package rtspproxy.filter.accounting;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;

import rtspproxy.rtsp.RtspMessage;

/**
 * @author Matteo Merli
 */
public interface AccountingProvider
{
	
	/**
	 * Called once at service startup. Should be used to initialize the
	 * provider.
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception;

	/**
	 * Called once at service shutdown.
	 * 
	 * @throws Exception
	 */
	public void shutdown() throws Exception;

	public void messageReceived( IoSession session, RtspMessage message );

	public void messageSent( IoSession session, RtspMessage message );
	
	public void sessionCreated(IoSession session);
	
	public void sessionOpened(IoSession session);
	
	public void sessionClosed(IoSession session);
	
	public void sessionIdle(IoSession session, IdleStatus status);

}
