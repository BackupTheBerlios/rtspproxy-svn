/**
 * 
 */
package rtspproxy.config;

import java.util.List;

import org.dom4j.Element;

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
	
	/**
	 * 
	 */
	AAAConfig(String implClass, List<Element> configElements) {
		this.implClass = implClass;
		this.configElements = configElements;
	}

	public final List<Element> getConfigElements() {
		return configElements;
	}

	public final String getImplClass() {
		return implClass;
	}

}
