/**
 * 
 */
package rtspproxy.rdt;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.mina.common.ByteBuffer;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class RdtDataPacketTest extends TestCase {

	public void testRdtPacketStream0Load() throws IOException {
		RdtDataPacket packet = (RdtDataPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtDataPacketStream0.txt"));
		
		assertEquals(packet.isNeedReliable(), true);
		assertEquals(packet.getStreamId(), 0);
		assertEquals(packet.isReliable(), false);
		assertEquals(packet.getSequence(), 1438);
		assertEquals(packet.isBackToBack(), false);
		assertEquals(packet.isSlowData(), false);
		assertEquals(packet.getAsmRule(), 1);
		assertEquals(packet.getTimestamp(), 23993);
		assertEquals(packet.getTotalReliable(), 0);
	}
	
	public void testRdtPacketStream0Save() throws IOException {
		ByteBuffer origBuffer = BufferUtils.loadBuffer("RdtDataPacketStream0.txt");
		RdtDataPacket packet = (RdtDataPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtDataPacketStream0.txt"));

		assertTrue(BufferUtils.buffersEqual(origBuffer, packet.toByteBuffer()));
	}

	public void testRdtPacketStream1Load() throws IOException {
		RdtDataPacket packet = (RdtDataPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtDataPacketStream1.txt"));
		
		assertEquals(packet.isNeedReliable(), true);
		assertEquals(packet.getStreamId(), 1);
		assertEquals(packet.isReliable(), false);
		assertEquals(packet.getSequence(), 3);
		assertEquals(packet.isBackToBack(), false);
		assertEquals(packet.isSlowData(), false);
		assertEquals(packet.getAsmRule(), 1);
		assertEquals(packet.getTimestamp(), 348);
		assertEquals(packet.getTotalReliable(), 0);
	}
	
	public void testRdtPacketStream1Save() throws IOException {
		ByteBuffer origBuffer = BufferUtils.loadBuffer("RdtDataPacketStream1.txt");
		RdtDataPacket packet = (RdtDataPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtDataPacketStream1.txt"));

		assertTrue(BufferUtils.buffersEqual(origBuffer, packet.toByteBuffer()));
	}
}
