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
import java.util.HashMap;

import org.apache.mina.common.ExceptionMonitor;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

class SessionAwareDatagramHandler extends IoHandlerAdapter implements IoHandler {

	private SocketAddress localAddress;
	private IoHandler wrapped;
	private IoFilterChainBuilder chainBuilder;
	private StatefulDatagramSessionManager sessionManager;
	
	/**
	 * create an instance
	 * @param sessionManager 
	 */
	SessionAwareDatagramHandler(SocketAddress localAddress, IoHandler wrapped, 
			IoFilterChainBuilder chainBuilder, StatefulDatagramSessionManager sessionManager) {
		this.localAddress = localAddress;
		this.wrapped = wrapped;
		this.chainBuilder = chainBuilder;
		this.sessionManager = sessionManager;
	}
	
	public void exceptionCaught(IoSession session, Throwable t) throws Exception {
		StatefulDatagramSessionImpl relay = this.sessionManager.getSession(localAddress, session.getRemoteAddress(),
				this.wrapped, this.chainBuilder);
		
		relay.setDownsideSession(session);
		this.wrapped.exceptionCaught(relay, t);
	}

	public void messageReceived(IoSession session, Object message) throws Exception {
		StatefulDatagramSessionImpl relay = this.sessionManager.getSession(localAddress, session.getRemoteAddress(),
				this.wrapped, this.chainBuilder);
		
		relay.setDownsideSession(session);
		this.wrapped.messageReceived(relay, message);
	}

	public void messageSent(IoSession session, Object message) throws Exception {
		StatefulDatagramSessionImpl relay = (StatefulDatagramSessionImpl)session;
		
		relay.getDownsideSession().write(message);
	}

	IoFilterChainBuilder getFilterChainBuilder() {
		return this.chainBuilder;
	}

	IoHandler getWrappedHandler() {
		return this.wrapped;
	}
}