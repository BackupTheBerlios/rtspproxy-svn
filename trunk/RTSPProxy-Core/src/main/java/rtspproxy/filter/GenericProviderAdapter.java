/**
 * 
 */
package rtspproxy.filter;

/**
 * Default implementation of the GenericProivder interface. Provides empty default 
 * implementations.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class GenericProviderAdapter implements GenericProvider {

	/* (non-Javadoc)
	 * @see rtspproxy.filter.GenericProvider#init()
	 */
	public void init() throws Exception {
	}

	/* (non-Javadoc)
	 * @see rtspproxy.filter.GenericProvider#shutdown()
	 */
	public void shutdown() {
	}

}
