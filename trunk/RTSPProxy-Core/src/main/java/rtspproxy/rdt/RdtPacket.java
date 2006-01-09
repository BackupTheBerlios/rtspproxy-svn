/**
 * 
 */
package rtspproxy.rdt;

import java.util.HashMap;

import org.apache.commons.collections.functors.NonePredicate;
import org.apache.mina.common.ByteBuffer;

import rtspproxy.rtp.RtpPacket;

/**
 * Base class of specific RDT packet types.
 * The knowledge of packet types, codes and bit packing was taken from analyzing
 * RDT streams with ethereal 0.10.14
 *  
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 * @see <a href="http://www.ethereal.com/">ethereal homepage</a>
 */
public abstract class RdtPacket {
	
	/**
	 * Defines the various packet types
	 * The knowledge of packet types, codes and bit packing was taken from analyzing
	 * RDT streams with ethereal 0.10.14
	 *  
	 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
	 * @see <a href="http://www.ethereal.com/">ethereal homepage</a>
	 */
	public enum Type {
		None, RttRequest, RttResponse, LatencyReport, Ack, StreamEnd, Data;
		
		private static HashMap<Type, Short> typeCodeMap = new HashMap<Type, Short>();
		private static HashMap<Short, Type> codeTypeMap = new HashMap<Short, Type>();
		private static void populateMaps(Type type, short code) {
			typeCodeMap.put(type, code);
			codeTypeMap.put(code, type);
		}
		static {
			populateMaps(RttRequest, (short)0xff03);
			populateMaps(RttResponse, (short)0xff04);
			populateMaps(LatencyReport, (short)0xff08);
			populateMaps(Ack, (short)0xff02);
			populateMaps(StreamEnd, (short)0xff06);
		};
		
		public short toCode() {
			short code = 0;

			if(typeCodeMap.containsKey(this))
				code = typeCodeMap.get(this);
			
			return code;
		};
		
		public static Type fromCode(short code) {
			Type type = None;

			if(codeTypeMap.containsKey(code))
				type = codeTypeMap.get(code);
			
			return type;
		}
		
		public byte[] toByteArray() {
			byte[] rep = new byte[2];
			short code;
			
			if(typeCodeMap.containsKey(this)) {
				code = typeCodeMap.get(this);
				
				rep[0] = (byte)((code >> 8) & 0x0ff);
				rep[1] = (byte)(code  & 0x0ff);
			} else 
				rep[0] = rep[1] = -1;
			
			return rep;
		}
	};
	
	// type field
	private Type type = Type.None;
	
	// payload
	private byte[] payload = null;
	
	// need reliable flag
	private boolean needReliable = false;

	// stream id
	private byte streamId = -1;
	
	// attached sub packet
	private RdtPacket subPacket = null;
	
	/**
	 * constructor
	 */
	protected RdtPacket(Type type, boolean needReliable, byte streamId) {
		this.type = type;
		this.needReliable = needReliable;
		this.streamId = streamId;
	}
	
	/**
	 * @return Returns the type.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @return Returns the needReliable.
	 */
	public boolean isNeedReliable() {
		return needReliable;
	}

	/**
	 * @param needReliable The needReliable to set.
	 */
	public void setNeedReliable(boolean needReliable) {
		this.needReliable = needReliable;
	}
	
	/**
	 * encode packet as byte buffer
	 */
	public ByteBuffer toByteBuffer() {
		ByteBuffer buf = ByteBuffer.allocate(128, true);

		buf.put(buildHeader());
		if(this.payload != null)
			buf.put(this.payload);
		if(this.subPacket != null)
			buf.put(this.subPacket.toByteBuffer());

		return buf;
	}

	
	/**
	 * build the package header
	 */
	protected abstract ByteBuffer buildHeader(); 
	
	/**
	 * @return Returns the subPacket.
	 */
	public RdtPacket getSubPacket() {
		return subPacket;
	}

	/**
	 * @param subPacket The subPacket to set.
	 */
	public void setSubPacket(RdtPacket subPacket) {
		this.subPacket = subPacket;
	}

	/**
	 * @return Returns the payload.
	 */
	public byte[] getPayload() {
		return payload;
	}

	/**
	 * get the payload size
	 */
	public short getPayloadSize() {
		if(this.payload == null)
			return 0;
		return (short)this.payload.length;
	}
	
	/**
	 * @param payload The payload to set.
	 */
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}
	
	/**
	 * debug output
	 */
	public final String toString() {
		StringBuffer buf = new StringBuffer();

		buf.append("packet[type=" + type);
		buf.append(" needReliable=" + this.needReliable);
		buf.append(" streamId=" + this.streamId);
		toStringHelper(buf);
		
		if(this.payload != null)
			buf.append(" data[" + this.payload.length + "]");
		if(this.subPacket != null)
			buf.append(" subpacket[" + this.subPacket + "]");

		buf.append("]");

		return buf.toString();
	}
	
	/**
	 * per packet-type specific toString output
	 */
	protected abstract void toStringHelper(StringBuffer buffer);

	/**
	 * @return Returns the streamId.
	 */
	public byte getStreamId() {
		return streamId;
	}

	/**
	 * @param streamId The streamId to set.
	 */
	public void setStreamId(byte streamId) {
		this.streamId = streamId;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(Type type) {
		this.type = type;
	}
	
	/**
	 * query if encoded packet should contain length field included
	 */
	protected boolean isLengthIncluded() {
		return (this.subPacket != null);
	}
	
	/**
	 * encode a short 
	 */
	protected byte[] encodeShort(short v) {
		byte[] buf = new byte[2];
		
		buf[0] = (byte)((v >> 8) & 0xff);
		buf[1] = (byte)(v & 0xff);
		
		return buf;
	}
	
	/**
	 * encode a short 
	 */
	protected byte[] encodeInt(int v) {
		byte[] buf = new byte[4];
		
		buf[0] = (byte)((v >> 24) & 0xff);
		buf[1] = (byte)((v >> 16) & 0xff);
		buf[2] = (byte)((v >> 8) & 0xff);
		buf[3] = (byte)(v & 0xff);
		
		return buf;
	}
	
}
