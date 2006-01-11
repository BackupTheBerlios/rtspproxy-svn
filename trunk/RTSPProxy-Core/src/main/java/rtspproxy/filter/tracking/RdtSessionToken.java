/**
 * 
 */
package rtspproxy.filter.tracking;

import java.net.SocketAddress;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class RdtSessionToken {

	// session attribute name
	public static final String SessionAttribute = "rdtSessionTrackingToken";
	
	private SocketAddress remoteServer;
	private int remotePort;
	
	/**
	 * 
	 */
	public RdtSessionToken(SocketAddress remoteServer, int remotePort) {
		this.remoteServer = remoteServer;
		this.remotePort = remotePort;
	}

	/**
	 * @return Returns the remotePort.
	 */
	public int getRemotePort() {
		return remotePort;
	}

	/**
	 * @return Returns the remoteServer.
	 */
	public SocketAddress getRemoteServer() {
		return remoteServer;
	}

}
