/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   Copyright (C) 2005 - Matteo Merli - matteo.merli@gmail.com            *
 *                                                                         *
 ***************************************************************************/

/*
 * $Id$
 * 
 * $URL$
 * 
 */

package server;

import org.apache.mina.protocol.ProtocolCodecFactory;
import org.apache.mina.protocol.ProtocolDecoder;
import org.apache.mina.protocol.ProtocolEncoder;
import org.apache.mina.protocol.ProtocolHandler;
import org.apache.mina.protocol.ProtocolProvider;

import rtspproxy.rtsp.RtspDecoder;
import rtspproxy.rtsp.RtspEncoder;


/**
 * @author mat
 *
 */
public class ServerProvider implements ProtocolProvider
{
	private static ProtocolHandler handler = new Server();
	
	private static ProtocolCodecFactory codecFactory = new ProtocolCodecFactory()
	{

		public ProtocolEncoder newEncoder()
		{
			// Create a new encoder.
			return new RtspEncoder();
		}

		public ProtocolDecoder newDecoder()
		{
			// Create a new decoder.
			return new RtspDecoder();
		}
	};


	/* (non-Javadoc)
	 * @see org.apache.mina.protocol.ProtocolProvider#getCodecFactory()
	 */
	public ProtocolCodecFactory getCodecFactory()
	{
		return codecFactory;
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.protocol.ProtocolProvider#getHandler()
	 */
	public ProtocolHandler getHandler()
	{
		return handler;
	}

}
