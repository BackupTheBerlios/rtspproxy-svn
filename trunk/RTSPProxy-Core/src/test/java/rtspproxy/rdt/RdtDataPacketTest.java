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

	public void testRdtPacketBackToBackLoad() throws IOException {
		RdtDataPacket packet = (RdtDataPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtDataPacketDataPacket.txt"));
		RdtDataPacket subPacket = (RdtDataPacket)packet.getSubPacket();
		
		// packet 0
		assertEquals(packet.isNeedReliable(), true);
		assertEquals(packet.getStreamId(), 0);
		assertEquals(packet.isReliable(), false);
		assertEquals(packet.getSequence(), 1);
		assertEquals(packet.isBackToBack(), false);
		assertEquals(packet.isSlowData(), true);
		assertEquals(packet.getAsmRule(), 0);
		assertEquals(packet.getTimestamp(), 0);
		assertEquals(packet.getTotalReliable(), 0);
		assertEquals(packet.getPayload().length, 145);
		
		assertEquals(subPacket.isNeedReliable(), true);
		assertEquals(subPacket.getStreamId(), 0);
		assertEquals(subPacket.isReliable(), false);
		assertEquals(subPacket.getSequence(), 2);
		assertEquals(subPacket.isBackToBack(), false);
		assertEquals(subPacket.isSlowData(), true);
		assertEquals(subPacket.getAsmRule(), 0);
		assertEquals(subPacket.getTimestamp(), 266);
		assertEquals(subPacket.getTotalReliable(), 0);
		assertEquals(subPacket.getPayload().length, 127);		
	}
	
	public void testRdtPacketBackToBackSave() throws IOException {
		ByteBuffer origBuffer = BufferUtils.loadBuffer("RdtDataPacketDataPacket.txt");
		RdtDataPacket packet = (RdtDataPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtDataPacketDataPacket.txt"));

		assertTrue(BufferUtils.buffersEqual(origBuffer, packet.toByteBuffer()));
	}
}
