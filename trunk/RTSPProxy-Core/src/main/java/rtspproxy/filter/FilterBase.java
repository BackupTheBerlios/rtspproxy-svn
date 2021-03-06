/**
 * 
 */
package rtspproxy.filter;

import javax.management.ObjectName;

import org.apache.commons.configuration.Configuration;
import org.apache.mina.common.IoFilterAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.IReactor;

import com.google.inject.Inject;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 * 
 */
public abstract class FilterBase<T extends GenericProvider> extends
        IoFilterAdapter
{
    
    private static Logger log = LoggerFactory.getLogger( FilterBase.class );
    
    @Inject
    private IReactor reactor;
    
    /** running flag */
    private boolean isRunning = false;
    
    /** chain name of filter */
    private String chainName;
    
    private T providerReference = null;
    
    // Abstract methods
    public abstract String getName();
    
    public String getProviderClassName()
    {
        return null;
    }
    
    protected Class<T> getProviderInterface()
    {
        return null;
    }
    
    protected void setProvider( T provider )
    {
    }
    
    /**
     * Subclasses can overload the method to read theyr own configuration
     * parameters.
     * 
     * @param configuration
     *            A {Configuration} object to read from.
     */
    protected void doConfigure( Configuration configuration )
    {
    }
    
    /**
     * query running flag
     */
    public final boolean isRunning()
    {
        return isRunning;
    }
    
    /**
     * suspend the filter
     */
    public final void suspend()
    {
        if ( providerReference != null ) providerReference.stop();
        
        isRunning = false;
        log.info( "{} suspended", getChainName() );
    }
    
    /**
     * resume the filter
     */
    public final void resume()
    {
        if ( providerReference != null )
        {
            try
            {
                providerReference.start();
            } catch ( Exception e )
            {
                log.error( "Error starting {}: {}", getProviderClassName(),
                           e.getMessage() );
                return;
            }
        }
        isRunning = true;
        log.info( "{} resumed", getChainName() );
    }
    
    /**
     * get the object name of a more specific MBean
     */
    public ObjectName getDetailMBean()
    {
        return null;
    }
    
    /**
     * get the chain name for the filter.
     */
    public String getChainName()
    {
        if ( this.chainName == null )
        {
            StringBuilder buf = new StringBuilder();
            
            buf.append( getName() );
            buf.append( '/' );
            buf.append( getProviderInterface() );
            if ( getProviderClassName() != null )
            {
                buf.append( '/' );
                buf.append( getProviderClassName() );
            }
            this.chainName = buf.toString();
        }
        
        return this.chainName;
    }
    
    @SuppressWarnings("unchecked")
    public final void configure( Configuration configuration )
    {
        // Configure the concrete class implementation
        doConfigure( configuration );
        
        Class providerClass;
        String className = getProviderClassName();
        if ( className == null )
        {
            // The filter does not have a provider
            // system. Ignore it.
            return;
        }
        
        try
        {
            providerClass = Class.forName( className );
            
        } catch ( Throwable t )
        {
            log.error( "Class not found: {}", className );
            reactor.stop();
            return;
        }
        
        // Check if the class implements the required interface
        boolean found = false;
        Class<T> requiredInterface = getProviderInterface();
        for ( Class interFace : providerClass.getInterfaces() )
        {
            if ( interFace.equals( requiredInterface ) )
            {
                found = true;
                break;
            }
        }
        
        if ( !found )
        {
            log.error( "Class ({}) does not implement the {} interface.",
                       providerClass, requiredInterface );
            reactor.stop();
            return;
        }
        
        // Instanciate the provider and configure it.
        try
        {
            T provider = (T) providerClass.newInstance();
            setProvider( provider );
            providerReference = provider;
            provider.configure( configuration );
            provider.start();
            
        } catch ( Exception e )
        {
            log.error( "Error instanciaing class '{}'", providerClass );
            reactor.stop();
            return;
        }
    }
    
}
