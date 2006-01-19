/**
 * 
 */
package rtspproxy.jmx;

import javax.management.ObjectName;

/**
 * Extension of the JmxManageable interface for components that wish to make their 
 * managed csub-components visible.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public interface JmxManageable2 extends JmxManageable {
	/**
	 * get the sub-component object name
	 */
	public ObjectName getMBean();
}
