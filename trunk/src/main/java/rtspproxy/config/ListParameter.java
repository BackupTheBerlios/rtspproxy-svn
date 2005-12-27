/**
 * 
 */
package rtspproxy.config;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public abstract class ListParameter extends Parameter {

	/**
	 * @param name
	 * @param mutable
	 * @param description
	 * @param xpathExpr
	 */
	public ListParameter(String name, boolean mutable, String description,
			String xpathExpr) {
		super(name, mutable, description, xpathExpr);
	}

	/**
	 * add a value to the list
	 */
	public abstract void addValue(String value);
}
