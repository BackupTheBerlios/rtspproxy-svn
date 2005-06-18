/**
 * 
 */
package rtspproxy;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * 
 */
public class Reactor
{

	private ServerSocket serverSocket = null;

	/**
	 * Constructor. Creates a new Reactor and starts it.
	 * 
	 * @throws IOException
	 */
	public Reactor() throws IOException
	{
		boolean listening = true;

		try {
			serverSocket = new ServerSocket( 5540 );
			
		} catch (IOException e) {
			System.err.println( "Could not listen on port: 5540." );
			System.exit( -1 );
		}

		while (listening) {
			// new KKMultiServerThread(serverSocket.accept()).start();
		}

		serverSocket.close();
	}

	/**
	 * @return Returns the serverSocket.
	 */
	public ServerSocket getServerSocket()
	{
		return serverSocket;
	}

	/**
	 * @param serverSocket
	 *            The serverSocket to set.
	 */
	public void setServerSocket(ServerSocket serverSocket)
	{
		this.serverSocket = serverSocket;
	}
}
