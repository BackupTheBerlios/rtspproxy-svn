/**
 * 
 */
package rtspproxy.rdt;

import java.io.IOException;

import org.apache.mina.common.ByteBuffer;

import junit.framework.TestCase;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class RdtAckPacketTest extends TestCase {

	public void testRdtAckPacketLoad() throws IOException {
		RdtAckPacket packet = (RdtAckPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtAckPacket.txt"));
		
		assertEquals(packet.isLengthIncluded(), false);
		assertEquals(packet.isLostHigh(), false);
	}
	
	public void testRdtAckPacketSave() throws IOException {
		ByteBuffer buffer = BufferUtils.loadBuffer("RdtAckPacket.txt");
		RdtAckPacket packet = (RdtAckPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtAckPacket.txt"));
		
		assertTrue(BufferUtils.buffersEqual(buffer, packet.toByteBuffer()));
	}
}
