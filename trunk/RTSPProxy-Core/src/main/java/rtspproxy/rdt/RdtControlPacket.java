/**
 * 
 */
package rtspproxy.rdt;


/**
 * Common base class for all RDT control packets.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public abstract class RdtControlPacket extends RdtPacket {

	/**
	 * @param type
	 * @param needReliable
	 * @param streamId
	 */
	public RdtControlPacket(Type type, byte streamId) {
		super(type, false, streamId);
	}


}
