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

import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoSessionManager;
import org.apache.mina.common.support.BaseIoSessionManager;

public class StatefulDatagramSessionManager extends BaseIoSessionManager
		implements IoSessionManager {

	private HashMap<SocketAddress, LocalSessionsHolder> sessions = new HashMap<SocketAddress, LocalSessionsHolder>();
	private SessionAwareDatagramAcceptorDelegate delegate;
	
	StatefulDatagramSessionManager(SessionAwareDatagramAcceptorDelegate delegate) {
		this.delegate = delegate;
	}

	/**
	 * obtain a session for a (local, remote) address pair. If a sessin does not exist,
	 * create a new one
	 * @param localAddr the local address
	 * @param remoteAddr the remote address
	 * @exception Exception session creation or initialization failed
	 */
	StatefulDatagramSessionImpl getSession(SocketAddress localAddr, SocketAddress remoteAddr, 
			IoHandler handler, IoFilterChainBuilder chainBuilder) throws Exception {
		synchronized (sessions) {
			LocalSessionsHolder holder = this.sessions.get(localAddr);
			
			if(holder == null) {
				holder = new LocalSessionsHolder(localAddr);
				this.sessions.put(localAddr, holder);
			}
			
			return holder.getSession(remoteAddr, handler, chainBuilder);
		}
	}

	/**
	 * obtain a session for a (local, remote) address pair.
	 * @param localAddr the local address
	 * @param remoteAddr the remote address
	 * @return the session or null no such session exists.
	 * @exception Exception session creation or initialization failed
	 */
	StatefulDatagramSessionImpl getSession(SocketAddress localAddr, SocketAddress remoteAddr) {
		StatefulDatagramSessionImpl session = null;
		
		synchronized (sessions) {
			LocalSessionsHolder holder = this.sessions.get(localAddr);
			
			if(holder != null)
				session = holder.getSession(remoteAddr);
		}
		
		return session;
	}

	
	
	/**
	 * obtain a new session for a (local, remote) address pair.
	 * @param localAddr the local address
	 * @param remoteAddr the remote address
	 * @throws Exception 
	 * @exception Exception session creation or initialization failed
	 */
	public IoSession newSession(SocketAddress localAddr, SocketAddress remoteAddr, IoHandler handler,
			IoFilterChainBuilder chainBuilder) throws Exception {
		synchronized (sessions) {
			LocalSessionsHolder holder = this.sessions.get(localAddr);
			
			if(holder == null) {
				holder = new LocalSessionsHolder(localAddr);
				this.sessions.put(localAddr, holder);
			}
			
			return holder.newSession(remoteAddr, handler, chainBuilder);
		}
	}

	void closeSessions(SocketAddress localAddr, IoHandler handler) {
		synchronized (this.sessions) {
			LocalSessionsHolder holder = this.sessions.get(localAddr);
			
			if(holder != null)
				holder.closeSessions(handler);
		}
	}

	SessionAwareDatagramAcceptorDelegate getDelegate() {
		return delegate;
	}

	public void closeSession(StatefulDatagramSessionImpl impl, IoHandler handler) {
		synchronized (this.sessions) {
			LocalSessionsHolder holder = this.sessions.get(impl.getLocalAddress());
			
			if(holder != null)
				holder.closeSession(impl, handler);
		}
	}

	private class LocalSessionsHolder {
		
		private SocketAddress localAddr;
		private StatefulDatagramSessionImpl nullAddrSession;
		private HashMap<SocketAddress, StatefulDatagramSessionImpl> sessions = 
			new HashMap<SocketAddress, StatefulDatagramSessionImpl>();
		
		private LocalSessionsHolder(SocketAddress localAddr) {
			this.localAddr = localAddr;
		}

		public IoSession newSession(SocketAddress remoteAddr, IoHandler handler, IoFilterChainBuilder chainBuilder) throws Exception {
			if(this.sessions.get(remoteAddr) != null)
				throw new IllegalArgumentException("remote address already bound to session: " + remoteAddr);

			StatefulDatagramSessionImpl session = createSession(remoteAddr, handler, chainBuilder);
			this.sessions.put(remoteAddr, session);
			
			return session;
		}

		private void closeSessions(IoHandler handler) {
			for(SocketAddress addr : this.sessions.keySet()) {
				StatefulDatagramSessionImpl session = this.sessions.get(addr);
				
				try {
					handler.sessionClosed(session);
				} catch(Exception e) {
					try {
						handler.exceptionCaught(session, e);
					} catch(Throwable t) {
						getExceptionMonitor().exceptionCaught(t);
					}
				}
			}
			this.sessions.clear();
		}

		public void closeSession(StatefulDatagramSessionImpl impl, IoHandler handler) {
			if(this.sessions.containsKey(impl.getRemoteAddress())) {
				try {
					handler.sessionClosed(impl);
				} catch(Exception e) {
					try {
						handler.exceptionCaught(impl, e);
					} catch(Throwable t) {
						getExceptionMonitor().exceptionCaught(t);
					}
				}
				this.sessions.remove(impl.getRemoteAddress());
			} else 
				throw new IllegalStateException("session not managed");
		}

		private StatefulDatagramSessionImpl getSession(SocketAddress remoteAddr, IoHandler handler, 
				IoFilterChainBuilder chainBuilder) throws Exception {
			StatefulDatagramSessionImpl session = null;
			
			if(remoteAddr == null) {
				if(this.nullAddrSession == null)
					this.nullAddrSession = createSession(remoteAddr, handler, chainBuilder);
				
				session = this.nullAddrSession;
			} else {
				session = this.sessions.get(remoteAddr);
				
				if(session == null) {
					session = createSession(remoteAddr, handler, chainBuilder);
					this.sessions.put(remoteAddr, session);
				}
			}
			
			return session;
		}

		private StatefulDatagramSessionImpl getSession(SocketAddress remoteAddr) {
			StatefulDatagramSessionImpl session = null;
			
			if(remoteAddr != null) {
				session = this.sessions.get(remoteAddr);
			}
			
			return session;
		}


		private StatefulDatagramSessionImpl createSession(SocketAddress remoteAddr, IoHandler handler, 
				IoFilterChainBuilder chainBuilder) throws Exception {
			StatefulDatagramSessionImpl session = new StatefulDatagramSessionImpl(handler, this.localAddr, remoteAddr,
					StatefulDatagramSessionManager.this);
			
			StatefulDatagramSessionManager.this.getFilterChainBuilder().buildFilterChain(session.getFilterChain());
			if(chainBuilder != null)
				chainBuilder.buildFilterChain(session.getFilterChain());
			
			// fire lifecycle events
			handler.sessionCreated(session);
			handler.sessionOpened(session);

			return session;
		}
	}

}
