/**
 * 
 */
package rtspproxy.rdt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.apache.mina.common.ByteBuffer;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class BufferUtils {

	/**
	 * 
	 */
	private BufferUtils() {
	}

	public static ByteBuffer loadBuffer(String fName) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(16);
		
		buffer.setAutoExpand(true);

		File baseDir = new File(System.getProperty("basedir"));
		File packetDump = new File(baseDir, 	"src/test/resources/rtspproxy/rdt/" + fName);

		buffer = ByteBuffer.allocate(32); 
		buffer.setAutoExpand(true);
		
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(
				new FileInputStream(packetDump)));
		String line;

		while ((line = lnr.readLine()) != null) {
			int pos = 0;
			int nibblePos = 0;
			byte b = 0;

			for (pos = 0; pos < line.length(); ++pos) {
				char c = line.charAt(pos);
				if (!Character.isWhitespace(c)) {
					byte n = 0;

					if (c >= '0' && c <= '9') {
						n = (byte) (c - '0');
					} else if (c >= 'a' && c <= 'f') {
						n = (byte) (c - 'a' + 10);
					} else if (c >= 'A' && c <= 'F') {
						n = (byte) (c - 'A' + 10);
					} else
						throw new IllegalArgumentException(
								"invalid character '" + c + "' in packet dump");
					if (nibblePos == 0) {
						b = (byte)(n << 4);
						nibblePos++;
					} else {
						b |= n;
						nibblePos = 0;
						buffer.put(b);
					}
				}
			}
		}
		buffer.limit(buffer.position());
		
		return buffer;
	}
	
	/**
	 * compare two buffers for equality
	 */
	public static final boolean buffersEqual(ByteBuffer first, ByteBuffer second) {
		boolean equal = false;
		
		/*
		System.out.println("first limit=" + first.limit() + ", second limit=" + second.limit());
		*/
		if(first.limit() == second.limit()) {
			int i;
			
			for(i=0; i<first.limit(); ++i) {
				if(first.get(i) != second.get(i)) {
					/*
					System.out.println("index " + i + ": expected=" + Integer.toHexString(first.get(i))
							+ ": got=" + Integer.toHexString(second.get(i)));
							*/
					break;
				}
			}

			if(i == first.limit())
				equal = true;
		}
		
		return equal;
	}
}
