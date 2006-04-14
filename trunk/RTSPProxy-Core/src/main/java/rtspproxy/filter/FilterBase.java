/**
 * 
 */
package rtspproxy.filter;

import java.util.List;

import javax.management.ObjectName;

import org.apache.mina.common.IoFilterAdapter;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.Reactor;
import rtspproxy.config.AAAConfigurable;
import rtspproxy.lib.Side;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 * 
 */
public class FilterBase extends IoFilterAdapter
{

    private static Logger logger = LoggerFactory.getLogger( FilterBase.class );

    // filter name
    private String filterName;

    // class name
    private String className;

    // filter type
    private String typeName;

    // running flag
    protected boolean running = true;

    // side
    private Side side;

    // MBean name assigned by JMX interface
    private ObjectName mbeanName;

    // chain name of filter
    private String chainName;

    /**
     * 
     */
    public FilterBase( String filterName, String className, String typeName )
    {
        this.filterName = filterName;
        this.className = className;
        this.typeName = typeName;
    }

    /**
     * 
     */
    public FilterBase( String filterName, String typeName )
    {
        this.filterName = filterName;
        this.typeName = typeName;
    }

    /**
     * query running flag
     */
    public final boolean isRunning()
    {
        return this.running;
    }

    /**
     * suspend the filter
     */
    public final void suspend()
    {
        this.running = false;
        logger.info( getChainName() + " suspended" );
    }

    /**
     * resume the filter
     */
    public final void resume()
    {
        this.running = true;
        logger.info( getChainName() + " resumed" );
    }

    /**
     * get the object name of a more specific MBean
     */
    public ObjectName getDetailMBean()
    {
        return null;
    }

    /**
     * get the side
     */
    public Side getSide()
    {
        return this.side;
    }

    /**
     * set the side
     */
    public void setSide( Side side )
    {
        this.side = side;
    }

    /**
     * @return Returns the mbeanName.
     */
    public ObjectName getMbeanName()
    {
        return mbeanName;
    }

    /**
     * Set the name of the MBean used for filter management. This property is
     * write-once.
     * 
     * @param mbeanName
     *            The mbeanName to set.
     */
    public void setMbeanName( ObjectName mbeanName )
    {
        // once set it can not change
        if ( this.mbeanName == null )
            this.mbeanName = mbeanName;
    }

    /**
     * @return Returns the className.
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * @return Returns the typeName.
     */
    public String getTypeName()
    {
        return typeName;
    }

    /**
     * get the chain name for the filter.
     */
    public String getChainName()
    {

        if ( this.chainName == null ) {
            StringBuffer buf = new StringBuffer( this.filterName );

            buf.append( '/' );
            buf.append( this.typeName );
            if ( this.className != null ) {
                buf.append( '/' );
                buf.append( this.className );
            }

            this.chainName = buf.toString();
        }
        return this.chainName;
    }

    /**
     * load a provider class, instantiate an object, configure and init it.
     * 
     * @param className
     *            the name of the provider class to load
     * @param requiredInterface
     *            the provider interface the loaded class should implement. This
     *            implementation assumes that the interface is a sub-interface
     *            of GenericProvider
     * @param configElements
     *            the configuration elements used to configure the loaded
     *            provider
     */
    protected GenericProvider loadConfigInitProvider( String className,
            Class requiredInterface, List<Element> configElements )
    {
        GenericProvider provider = null;
        String providerName = requiredInterface.getSimpleName();
        Class providerClass;
        try {
            providerClass = Class.forName( className );

        } catch ( Throwable t ) {
            logger.error( "Invalid " + providerName + "class: " + className, t );
            Reactor.stop();
            return null;
        }

        // Check if the class implements the IpAddressProvider interfaces
        boolean found = false;
        for ( Class interFace : providerClass.getInterfaces() ) {
            if ( requiredInterface.equals( interFace ) ) {
                found = true;
                break;
            }
        }

        if ( !found ) {
            logger.error( "Class ({}) does not implement the {} interface.", providerClass,
                    providerName );
            Reactor.stop();
            return null;
        }

        try {
            provider = (GenericProvider) providerClass.newInstance();

            if ( provider instanceof AAAConfigurable )
                ((AAAConfigurable) provider).configure( configElements );

            provider.init();

        } catch ( Exception e ) {
            logger.error( "Error starting " + providerName + ": " + e );
            Reactor.stop();
            return null;
        }

        return provider;
    }
}
