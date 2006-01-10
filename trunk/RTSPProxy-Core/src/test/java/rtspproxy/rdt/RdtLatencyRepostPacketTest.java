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
public class RdtLatencyRepostPacketTest extends TestCase {

	public void testRdtLatencyReportPacketLoad() throws IOException {
		RdtLatencyReportPacket packet = (RdtLatencyReportPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtLatencyReportWithDataPacket.txt"));
		
		assertEquals(packet.isLengthIncluded(), true);
		assertEquals(packet.getServerTimeout(), 0);
	}

	public void testRdtLatencyReportPacketDataLoad() throws IOException {
		RdtLatencyReportPacket packet = (RdtLatencyReportPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtLatencyReportWithDataPacket.txt"));
		
		assertEquals(packet.isLengthIncluded(), true);
		assertEquals(packet.getServerTimeout(), 0);
		
		RdtDataPacket subPacket = (RdtDataPacket)packet.getSubPacket();
		
		assertEquals(subPacket.isLengthIncluded(), false);
		assertEquals(subPacket.isNeedReliable(), true);
		assertEquals(subPacket.getStreamId(), 0);
		assertEquals(subPacket.getSequence(), 0);
		assertEquals(subPacket.isBackToBack(), false);
		assertEquals(subPacket.isSlowData(), false);
		assertEquals(subPacket.getAsmRule(), 0);
		assertEquals(subPacket.getTimestamp(), 0);
		assertEquals(subPacket.getTotalReliable(), 0);
	}

	public void testRdtLatencyReportPacketSave() throws IOException {
		ByteBuffer buffer = BufferUtils.loadBuffer("RdtLatencyReportWithDataPacket.txt");
		RdtLatencyReportPacket packet = (RdtLatencyReportPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtLatencyReportWithDataPacket.txt"));
		
		assertTrue(BufferUtils.buffersEqual(buffer, packet.toByteBuffer()));
	}
}
