/*
 *   @(#) $Id: DatagramSession.java 355016 2005-12-08 07:00:30Z trustin $
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

import java.net.SocketException;

import org.apache.mina.common.IoSession;

/**
 * An {@link IoSession} for datagram transport (UDP/IP).
 * 
 * @author The Apache Directory Project (dev@directory.apache.org)
 * @version $Rev: 355016 $, $Date: 2005-12-08 16:00:30 +0900 (Thu, 08 Dec 2005) $
 */
public interface DatagramSession extends IoSession
{
    boolean getReuseAddress() throws SocketException;
    void setReuseAddress( boolean on ) throws SocketException;
    int getTrafficClass() throws SocketException;
    void setTrafficClass( int tc ) throws SocketException;
}