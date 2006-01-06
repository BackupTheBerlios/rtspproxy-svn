/**
 * 
 */
package rtspproxy.config;

import java.util.List;

import org.dom4j.Element;

/**
 * This interface is implemented by filters which can be configured via the XML 
 * mechanism
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 */
public interface AAAConfigurable {
	/**
	 * configure the filter.
	 * @param configElements a list of dom4j elements containing the actual configuration
	 */
	public void configure(List<Element> configElements) throws Exception;
}
