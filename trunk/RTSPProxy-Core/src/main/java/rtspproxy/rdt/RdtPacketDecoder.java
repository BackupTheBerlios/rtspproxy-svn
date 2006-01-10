/**
 * 
 */
package rtspproxy.rdt;

import java.lang.reflect.Array;

import org.apache.mina.common.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Decoder for RDT packets.
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 */
public class RdtPacketDecoder {

	// logger
	private static Logger logger = LoggerFactory.getLogger(RdtPacketDecoder.class);
	
	/**
	 * not instaniable
	 */
	private RdtPacketDecoder() {}
	
	/**
	 * decode packet
	 * @param buffer the byte buffer to decode packet from
	 */
	public static RdtPacket decode(ByteBuffer buffer) {
		byte[] data = new byte[buffer.position()];

		// copy buffer content into temp array
		buffer.rewind();
		buffer.get(data);

		return decode(data, 0, 0);
	}

	/**
	 * decode packet
	 * @param buffer the byte buffer to decode packet from
	 */
	public static RdtPacket decode(byte[] data, int ind, int depth) {
		RdtPacket packet = null;
		byte markerByte;
		byte seqLo, seqHi;
		short sequence;
		boolean lengthIncluded = false;
		short packetLength = -1;
		int payloadSize = -1;

		if(depth > 1) {
			logger.error("detected packet-decoding recursion overrun, aborting");
			
			return null;
		}
		
		// process marker byte
		markerByte = data[ind++];
		lengthIncluded = ((markerByte & (1<<7)) > 0);

		// process sequence / type field
		seqHi = data[ind++];
		seqLo = data[ind++];
		sequence = decodeShort(seqHi, seqLo);
		
		if(seqHi < 0 ) {
			logger.debug("decoding control packet");

			// extract streamid from marker byte
			byte streamId = (byte)((markerByte & 0x7c) >> 2);

			// control packet
			RdtPacket.Type type = RdtPacket.Type.fromCode(sequence);
			
			switch(type) {
			case RttRequest:
				// process packet length (if included)
				if(lengthIncluded) {
					packetLength = decodeShort(data, ind);
					ind += 2;
				}
				
				packet = new RdtRttRequestPacket();
				break;
			case RttResponse:
				// process packet length (if included)
				if(lengthIncluded) {
					packetLength = decodeShort(data, ind);
					ind += 2;
				}
				
				int roundtripTimestampSeconds = decodeInt(data, ind);
				int roundtripTimestampMicroeconds = decodeInt(data, ind+4);
				
				ind += 8;
				packet = new RdtRttResponsePacket(roundtripTimestampSeconds, roundtripTimestampMicroeconds);
				
				payloadSize = (lengthIncluded ? packetLength : (data.length - ind));
				
				if(payloadSize > 0)
					ind = attachPayload(packet, data, ind, payloadSize);
				
				break;
			case LatencyReport:
				// process packet length (if included)
				if(lengthIncluded) {
					packetLength = decodeShort(data, ind);
					ind += 2;
				}
				
				int serverTimeout = decodeInt(data, ind);
				
				ind += 4;
				packet = new RdtLatencyReportPacket(serverTimeout);
				break;
			case Ack:
				// process packet length (if included)
				if(lengthIncluded) {
					packetLength = decodeShort(data, ind);
					ind += 2;
				}
				
				boolean lostHigh = ((markerByte & (1<<6)) > 0);
				payloadSize = (lengthIncluded ? packetLength : (data.length - ind));

				packet = new RdtAckPacket(lostHigh);
				if(payloadSize > 0)
					ind = attachPayload(packet, data, ind, payloadSize);
				
				break;
			case StreamEnd:
				// in the stream end packet, the length-included serves as need reliable field
				boolean packetSent = ((markerByte & (1<<1)) > 0);
				boolean extFlag = ((markerByte & (1<<0)) > 0);
				short streamEndSequenceNumber = decodeShort(data, ind);
				int timeout = decodeInt(data, ind+2);
				short totalReliable = decodeShort(data, ind+6);
				
				ind += 8;
				
				// length included servers as need reliable (speical case)
				packet = new RdtStreamEndPacket(lengthIncluded, streamId, packetSent, extFlag, 
						streamEndSequenceNumber, timeout, totalReliable);
				
				payloadSize = (data.length - ind);
				if(payloadSize > 0)
					ind = attachPayload(packet, data, ind, payloadSize);
				break;
			}
		} else {
			logger.debug("decoding data packet");
			
			// data packet
			// process packet length (if included)
			if(lengthIncluded) {
				packetLength = decodeShort(data, ind);
				ind += 2;
			}
			
			// process marker byte
			boolean needReliable = ((markerByte & (1<<6)) > 0);
			boolean isReliable = ((markerByte & (1<<0)) > 0);
			byte streamId = (byte)((markerByte & 0x3e) >> 1);
			
			// process next control byte
			if(lengthIncluded)
				packetLength--;
			byte controlByte = data[ind++];
			boolean backToBack = ((controlByte & (1<<7)) > 0);
			boolean slowData = ((controlByte & (1<<6)) > 0);
			byte asmRule = (byte)(controlByte & 0x3f);
			
			// process timestamp
			if(lengthIncluded)
				packetLength -= 4;
			int timestamp = decodeInt(data, ind);
			ind += 4;
			
			// process total reliable count
			if(lengthIncluded)
				packetLength -= 2;

			packet = new RdtDataPacket(needReliable, isReliable, streamId,
					sequence, backToBack, slowData, asmRule, timestamp);
			if(needReliable) {
				short totalReliable = decodeShort(data, ind);

				ind += 2;
				((RdtDataPacket)packet).setTotalReliable(totalReliable);
				if(lengthIncluded)
					packetLength -= 2;
			}

			payloadSize = (lengthIncluded ? packetLength : (data.length - ind));
			if(payloadSize > 0)
				ind = attachPayload(packet, data, ind, payloadSize);
		}
		
		if(ind != data.length) {
			// handle attached subpacket
			logger.debug("handling attached sub-packet");
			
			packet.setSubPacket(decode(data, ind, depth+1));
		}
		
		if(logger.isDebugEnabled())
			logger.debug("decoded packet: " + packet);
		
		return packet;
	}
	
	/**
	 * attach payload to packet
	 */
	private static int attachPayload(RdtPacket packet, byte[] data, int ind, int size) {
		byte[] buf = new byte[size];
		
		System.arraycopy(data, ind, buf, 0, size);
		packet.setPayload(buf);
		
		return (ind + size);
	}
	
	/**
	 * decode a short from a byte array
	 */
	private static final short decodeShort(byte[] bytes, int ind) {
		return decodeShort(bytes[ind], bytes[ind+1]);
	}

	private static final short decodeShort(byte hi, byte lo) {
		return (short)((hi & 0xff) * 256 + (lo & 0xff));
	}
	
	/**
	 * decode an int 
	 */
	private static final int decodeInt(byte[] bytes, int ind) {
		return decodeInt(bytes[ind], bytes[ind+1], bytes[ind+2], bytes[ind+3]);
	}
	
	private static final int decodeInt(byte b3, byte b2, byte b1, byte b0) {
		return ((b3 & 0xff) * 16777216) + ((b2 & 0xff) * 65536) + ((b1 & 0xff) * 256) + (b0 & 0xff);
	}
}
