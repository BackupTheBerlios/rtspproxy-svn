/**
 * 
 */
package rtspproxy.lib;

/**
 * This enumeration defines the direction a filter is applied on.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 */
public enum Side {
	Any,     // filter is meant for any side
	Client,  // select filters that are applied on the client-side session
	Server;  // select filters that are applied on the server-side session
	
	public String toString() {
		switch(this) {
		case Any:
			return "any";
		case Client:
			return "client";
		case Server:
			return "server";
		default:
			return "unknown";
		}
	}
	
	public static Side fromString(String value) throws IllegalArgumentException {
		Side side;
		
		if(value == null || value.length() == 0)
			side = Any;
		else if(value.equalsIgnoreCase("server"))
			side = Server;
		else if(value.equalsIgnoreCase("client"))
			side = Client;
		else if(value.equalsIgnoreCase("any"))
			side = Any;
		else
			throw new IllegalArgumentException("invalid side value given: " + value);
		
		return side;
	}
}