/**
 * 
 */
package rtspproxy.jmx;

import javax.management.MBeanServer;

/**
 * This interface is implemented by components that wish you expose its own
 * MBeans to the managment interface.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 */
public interface JmxManageable
{

    /**
     * Set the MBeanServer instance. This method becomes called after the MBean
     * managing the component has been created and attached to the MBeanServer.
     * 
     * @param mbeanServer
     *            the MBeanServer instance used to manage the RTSPProxy.
     */
    public void setMBeanServer( MBeanServer mbeanServer );
}
