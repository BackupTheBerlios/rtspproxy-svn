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
package rtspproxy.transport.socket.nio;

import org.apache.mina.common.IoSession;

public interface StatefulDatagramSession extends IoSession {
	/**
	 * set the per-session timeout. If the timeout expires on both up- and downstream
	 * directions without passing messages along, the session gets closed.
	 * @param timeout the timeout value in millisecods
	 */
	public void setSessionTimeout(int timeout);

	/**
	 * get the per-session timeout. If the timeout expires on both up- and downstream
	 * directions without passing messages along, the session gets closed.
	 * @return timeout the timeout value in millisecods
	 */
	public int getSessionTimeout();

}