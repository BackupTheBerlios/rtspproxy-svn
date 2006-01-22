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

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

class SessionAwareDatagramHandler extends IoHandlerAdapter implements IoHandler {

	private SocketAddress localAddress;
	private IoHandler wrapped;
	private IoFilterChainBuilder chainBuilder;
	
	private StatefulDatagramSessionImpl defaultSession;
	private HashMap<SocketAddress, StatefulDatagramSessionImpl> sessions = 
		new HashMap<SocketAddress, StatefulDatagramSessionImpl>();
	
	/**
	 * create an instance
	 */
	SessionAwareDatagramHandler(SocketAddress localAddress, IoHandler wrapped, 
			IoFilterChainBuilder chainBuilder) {
		this.localAddress = localAddress;
		this.wrapped = wrapped;
		this.chainBuilder = chainBuilder;
	}
	
	public void exceptionCaught(IoSession session, Throwable t) {
		StatefulDatagramSessionImpl relay = getRelaySession(session.getRemoteAddress());
		
		relay.setDownsideSession(session);
		relay.fireExceptionCaught(relay, t);
	}

	public void messageReceived(IoSession session, Object message) throws Exception {
		StatefulDatagramSessionImpl relay = getRelaySession(session.getRemoteAddress());
		
		relay.setDownsideSession(session);
		relay.fireMessageReceived(relay, message);
	}

	public void messageSent(IoSession session, Object message) throws Exception {
		StatefulDatagramSessionImpl relay = (StatefulDatagramSessionImpl)session;
		
		relay.getDownsideSession().write(message);
	}

	/**
	 * handle the acceptor unbind operation. Do this by closing all open sessions.
	 */
	public void unbind() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * get a session for a remote peer. If there is no session for the remote peer,
	 * a fresh one gets created and lifecycle methods are called.
	 * @param addr the remotem peer address. If null, the default session is used.
	 */
	private StatefulDatagramSessionImpl getRelaySession(SocketAddress addr) {
		StatefulDatagramSessionImpl session = null;
		
		if(addr == null) {
			session = this.defaultSession;
			
			if(session == null) {
				session = createSession(addr);
				
				this.defaultSession = session;
			}
		} else {
			session = this.sessions.get(addr);
			
			if(session == null) {
				session = createSession(addr);
				
				this.sessions.put(addr, session);
			}
		}
		
		return session;
	}
	
	/**
	 * create a session for a remote peer. Lifecycle methods are fired accordingly
	 */
	private StatefulDatagramSessionImpl createSession(SocketAddress addr) {
		StatefulDatagramSessionImpl session = null;
		
		return session;
	}
}
