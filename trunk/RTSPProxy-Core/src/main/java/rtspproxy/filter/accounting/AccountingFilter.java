package rtspproxy.filter.accounting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;

import rtspproxy.Reactor;
import rtspproxy.config.Config;
import rtspproxy.rtsp.RtspMessage;

/**
 * 
 * @author Matteo Merli
 */
public class AccountingFilter extends IoFilterAdapter
{

	private static Logger log = LoggerFactory.getLogger( AccountingFilter.class );

	private AccountingProvider provider = null;

	public AccountingFilter()
	{
		// Check which backend implementation to use
		// Default is plain-text implementation
		String className = Config.proxyFilterAccountingImplementationClass.getValue();

		Class providerClass;
		try {
			providerClass = Class.forName( className );

		} catch ( ClassNotFoundException e ) {
			log.error( "Invalid AccountingProvider class: " + className );
			Reactor.stop();
			return;
		}

		// Check if the class implements the IpAddressProvider interfaces
		boolean found = false;
		for ( Class interFace : providerClass.getInterfaces() ) {
			if ( AccountingProvider.class.equals( interFace ) ) {
				found = true;
				break;
			}
		}

		if ( !found ) {
			log.error( "Class (" + providerClass
					+ ") does not implement the AccountingProvider interface." );
			Reactor.stop();
			return;
		}

		try {
			provider = (AccountingProvider) providerClass.newInstance();
			provider.init();

		} catch ( Exception e ) {
			log.error( "Error starting AccountingProvider: " + e );
			Reactor.stop();
			return;
		}

		log.info( "Using AccountingFilter " + " (" + className + ")" );
	}

	@Override
	public void messageReceived( NextFilter nextFilter, IoSession session, Object message )
			throws Exception
	{
		if ( provider != null ) {
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
		if ( provider != null ) {
			if ( message instanceof RtspMessage )
				provider.messageSent( session, (RtspMessage) message );
			else
				log.error( "Expecting a RtspMessage. Received a "
						+ message.getClass().getName() );
		}
		
		// Forward message
		nextFilter.messageSent( session, message );
	}

}
