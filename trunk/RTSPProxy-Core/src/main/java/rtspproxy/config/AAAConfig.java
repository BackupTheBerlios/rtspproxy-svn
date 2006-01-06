/**
 * 
 */
package rtspproxy.config;

import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;

import rtspproxy.lib.Side;

/**
 * This class contains the configuration for an AAA filter.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 */
public class AAAConfig {

	// implementation class name
	private String implClass;
	
	// list of configuration elements
	private List<Element> configElements;
	
	// filter application
	private Side side = Side.Any;
	
	// any additional attributes given
	private HashMap<String, String> attrs = new HashMap<String, String>();
	
	/**
	 * 
	 */
	AAAConfig(String implClass, Side side, List<Element> configElements) {
		this.implClass = implClass;
		this.configElements = configElements;
		this.side = side;
	}

	public final List<Element> getConfigElements() {
		return configElements;
	}

	public final String getImplClass() {
		return implClass;
	}

	public final Side getSide() {
		return this.side;
	}
	
	final void setAttribute(String name, String value) {
		this.attrs.put(name, value);
	}
	
	public final String getAttribute(String name) {
		return this.getAttribute(name, null);
	}

	public final String getAttribute(String name, String defValue) {
		if(this.attrs.containsKey(name))
			return this.attrs.get(name);
		
		return defValue;
	}
}
