/**
 * 
 */
package rtspproxy.filter.accounting;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;

import rtspproxy.rtsp.RtspMessage;

/**
 * Default implementation of the AccountingProvider interface. Provides no-op
 * method implementations.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 * 
 */
public abstract class AccountingProviderAdapter implements AccountingProvider
{

    /**
     * 
     */
    public AccountingProviderAdapter()
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.accounting.AccountingProvider#messageReceived(org.apache.mina.common.IoSession,
     *      rtspproxy.rtsp.RtspMessage)
     */
    public void messageReceived( IoSession session, RtspMessage message )
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.accounting.AccountingProvider#messageSent(org.apache.mina.common.IoSession,
     *      rtspproxy.rtsp.RtspMessage)
     */
    public void messageSent( IoSession session, RtspMessage message )
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.accounting.AccountingProvider#sessionCreated(org.apache.mina.common.IoSession)
     */
    public void sessionCreated( IoSession session )
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.accounting.AccountingProvider#sessionOpened(org.apache.mina.common.IoSession)
     */
    public void sessionOpened( IoSession session )
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.accounting.AccountingProvider#sessionClosed(org.apache.mina.common.IoSession)
     */
    public void sessionClosed( IoSession session )
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.accounting.AccountingProvider#sessionIdle(org.apache.mina.common.IoSession,
     *      org.apache.mina.common.IdleStatus)
     */
    public void sessionIdle( IoSession session, IdleStatus status )
    {
    }

}
