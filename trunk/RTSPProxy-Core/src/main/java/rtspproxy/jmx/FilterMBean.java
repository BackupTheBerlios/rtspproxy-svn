/**
 * 
 */
package rtspproxy.jmx;

import javax.management.MBeanException;
import javax.management.ObjectName;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public interface FilterMBean {

	public ObjectName getDetailMBean();
	
	public boolean isRunning();

	/* Actions */

	public void suspend() throws MBeanException;

	public void resume() throws MBeanException;

}
