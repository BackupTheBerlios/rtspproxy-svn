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

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashMap;

import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoFilterChainBuilder;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.support.BaseIoAcceptor;

import rtspproxy.transport.socket.nio.DatagramAcceptor;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 * 
 */
public class SessionAwareDatagramAcceptorDelegate extends BaseIoAcceptor implements
		IoAcceptor {

	private IoAcceptor wrapper;

	private HashMap<SocketAddress, HandlerInfo> acceptors = new HashMap<SocketAddress, HandlerInfo>();

	/**
	 * create an instance
	 */
	public SessionAwareDatagramAcceptorDelegate(IoAcceptor wrapper) {
		this.wrapper = wrapper;
	}

	public void bind(SocketAddress addr, IoHandler handler,
			IoFilterChainBuilder chainBuilder) throws IOException {
		DatagramAcceptor acceptor = new DatagramAcceptor();
		SessionAwareDatagramHandler sessionHandler = new SessionAwareDatagramHandler(addr, handler, chainBuilder);
		
		acceptor.bind(addr, sessionHandler, null);
		synchronized (acceptors) {
			acceptors.put(addr, new HandlerInfo(acceptor, sessionHandler));
		}
	}

	public void unbind(SocketAddress addr) {
		synchronized (acceptors) {
			HandlerInfo info = acceptors.get(addr);
			
			if(info != null) {
				info.acceptor.unbind(addr);
				info.handler.unbind();
				acceptors.remove(addr);
			}
		}
	}

	private static class HandlerInfo {
		private DatagramAcceptor acceptor;
		private SessionAwareDatagramHandler handler;
		
		private HandlerInfo(DatagramAcceptor acceptor, SessionAwareDatagramHandler handler) {
			this.acceptor = acceptor;
			this.handler = handler;
		}
	}

	@Override
	public IoFilterChainBuilder getFilterChainBuilder() {
		// TODO Auto-generated method stub
		return super.getFilterChainBuilder();
	}

	@Override
	public void setFilterChainBuilder(IoFilterChainBuilder builder) {
		// TODO Auto-generated method stub
		super.setFilterChainBuilder(builder);
	}

	@Override
	public IoSession newSession(SocketAddress arg0, SocketAddress arg1) {
		// TODO Auto-generated method stub
		return super.newSession(arg0, arg1);
	}
}
