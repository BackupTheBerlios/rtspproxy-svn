/**
 * 
 */
package rtspproxy.jmx.mbeans;

import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import java.util.Hashtable;

import rtspproxy.filter.FilterBase;
import rtspproxy.jmx.JmxAgent;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class Filter implements FilterMBean {

	// managed filter
	private FilterBase filter;
	
	// object name
	private ObjectName name;
	
	/**
	 * @throws NullPointerException 
	 * @throws MalformedObjectNameException 
	 * 
	 */
	public Filter(FilterBase filter) throws MalformedObjectNameException, NullPointerException {
		this.filter = filter;
		
		// build the MBean name
		Hashtable<String, String> keys = new Hashtable<String, String>();
		
		keys.put("filter", filter.getTypeName());
		keys.put("side", filter.getSide().toString());
		keys.put("classname", filter.getClassName());
		keys.put("id", Long.toHexString(System.identityHashCode(filter)));
		
		this.name = new ObjectName(JmxAgent.FILTERS_DOMAIN, keys);
	}

	/* (non-Javadoc)
	 * @see rtspproxy.jmx.FilterMBean#getDetailMBean()
	 */
	public ObjectName getDetailMBean() {
		return this.filter.getDetailMBean();
	}

	/* (non-Javadoc)
	 * @see rtspproxy.jmx.FilterMBean#isRunning()
	 */
	public boolean isRunning() {
		return this.filter.isRunning();
	}

	/* (non-Javadoc)
	 * @see rtspproxy.jmx.FilterMBean#suspend()
	 */
	public void suspend() throws MBeanException {
		this.filter.suspend();
	}

	/* (non-Javadoc)
	 * @see rtspproxy.jmx.FilterMBean#resume()
	 */
	public void resume() throws MBeanException {
		this.filter.resume();
	}

	/**
	 * @return Returns the name.
	 */
	public ObjectName getName() {
		return name;
	}

}
