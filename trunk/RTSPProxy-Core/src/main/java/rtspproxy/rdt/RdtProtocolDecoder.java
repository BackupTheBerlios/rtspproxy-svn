/**
 * 
 */
package rtspproxy.rdt;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 * 
 */
public class RdtProtocolDecoder implements ProtocolDecoder
{
    
    private static Logger logger = LoggerFactory
            .getLogger( RdtProtocolDecoder.class );
    
    /**
     * 
     */
    RdtProtocolDecoder()
    {
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.filter.codec.ProtocolDecoder#decode(org.apache.mina.common.IoSession,
     *      org.apache.mina.common.ByteBuffer,
     *      org.apache.mina.filter.codec.ProtocolDecoderOutput)
     */
    public void decode( IoSession ioSession, ByteBuffer buffer,
            ProtocolDecoderOutput out ) throws Exception
    {
        try
        {
            RdtPacket rdtPacket = RdtPacketDecoder.decode( buffer );
            
            if ( logger.isDebugEnabled() )
                logger.debug( "received RDT packet: " + rdtPacket
                        + " from client " + ioSession.getRemoteAddress() );
            
            if ( rdtPacket == null )
                throw new IllegalStateException(
                        "RDT network packet cannot be decoded" );
            
            out.write( rdtPacket );
        } catch ( Exception e )
        {
            logger.debug( "error decoding packet", e );
            
            throw e;
        }
    }
    
    
    
    /* (non-Javadoc)
     * @see org.apache.mina.filter.codec.ProtocolDecoder#finishDecode(org.apache.mina.common.IoSession, org.apache.mina.filter.codec.ProtocolDecoderOutput)
     */
    public void finishDecode( IoSession session, ProtocolDecoderOutput out ) throws Exception
    {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.filter.codec.ProtocolDecoder#dispose(org.apache.mina.common.IoSession)
     */
    public void dispose( IoSession arg0 ) throws Exception
    {
        
    }
    
}
