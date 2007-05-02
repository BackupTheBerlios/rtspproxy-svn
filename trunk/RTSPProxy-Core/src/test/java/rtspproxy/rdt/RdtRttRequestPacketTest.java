/**
 * 
 */
package rtspproxy.rdt;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.mina.common.ByteBuffer;
import org.testng.annotations.Test;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class RdtRttRequestPacketTest {

	@Test()
    public void testRdtRttRequestPacketLoad() throws IOException {
		RdtRttRequestPacket packet = 
			(RdtRttRequestPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtRttRequestPacket.txt"));
		
		assertEquals(packet.isLengthIncluded(), false);
	}

	@Test()
    public void testRdtRttRequestPacketSave() throws IOException {
		ByteBuffer buffer = BufferUtils.loadBuffer("RdtRttRequestPacket.txt");
		RdtRttRequestPacket packet = 
			(RdtRttRequestPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtRttRequestPacket.txt"));
		
		assertTrue(BufferUtils.buffersEqual(buffer, packet.toByteBuffer()));
	}
}
