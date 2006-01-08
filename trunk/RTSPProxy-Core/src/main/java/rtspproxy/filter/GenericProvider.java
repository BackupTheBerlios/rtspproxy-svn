/**
 * 
 */
package rtspproxy.filter;

/**
 * Generic interface used as a parent interface to specific filter provider definitions.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public interface GenericProvider {

	/**
	 * Called once at service startup. Should be used to initialize the
	 * provider.
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception;

	/**
	 * Called once at service shutdown.
	 */
	public void shutdown();

}
