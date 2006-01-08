package rtspproxy.filter.accounting;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;

import rtspproxy.filter.GenericProvider;
import rtspproxy.rtsp.RtspMessage;

/**
 * @author Matteo Merli
 */
public interface AccountingProvider extends GenericProvider
{
	
	public void messageReceived( IoSession session, RtspMessage message );

	public void messageSent( IoSession session, RtspMessage message );
	
	public void sessionCreated(IoSession session);
	
	public void sessionOpened(IoSession session);
	
	public void sessionClosed(IoSession session);
	
	public void sessionIdle(IoSession session, IdleStatus status);

}
