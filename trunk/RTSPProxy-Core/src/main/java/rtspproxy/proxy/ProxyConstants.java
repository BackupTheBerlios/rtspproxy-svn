/**
 * 
 */
package rtspproxy.proxy;

/**
 * Shared constants
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public interface ProxyConstants {

	/**
	 * Session attribute of a map shared between client and server RTSP session
	 */
	public static final String RSTP_SHARED_SESSION_ATTRIBUTE = 
		ProxyHandler.class.getName() + ".SharedSessionArttributes";

}
