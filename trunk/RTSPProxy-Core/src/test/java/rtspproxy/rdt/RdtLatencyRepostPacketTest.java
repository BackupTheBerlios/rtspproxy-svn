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
public class RdtLatencyRepostPacketTest {

	@Test()
    public void testRdtLatencyReportPacketLoad() throws IOException {
		RdtLatencyReportPacket packet = (RdtLatencyReportPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtLatencyReportWithDataPacket.txt"));
		
		assertEquals(packet.isLengthIncluded(), true);
		assertEquals(packet.getServerTimeout(), 0);
	}

	@Test()
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

	@Test()
    public void testRdtLatencyReportPacketSave() throws IOException {
		ByteBuffer buffer = BufferUtils.loadBuffer("RdtLatencyReportWithDataPacket.txt");
		RdtLatencyReportPacket packet = (RdtLatencyReportPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtLatencyReportWithDataPacket.txt"));
		
		assertTrue(BufferUtils.buffersEqual(buffer, packet.toByteBuffer()));
	}
}
