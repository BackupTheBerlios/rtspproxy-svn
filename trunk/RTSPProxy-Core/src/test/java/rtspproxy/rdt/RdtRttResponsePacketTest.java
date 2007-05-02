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
public class RdtRttResponsePacketTest {


	@Test()
    public void testRdtRttResponsePacketLoad() throws IOException {
		RdtRttResponsePacket packet = (RdtRttResponsePacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtRttResponsePacket.txt"));

		assertEquals(packet.isLengthIncluded(), false);
		assertEquals(packet.getRoundtripTimestampSeconds(), 1136818556);
		assertEquals(packet.getRoundtripTimestampMicroseconds(), 101211);
	}
	
	@Test()
    public void testRdtRttResponsePacketSave() throws IOException {
		ByteBuffer buffer = BufferUtils.loadBuffer("RdtRttResponsePacket.txt");
		RdtRttResponsePacket packet = (RdtRttResponsePacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtRttResponsePacket.txt"));
		
		assertTrue(BufferUtils.buffersEqual(buffer, packet.toByteBuffer()));
	}

}
