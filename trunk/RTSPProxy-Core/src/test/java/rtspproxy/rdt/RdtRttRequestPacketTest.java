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
public class RdtRttRequestPacketTest extends TestCase {

	public void testRdtRttRequestPacketLoad() throws IOException {
		RdtRttRequestPacket packet = 
			(RdtRttRequestPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtRttRequestPacket.txt"));
		
		assertEquals(packet.isLengthIncluded(), false);
	}

	public void testRdtRttRequestPacketSave() throws IOException {
		ByteBuffer buffer = BufferUtils.loadBuffer("RdtRttRequestPacket.txt");
		RdtRttRequestPacket packet = 
			(RdtRttRequestPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtRttRequestPacket.txt"));
		
		assertTrue(BufferUtils.buffersEqual(buffer, packet.toByteBuffer()));
	}
}
