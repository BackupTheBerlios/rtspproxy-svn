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
public class RdtStreamEndPacketTest {

		@Test()
        public void testRdtStreamEndPacketStream0Load() throws IOException {
			RdtStreamEndPacket packet = (RdtStreamEndPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtStreamEndPacketStream0.txt"));
			
			assertEquals(packet.isNeedReliable(), true);
			assertEquals(packet.getStreamId(), 0);
			assertEquals(packet.isPacketSent(), true);
			assertEquals(packet.isExtFlag(), false);
			assertEquals(packet.getStreamEndSequenceNumber(), 1423);
			assertEquals(packet.getTimestamp(), 23726);
			assertEquals(packet.getTotalReliable(), 0);
		}
		
		@Test()
        public void testRdtStreamEndPacketStream0Save() throws IOException {
			ByteBuffer buffer = BufferUtils.loadBuffer("RdtStreamEndPacketStream0.txt");
			RdtStreamEndPacket packet = (RdtStreamEndPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtStreamEndPacketStream0.txt"));
	
			assertTrue(BufferUtils.buffersEqual(buffer, packet.toByteBuffer()));
		}


		@Test()
        public void testRdtStreamEndPacketStream1Load() throws IOException {
			RdtStreamEndPacket packet = (RdtStreamEndPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtStreamEndPacketStream1.txt"));
			
			assertEquals(packet.isNeedReliable(), true);
			assertEquals(packet.getStreamId(), 1);
			assertEquals(packet.isPacketSent(), true);
			assertEquals(packet.isExtFlag(), false);
			assertEquals(packet.getStreamEndSequenceNumber(), 271);
			assertEquals(packet.getTimestamp(), 31462);
			assertEquals(packet.getTotalReliable(), 0);
		}
		
		@Test()
        public void testRdtStreamEndPacketStream1Save() throws IOException {
			ByteBuffer buffer = BufferUtils.loadBuffer("RdtStreamEndPacketStream1.txt");
			RdtStreamEndPacket packet = (RdtStreamEndPacket)RdtPacketDecoder.decode(BufferUtils.loadBuffer("RdtStreamEndPacketStream1.txt"));
	
			assertTrue(BufferUtils.buffersEqual(buffer, packet.toByteBuffer()));
		}
}
