package rtspproxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.Executor;

import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.common.IoHandler;

import com.google.inject.ImplementedBy;

@ImplementedBy(ProxyServiceRegistry.class)
public interface IProxyServiceRegistry
{
    /**
     * Bind a Service to a local address and specify the IoHandler that will
     * manage ingoing and outgoing messages.
     * 
     * @param service
     *            the ProxyService
     * @param ioHandler
     *            the IoHandler that will handle the messages
     * @param address
     *            the local address to bind on
     * @throws IOException
     */
    public void bind( ProxyService service, IoHandler ioHandler,
            InetSocketAddress address ) throws IOException;
    
    /**
     * Bind a Service to a local address and specify the IoHandler that will
     * manage ingoing and outgoing messages.
     * <p>
     * In addition it should be specified an IoFilterChainBuilder. This builder
     * will be associated with the IoAcceptor itself (which is unique per
     * ProxyService) and not for every IoSession created.
     * 
     * @param service
     *            the ProxyService
     * @param ioHandler
     *            the IoHandler that will handle the messages
     * @param address
     *            the local address to bind on
     * @param filterChainBuilder
     *            the IoFilterChainBuilder instance
     * @throws IOException
     */
    public void bind( ProxyService service, IoHandler ioHandler,
            InetSocketAddress address, IoFilterChainBuilder filterChainBuilder )
            throws IOException;
    
    public void unbind( ProxyService service ) throws Exception;
    
    /**
     * Unbind the service from all of its bound addresses.
     * 
     * @param service
     *            the ProxyService
     * @throws Exception
     */
    public void unbind( ProxyService service, boolean stopService )
            throws Exception;
    
    /**
     * Unbind all the services registered in the ProxyServiceRegistry, from all
     * of they bound addresses.
     * 
     * @throws Exception
     */
    public void unbindAll() throws Exception;
    
    /**
     * @return a Set containing all the registered services.
     */
    public Set<ProxyService> getAllServices();
    
    /**
     * Return the instance of a ProxyService.
     * 
     * @param name
     *            the name of the ProxyService
     * @return the instance of the ProxyService
     */
    public ProxyService getService( String name );
    
    /**
     * Returns a reference to the IoAcceptor used by the specified ProxyService.
     * 
     * @param serviceName
     *            the name of the ProxyService
     * @return the IoAcceptor associated with the service or null if the
     *         serviceName is invalid
     */
    public IoAcceptor getAcceptor( String serviceName );
    
    /**
     * Returns a reference to the IoAcceptor used by the specified ProxyService.
     * 
     * @param service
     *            the ProxyService
     * @return the IoAcceptor associated with the service
     */
    public IoAcceptor getAcceptor( ProxyService service );
    
    public Executor getExecutor();
}