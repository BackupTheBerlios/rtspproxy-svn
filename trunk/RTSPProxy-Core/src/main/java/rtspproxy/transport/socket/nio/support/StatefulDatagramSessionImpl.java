/**
 *   @(#) $Id: DatagramAcceptor.java 355016 2005-12-08 07:00:30Z trustin $
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

import java.net.SocketAddress;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.ByteBufferProxy;
import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoSessionManager;
import org.apache.mina.common.TransportType;
import org.apache.mina.common.IoFilter.WriteRequest;
import org.apache.mina.common.support.BaseIoSession;
import org.apache.mina.util.ByteBufferUtil;

import rtspproxy.transport.socket.nio.StatefulDatagramSession;

class StatefulDatagramSessionImpl extends BaseIoSession implements
		StatefulDatagramSession {
	
	private static class DecoupledByteBuffer extends ByteBufferProxy {

		protected DecoupledByteBuffer(ByteBuffer buffer) {
			super(buffer);
		}

		@Override
		public void acquire() {
		}

		@Override
		public void release() {
		}
	}

	private SocketAddress localAddr;
	private SocketAddress remoteAddr;
	private IoHandler handler;
	private StatefulDatagramSessionManager sessionManager;
	private StatefulDatagramSessionFilterChain filterChain;
	private SessionAwareDatagramAcceptorDelegate delegate;
	
	/**
	 * only constructable from within this package
	 */
	StatefulDatagramSessionImpl(IoHandler handler, SocketAddress localAddr, SocketAddress remoteAddr,
			StatefulDatagramSessionManager sessionManager) {
		this.handler = handler;
		this.localAddr = localAddr;
		this.remoteAddr = remoteAddr;
		this.sessionManager = sessionManager;
		this.delegate = sessionManager.getDelegate();
		
		this.filterChain = new StatefulDatagramSessionFilterChain(this);
	}
	
	@Override
	protected void updateTrafficMask() {
		// TODO Auto-generated method stub

	}

	public IoSessionManager getManager() {
		return this.sessionManager;
	}

	public IoHandler getHandler() {
		return handler;
	}

	public IoFilterChain getFilterChain() {
		return this.filterChain;
	}

	public TransportType getTransportType() {
		return TransportType.DATAGRAM;
	}

	public SocketAddress getRemoteAddress() {
		return this.remoteAddr;
	}

	public SocketAddress getLocalAddress() {
		return this.localAddr;
	}

	public int getScheduledWriteRequests() {
		return 0;
	}

	public void setSessionTimeout(int timeout) {
		// TODO Auto-generated method stub
		
	}

	public int getSessionTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	SessionAwareDatagramAcceptorDelegate getDelegate() {
		return delegate;
	}

	public void doClose(CloseFuture closeFuture) {
		this.sessionManager.closeSession(this, handler);
		
		closeFuture.setClosed();
	}

	void fireExceptionCaught(Throwable t) throws Exception {
		this.filterChain.exceptionCaught(this, t);
	}

	void fireMessageReceived(Object message) {
		ByteBufferProxy proxy = new DecoupledByteBuffer((ByteBuffer)message);
		
		this.filterChain.messageReceived(this, proxy);
	}

	@Override
	protected void write0(WriteRequest wrReq) {
		this.filterChain.filterWrite(this, wrReq);
	}

	@Override
	protected void close0(CloseFuture arg0) {
		this.sessionManager.closeSession(this, this.handler);
		
		super.close0(arg0);
	}
}
