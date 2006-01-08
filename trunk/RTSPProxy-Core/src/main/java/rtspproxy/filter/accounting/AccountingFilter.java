package rtspproxy.filter.accounting;

import java.util.List;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.filter.FilterBase;
import rtspproxy.rtsp.RtspMessage;

/**
 * 
 * @author Matteo Merli
 */
public class AccountingFilter extends FilterBase
{

	private static Logger log = LoggerFactory.getLogger( AccountingFilter.class );

	public static final String FilterNAME = "accountingFilter";

	private AccountingProvider provider = null;

	public AccountingFilter(String className, List<Element> configElements)
	{
		super(FilterNAME, className, "accounting");
		
		this.provider = (AccountingProvider)loadConfigInitProvider(className, AccountingProvider.class,
				configElements);
	}

	@Override
	public void messageReceived( NextFilter nextFilter, IoSession session, Object message )
			throws Exception
	{
		if ( provider != null && isRunning()) {
			if ( message instanceof RtspMessage )
				provider.messageReceived( session, (RtspMessage) message );
			else
				log.error( "Expecting a RtspMessage. Received a "
						+ message.getClass().getName() );
		}

		// Forward message
		nextFilter.messageReceived( session, message );
	}

	@Override
	public void messageSent( NextFilter nextFilter, IoSession session, Object message )
			throws Exception
	{
		if ( provider != null  && isRunning()) {
			if ( message instanceof RtspMessage )
				provider.messageSent( session, (RtspMessage) message );
			else
				log.error( "Expecting a RtspMessage. Received a "
						+ message.getClass().getName() );
		}
		
		// Forward message
		nextFilter.messageSent( session, message );
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.common.IoFilterAdapter#sessionClosed(org.apache.mina.common.IoFilter.NextFilter, org.apache.mina.common.IoSession)
	 */
	@Override
	public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
		if ( provider != null  && isRunning())
			provider.sessionClosed( session );

		super.sessionClosed(nextFilter, session);
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.common.IoFilterAdapter#sessionCreated(org.apache.mina.common.IoFilter.NextFilter, org.apache.mina.common.IoSession)
	 */
	@Override
	public void sessionCreated(NextFilter nextFilter, IoSession session) throws Exception {
		if ( provider != null && isRunning() )
			provider.sessionCreated( session );

		super.sessionCreated(nextFilter, session);
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.common.IoFilterAdapter#sessionIdle(org.apache.mina.common.IoFilter.NextFilter, org.apache.mina.common.IoSession, org.apache.mina.common.IdleStatus)
	 */
	@Override
	public void sessionIdle(NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
		if ( provider != null && isRunning() )
			provider.sessionIdle( session, status );

		super.sessionIdle(nextFilter, session, status);
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.common.IoFilterAdapter#sessionOpened(org.apache.mina.common.IoFilter.NextFilter, org.apache.mina.common.IoSession)
	 */
	@Override
	public void sessionOpened(NextFilter nextFilter, IoSession session) throws Exception {
		if ( provider != null && isRunning() )
			provider.sessionOpened( session );

		super.sessionOpened(nextFilter, session);
	}

}
