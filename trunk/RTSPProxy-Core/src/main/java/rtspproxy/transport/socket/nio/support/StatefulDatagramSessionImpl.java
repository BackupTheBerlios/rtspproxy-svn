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

import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoSessionManager;
import org.apache.mina.common.TransportType;
import org.apache.mina.common.support.BaseIoSession;

import rtspproxy.transport.socket.nio.StatefulDatagramSession;

class StatefulDatagramSessionImpl extends BaseIoSession implements
		StatefulDatagramSession {

	private SocketAddress localAddr;
	private SocketAddress remoteAddr;
	private IoHandler handler;
	private IoSession downsideSession;
	
	/**
	 * only constructable from within this package
	 */
	StatefulDatagramSessionImpl(IoHandler handler, SocketAddress localAddr, SocketAddress remoteAddr) {
		this.handler = handler;
		this.localAddr = localAddr;
		this.remoteAddr = remoteAddr;
	}
	
	@Override
	protected void updateTrafficMask() {
		// TODO Auto-generated method stub

	}

	public IoSessionManager getManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public IoHandler getHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	public IoFilterChain getFilterChain() {
		// TODO Auto-generated method stub
		return null;
	}

	public TransportType getTransportType() {
		// TODO Auto-generated method stub
		return null;
	}

	public SocketAddress getRemoteAddress() {
		return this.localAddr;
	}

	public SocketAddress getLocalAddress() {
		return this.remoteAddr;
	}

	public int getScheduledWriteRequests() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setSessionTimeout(int timeout) {
		// TODO Auto-generated method stub
		
	}

	public int getSessionTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void fireExceptionCaught(StatefulDatagramSessionImpl relay, Throwable t) {
		// TODO Auto-generated method stub
		
	}

	public void fireMessageReceived(StatefulDatagramSessionImpl relay, Object message) {
		// TODO Auto-generated method stub
		
	}

	IoSession getDownsideSession() {
		return downsideSession;
	}

	void setDownsideSession(IoSession downsideSession) {
		this.downsideSession = downsideSession;
	}

}
