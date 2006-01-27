/**
 * 
 */
package rtspproxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import rtspproxy.lib.Side;

/**
 * This class implements a parser for XML configuration files.
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 * @todo validate the parsed configuration file by a schema.
 */
public class XMLConfigReader {

	// logger
	private static final Logger logger = LoggerFactory.getLogger(XMLConfigReader.class);
	
	/**
	 * create the xml config read object
	 */
	public XMLConfigReader() {}
	
	/**
	 * read the configuration file.
	 * @param fName the pathname of the configuration file
	 * @exception IOException the file denoted by the file name cannot be read
	 * @throws DocumentException parsing the config file failed.
	 */ 
	public final void readConfig(String fName) throws IOException, DocumentException {
		logger.debug("radConfig, fName=" + fName);
		File fFile = new File(fName);
		
		if(fFile.canRead())
			this.readConfig(new FileInputStream(fFile));
	}
	
	/**
	 * read the configuration file
	 * @param is the input stream to read the configuration from.
	 * @throws DocumentException parsing the input stream failed.
	 * @throws IllegalArgumentException invalid parameter value given
	 */
	public final void readConfig(InputStream is) throws DocumentException, IllegalArgumentException {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(is); // TODO validate the document by a schema
		
		for(Parameter param : Config.getAllParameters()) {
			String xpathExpr = param.getXPathExpr();
			
			if(xpathExpr != null) {
				logger.debug("evaluating parameter " + param.getName() + " with xpath " + xpathExpr);
				
				List<Node> nodes = (List<Node>)doc.selectNodes(xpathExpr);
				
				if(nodes.size() > 1) {
					if(param instanceof ListParameter) {
						for(Node node : nodes) {
							String value = null;
							
							if(node instanceof Attribute)
								value = ((Attribute)node).getText();
							else if(node instanceof Element)
								value = ((Element)node).getTextTrim();
							
							if(value == null)
								throw new IllegalArgumentException("Invalid value specified for parameter " + param.getName());
							
							((ListParameter)param).addValue(value);							
						}
					} else
						throw new IllegalArgumentException("Parameter " + param.getName() + " is not a list value");
				} else if(nodes.size() == 1){
					String value = null;
					Node node = nodes.get(0);
					
					if(node instanceof Attribute)
						value = ((Attribute)node).getText();
					else if(node instanceof Element)
						value = ((Element)node).getTextTrim();
					
					if(value == null)
						throw new IllegalArgumentException("Invalid value specified for parameter " + param.getName());
					
					param.setValue(value);
				}
			}
		}
		
		for(Node aaaNode : (List<Node>)doc.selectNodes("/rtspproxy/filters/*")) {
			String name = aaaNode.getName();
			String implClass = ((Element)aaaNode).attributeValue("implClass");
			Side side = Side.fromString(((Element)aaaNode).attributeValue("side"));
			
			logger.debug("element name=" + name + ", implClass=" + implClass + ",side=" + side);
			
			if(implClass == null || implClass.length() == 0)
				throw new IllegalArgumentException("no implementation class given");

			AAAConfig aaa = new AAAConfig(implClass, side, 
					(List<Element>)((Element)aaaNode).elements());
			
			for(Attribute attr : (List<Attribute>)((Element)aaaNode).attributes()) {
				if(attr.getName().equals("implClass") || attr.getName().equals("side"))
					continue;
				aaa.setAttribute(attr.getName(), attr.getText().trim());
			}
			
			if(name.equals("authentication")) {
				Config.addAuthenticationFilter(aaa);
			} else if(name.equals("ipaddress")) {
				Config.addIpAddressFilter(aaa);
			} else if(name.equals("accounting")) {
				Config.addAccountingFilter(aaa);				
			} else if(name.equals("rewriting")) {
				Config.addUrlRewritingFilter(aaa);
			} else if(name.equals("control")) {
				if(side == Side.Client || side == Side.Server) 
					Config.addControlFilter(aaa);
				else
					throw new IllegalArgumentException("control filters can be only applied to either client or server side, not both");
			} else
				throw new IllegalArgumentException("invalid AAA element given, name=" + name);
		}
	}
}
