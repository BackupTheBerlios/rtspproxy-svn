/**
 * 
 */
package rtspproxy.filter;

import javax.management.ObjectName;

import org.apache.mina.common.IoFilterAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.lib.Side;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class FilterBase extends IoFilterAdapter {

	private static Logger logger = LoggerFactory.getLogger(FilterBase.class);
	
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
	
	/**
	 * 
	 */
	public FilterBase(String filterName, String className, String typeName) {
		this.filterName = filterName;
		this.className = className;
		this.typeName = typeName;
	}

	/**
	 * query running flag
	 */
	public final boolean isRunning() {
		return this.running;
	}
	
	/**
	 * suspend the filter
	 */
	public final void suspend() {
		this.running = false;
		logger.info("filter " + this.typeName + "/" + this.className + " suspended");
	}
	
	/**
	 * resume the filter
	 */
	public final void resume() {
		this.running = true;
		logger.info("filter " + this.typeName + "/" + this.className + " resumed");
	}
	
	/**
	 * get the object name of a more specific MBean 
	 */
	public ObjectName getDetailMBean() {
		return null;
	}
	
	/**
	 * get the side 
	 */
	public Side getSide() {
		return this.side;
	}
	
	/**
	 * set the side
	 */
	public void setSide(Side side) {
		this.side = side;
	}

	/**
	 * @return Returns the mbeanName.
	 */
	public ObjectName getMbeanName() {
		return mbeanName;
	}

	/**
	 * Set the name of the MBean used for filter management. This property is write-once.
	 * @param mbeanName The mbeanName to set.
	 */
	public void setMbeanName(ObjectName mbeanName) {
		// once set it can not change
		if(this.mbeanName == null)
			this.mbeanName = mbeanName;
	}

	/**
	 * @return Returns the className.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return Returns the typeName.
	 */
	public String getTypeName() {
		return typeName;
	}
	
	/**
	 * get the chain name for the filter. 
	 */
	public String getChainName() {
		return this.filterName +  "/" + this.typeName + "/" + this.className;
	}
}
