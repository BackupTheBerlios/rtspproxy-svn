/*
 *   @(#) $Id: DatagramConnectorDelegate.java 355016 2005-12-08 07:00:30Z trustin $
 *
 *   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package rtspproxy.transport.socket.nio.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.ExceptionMonitor;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoFilter.WriteRequest;
import org.apache.mina.common.support.BaseIoConnector;
import org.apache.mina.util.Queue;

/**
 * {@link IoConnector} for datagram transport (UDP/IP).
 * 
 * @author The Apache Directory Project (dev@directory.apache.org)
 * @version $Rev: 355016 $, $Date: 2005-12-08 16:00:30 +0900 (Thu, 08 Dec 2005) $
 */
public class DatagramConnectorDelegate extends BaseIoConnector implements DatagramSessionManager
{
    private static volatile int nextId = 0;

    private final IoConnector wrapper;
    private final int id = nextId ++ ;
    private Selector selector;
    private final Queue registerQueue = new Queue();
    private final Queue cancelQueue = new Queue();
    private final Queue flushingSessions = new Queue();
    private final Queue trafficControllingSessions = new Queue();
    private Worker worker;

    /**
     * Creates a new instance.
     */
    public DatagramConnectorDelegate( IoConnector wrapper )
    {
        this.wrapper = wrapper;
    }

    public ConnectFuture connect( SocketAddress address, IoHandler handler, IoFilterChainBuilder filterChainBuilder )
    {
        return connect( address, null, handler, filterChainBuilder );
    }

    public ConnectFuture connect( SocketAddress address, SocketAddress localAddress,
                                  IoHandler handler, IoFilterChainBuilder filterChainBuilder )
    {
        if( address == null )
            throw new NullPointerException( "address" );
        if( handler == null )
            throw new NullPointerException( "handler" );

        if( !( address instanceof InetSocketAddress ) )
            throw new IllegalArgumentException( "Unexpected address type: "
                                                + address.getClass() );
        
        if( localAddress != null && !( localAddress instanceof InetSocketAddress ) )
        {
            throw new IllegalArgumentException( "Unexpected local address type: "
                                                + localAddress.getClass() );
        }
        
        if( filterChainBuilder == null )
        {
            filterChainBuilder = IoFilterChainBuilder.NOOP;
        }
        
        DatagramChannel ch = null;
        boolean initialized = false;
        try
        {
            ch = DatagramChannel.open();
            ch.socket().setReuseAddress( true );
            if( localAddress != null )
            {
                ch.socket().bind( localAddress );
            }
            ch.connect( address );
            ch.configureBlocking( false );
            initialized = true;
        }
        catch( IOException e )
        {
            return ConnectFuture.newFailedFuture( e );
        }
        finally
        {
            if( !initialized && ch != null )
            {
                try
                {
                    ch.close();
                }
                catch( IOException e )
                {
                    ExceptionMonitor.getInstance().exceptionCaught( e );
                }
            }
        }

        RegistrationRequest request = new RegistrationRequest( ch, handler, filterChainBuilder );
        synchronized( this )
        {
            try
            {
                startupWorker();
            }
            catch( IOException e )
            {
                try
                {
                    ch.close();
                }
                catch( IOException e2 )
                {
                    ExceptionMonitor.getInstance().exceptionCaught( e2 );
                }

                return ConnectFuture.newFailedFuture( e );
            }
            
            synchronized( registerQueue )
            {
                registerQueue.push( request );
            }
        }

        selector.wakeup();
        return request;
    }
    
    private synchronized void startupWorker() throws IOException
    {
        if( worker == null )
        {
            selector = Selector.open();
            worker = new Worker();
            worker.start();
        }
    }

    public void closeSession( DatagramSessionImpl session )
    {
        synchronized( this )
        {
            try
            {
                startupWorker();
            }
            catch( IOException e )
            {
                // IOException is thrown only when Worker thread is not
                // running and failed to open a selector.  We simply return
                // silently here because it we can simply conclude that
                // this session is not managed by this connector or
                // already closed.
                return;
            }

            synchronized( cancelQueue )
            {
                cancelQueue.push( session );
            }
        }

        selector.wakeup();
    }

    public void flushSession( DatagramSessionImpl session )
    {
        scheduleFlush( session );
        Selector selector = this.selector;
        if( selector != null )
        {
            selector.wakeup();
        }
    }

    private void scheduleFlush( DatagramSessionImpl session )
    {
        synchronized( flushingSessions )
        {
            flushingSessions.push( session );
        }
    }

    public void updateTrafficMask( DatagramSessionImpl session )
    {
        scheduleTrafficControl( session );
        Selector selector = this.selector;
        if( selector != null )
        {
            selector.wakeup();
        }
        selector.wakeup();
    }
    
    private void scheduleTrafficControl( DatagramSessionImpl session )
    {
        synchronized( trafficControllingSessions )
        {
            trafficControllingSessions.push( session );
        }
    }
    
    private void doUpdateTrafficMask() 
    {
        if( trafficControllingSessions.isEmpty() )
            return;

        for( ;; )
        {
            DatagramSessionImpl session;

            synchronized( trafficControllingSessions )
            {
                session = ( DatagramSessionImpl ) trafficControllingSessions.pop();
            }

            if( session == null )
                break;

            SelectionKey key = session.getSelectionKey();
            // Retry later if session is not yet fully initialized.
            // (In case that Session.suspend??() or session.resume??() is 
            // called before addSession() is processed)
            if( key == null )
            {
                scheduleTrafficControl( session );
                break;
            }
            // skip if channel is already closed
            if( !key.isValid() )
            {
                continue;
            }

            // The normal is OP_READ and, if there are write requests in the
            // session's write queue, set OP_WRITE to trigger flushing.
            int ops = SelectionKey.OP_READ;
            Queue writeRequestQueue = session.getWriteRequestQueue();
            synchronized( writeRequestQueue )
            {
                if( !writeRequestQueue.isEmpty() )
                {
                    ops |= SelectionKey.OP_WRITE;
                }
            }

            // Now mask the preferred ops with the mask of the current session
            int mask = session.getTrafficMask().getInterestOps();
            key.interestOps( ops & mask );
        }
    }
    
    private class Worker extends Thread
    {
        public Worker()
        {
            super( "DatagramConnector-" + id );
        }

        public void run()
        {
            for( ;; )
            {
                try
                {
                    int nKeys = selector.select();

                    registerNew();
                    doUpdateTrafficMask();

                    if( nKeys > 0 )
                    {
                        processReadySessions( selector.selectedKeys() );
                    }

                    flushSessions();
                    cancelKeys();

                    if( selector.keys().isEmpty() )
                    {
                        synchronized( DatagramConnectorDelegate.this )
                        {
                            if( selector.keys().isEmpty() &&
                                registerQueue.isEmpty() &&
                                cancelQueue.isEmpty() )
                            {
                                worker = null;
                                try
                                {
                                    selector.close();
                                }
                                catch( IOException e )
                                {
                                    ExceptionMonitor.getInstance().exceptionCaught( e );
                                }
                                finally
                                {
                                    selector = null;
                                }
                                break;
                            }
                        }
                    }
                }
                catch( IOException e )
                {
                    ExceptionMonitor.getInstance().exceptionCaught(  e );

                    try
                    {
                        Thread.sleep( 1000 );
                    }
                    catch( InterruptedException e1 )
                    {
                    }
                }
            }
        }
    }

    private void processReadySessions( Set keys )
    {
        Iterator it = keys.iterator();
        while( it.hasNext() )
        {
            SelectionKey key = ( SelectionKey ) it.next();
            it.remove();

            DatagramSessionImpl session = ( DatagramSessionImpl ) key.attachment();

            if( key.isReadable() && session.getTrafficMask().isReadable() )
            {
                readSession( session );
            }

            if( key.isWritable() && session.getTrafficMask().isWritable() )
            {
                scheduleFlush( session );
            }
        }
    }

    private void readSession( DatagramSessionImpl session )
    {

        ByteBuffer readBuf = ByteBuffer.allocate( 2048 );
        try
        {
            int readBytes = session.getChannel().read( readBuf.buf() );
            if( readBytes > 0 )
            {
                readBuf.flip();
                ByteBuffer newBuf = ByteBuffer.allocate( readBuf.limit() );
                newBuf.put( readBuf );
                newBuf.flip();

                session.increaseReadBytes( readBytes );
                ( ( DatagramFilterChain ) session.getFilterChain() ).messageReceived( session, newBuf );
            }
        }
        catch( IOException e )
        {
            ( ( DatagramFilterChain ) session.getFilterChain() ).exceptionCaught( session, e );
        }
        finally
        {
            readBuf.release();
        }
    }

    private void flushSessions()
    {
        if( flushingSessions.size() == 0 )
            return;

        for( ;; )
        {
            DatagramSessionImpl session;

            synchronized( flushingSessions )
            {
                session = ( DatagramSessionImpl ) flushingSessions.pop();
            }

            if( session == null )
                break;

            try
            {
                flush( session );
            }
            catch( IOException e )
            {
                ( ( DatagramFilterChain ) session.getFilterChain() ).exceptionCaught( session, e );
            }
        }
    }

    private void flush( DatagramSessionImpl session ) throws IOException
    {
        DatagramChannel ch = session.getChannel();

        Queue writeRequestQueue = session.getWriteRequestQueue();

        WriteRequest req;
        for( ;; )
        {
            synchronized( writeRequestQueue )
            {
                req = ( WriteRequest ) writeRequestQueue.first();
            }

            if( req == null )
                break;

            ByteBuffer buf = ( ByteBuffer ) req.getMessage();
            if( buf.remaining() == 0 )
            {
                // pop and fire event
                synchronized( writeRequestQueue )
                {
                    writeRequestQueue.pop();
                }

                req.getFuture().setWritten( true );
                session.increaseWrittenWriteRequests();
                ( ( DatagramFilterChain ) session.getFilterChain() ).messageSent( session, buf );
                continue;
            }

            SelectionKey key = session.getSelectionKey();
            if( key == null )
            {
                scheduleFlush( session );
                break;
            }
            if( !key.isValid() )
            {
                continue;
            }

            int pos = buf.position();
            int writtenBytes = ch.write( buf.buf() );

            if( writtenBytes == 0 )
            {
                // Kernel buffer is full
                key.interestOps( key.interestOps() | SelectionKey.OP_WRITE );
            }
            else if( writtenBytes > 0 )
            {
                key.interestOps( key.interestOps()
                                 & ( ~SelectionKey.OP_WRITE ) );

                // pop and fire event
                synchronized( writeRequestQueue )
                {
                    writeRequestQueue.pop();
                }

                session.increaseWrittenBytes( writtenBytes );
                req.getFuture().setWritten( true );
                session.increaseWrittenWriteRequests();
                ( ( DatagramFilterChain ) session.getFilterChain() ).messageSent( session, buf.position( pos ) );
            }
        }
    }

    private void registerNew()
    {
        if( registerQueue.isEmpty() )
            return;

        for( ;; )
        {
            RegistrationRequest req;
            synchronized( registerQueue )
            {
                req = ( RegistrationRequest ) registerQueue.pop();
            }

            if( req == null )
                break;

            DatagramSessionImpl session =
                new DatagramSessionImpl( wrapper, this, req.channel, req.handler );

            boolean success = false;
            try
            {
                this.filterChainBuilder.buildFilterChain( session.getFilterChain() );
                req.filterChainBuilder.buildFilterChain( session.getFilterChain() );
                ( ( DatagramFilterChain ) session.getFilterChain() ).sessionCreated( session );

                SelectionKey key = req.channel.register( selector,
                        SelectionKey.OP_READ, session );
    
                session.setSelectionKey( key );

                req.setSession( session );
                success = true;
            }
            catch( Throwable t )
            {
                req.setException( t );
            }
            finally 
            {
                if( !success )
                {
                    try
                    {
                        req.channel.close();
                    }
                    catch (IOException e)
                    {
                        ExceptionMonitor.getInstance().exceptionCaught( e );
                    }
                }
            }
        }
    }

    private void cancelKeys()
    {
        if( cancelQueue.isEmpty() )
            return;

        for( ;; )
        {
            DatagramSessionImpl session;
            synchronized( cancelQueue )
            {
                session = ( DatagramSessionImpl ) cancelQueue.pop();
            }

            if( session == null )
                break;
            else
            {
                SelectionKey key = session.getSelectionKey();
                DatagramChannel ch = ( DatagramChannel ) key.channel();
                try
                {
                    ch.close();
                }
                catch( IOException e )
                {
                    ExceptionMonitor.getInstance().exceptionCaught( e );
                }
                session.getCloseFuture().setClosed();
                key.cancel();
                selector.wakeup(); // wake up again to trigger thread death
            }
        }
    }

    private static class RegistrationRequest extends ConnectFuture
    {
        private final DatagramChannel channel;
        private final IoHandler handler;
        private final IoFilterChainBuilder filterChainBuilder;

        private RegistrationRequest( DatagramChannel channel,
                                     IoHandler handler,
                                     IoFilterChainBuilder filterChainBuilder )
        {
            this.channel = channel;
            this.handler = handler;
            this.filterChainBuilder = filterChainBuilder;
        }
    }
}
