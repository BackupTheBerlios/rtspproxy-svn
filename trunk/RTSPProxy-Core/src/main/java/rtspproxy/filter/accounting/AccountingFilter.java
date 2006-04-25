package rtspproxy.filter.accounting;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.config.Config;
import rtspproxy.filter.FilterBase;
import rtspproxy.rtsp.RtspMessage;

/**
 * 
 * @author Matteo Merli
 */
public class AccountingFilter extends FilterBase<AccountingProvider>
{

    private static Logger log = LoggerFactory.getLogger( AccountingFilter.class );

    public static final String FilterNAME = "accountingFilter";

    private AccountingProvider provider = null;

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.FilterBase#getName()
     */
    @Override
    public String getName()
    {
        return FilterNAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.FilterBase#setProvider(rtspproxy.filter.GenericProvider)
     */
    @Override
    public void setProvider( AccountingProvider provider )
    {
        this.provider = provider;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.FilterBase#getProviderInterface()
     */
    @Override
    public Class<AccountingProvider> getProviderInterface()
    {
        return AccountingProvider.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.FilterBase#getProviderClassName()
     */
    @Override
    public String getProviderClassName()
    {
        return Config.filtersAccountingImplClass.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.common.IoFilterAdapter#messageReceived(org.apache.mina.common.IoFilter.NextFilter,
     *      org.apache.mina.common.IoSession, java.lang.Object)
     */
    @Override
    public void messageReceived( NextFilter nextFilter, IoSession session, Object message )
            throws Exception
    {
        if ( provider != null && isRunning() ) {
            if ( message instanceof RtspMessage )
                provider.messageReceived( session, (RtspMessage) message );
            else
                log.error( "Expecting a RtspMessage. Received a "
                        + message.getClass().getName() );
        }

        // Forward message
        nextFilter.messageReceived( session, message );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.common.IoFilterAdapter#messageSent(org.apache.mina.common.IoFilter.NextFilter,
     *      org.apache.mina.common.IoSession, java.lang.Object)
     */
    @Override
    public void messageSent( NextFilter nextFilter, IoSession session, Object message )
            throws Exception
    {
        if ( provider != null && isRunning() ) {
            if ( message instanceof RtspMessage )
                provider.messageSent( session, (RtspMessage) message );
            else
                log.error( "Expecting a RtspMessage. Received a "
                        + message.getClass().getName() );
        }

        // Forward message
        nextFilter.messageSent( session, message );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.common.IoFilterAdapter#sessionClosed(org.apache.mina.common.IoFilter.NextFilter,
     *      org.apache.mina.common.IoSession)
     */
    @Override
    public void sessionClosed( NextFilter nextFilter, IoSession session )
            throws Exception
    {
        if ( provider != null && isRunning() )
            provider.sessionClosed( session );

        super.sessionClosed( nextFilter, session );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.common.IoFilterAdapter#sessionCreated(org.apache.mina.common.IoFilter.NextFilter,
     *      org.apache.mina.common.IoSession)
     */
    @Override
    public void sessionCreated( NextFilter nextFilter, IoSession session )
            throws Exception
    {
        if ( provider != null && isRunning() )
            provider.sessionCreated( session );

        super.sessionCreated( nextFilter, session );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.common.IoFilterAdapter#sessionIdle(org.apache.mina.common.IoFilter.NextFilter,
     *      org.apache.mina.common.IoSession, org.apache.mina.common.IdleStatus)
     */
    @Override
    public void sessionIdle( NextFilter nextFilter, IoSession session, IdleStatus status )
            throws Exception
    {
        if ( provider != null && isRunning() )
            provider.sessionIdle( session, status );

        super.sessionIdle( nextFilter, session, status );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.common.IoFilterAdapter#sessionOpened(org.apache.mina.common.IoFilter.NextFilter,
     *      org.apache.mina.common.IoSession)
     */
    @Override
    public void sessionOpened( NextFilter nextFilter, IoSession session )
            throws Exception
    {
        if ( provider != null && isRunning() )
            provider.sessionOpened( session );

        super.sessionOpened( nextFilter, session );
    }

}
