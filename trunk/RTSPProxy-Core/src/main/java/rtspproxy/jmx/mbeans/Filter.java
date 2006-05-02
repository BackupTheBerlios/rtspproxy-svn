/**
 * 
 */
package rtspproxy.jmx.mbeans;

import java.util.Hashtable;

import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import rtspproxy.filter.FilterBase;
import rtspproxy.jmx.JmxAgent;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 * 
 */
public class Filter implements FilterMBean
{

    /** managed filter */
    private final FilterBase filter;

    /** object name */
    private final ObjectName name;

    /**
     * @throws NullPointerException
     * @throws MalformedObjectNameException
     * 
     */
    public Filter( FilterBase filter ) throws MalformedObjectNameException,
            NullPointerException
    {
        // build the MBean name
        Hashtable<String, String> keys = new Hashtable<String, String>();

        keys.put( "filter", filter.getName() );
        keys.put( "classname", filter.getProviderClassName() );
        keys.put( "id", Long.toHexString( System.identityHashCode( filter ) ) );

        this.name = new ObjectName( JmxAgent.FILTERS_DOMAIN, keys );
        this.filter = filter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.jmx.FilterMBean#getDetailMBean()
     */
    public ObjectName getDetailMBean()
    {
        return filter.getDetailMBean();
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.jmx.FilterMBean#isRunning()
     */
    public boolean isRunning()
    {
        return filter.isRunning();
    }

    /*
     * (non -Javadoc)
     * 
     * @see rtspproxy.jmx.FilterMBean#suspend()
     */
    public void suspend() throws MBeanException
    {
        if ( filter != null )
            filter.suspend();
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.jmx.FilterMBean#resume()
     */
    public void resume() throws MBeanException
    {
        filter.resume();
    }

    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.jmx.mbeans.FilterMBean#getName()
     */
    public String getName()
    {
        return filter.getName();
    }

    public ObjectName getObjectName()
    {
        return name;
    }

}
