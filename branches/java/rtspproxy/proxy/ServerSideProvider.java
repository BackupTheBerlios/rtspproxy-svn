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

package rtspproxy.proxy;

import org.apache.mina.protocol.ProtocolCodecFactory;
import org.apache.mina.protocol.ProtocolDecoder;
import org.apache.mina.protocol.ProtocolEncoder;
import org.apache.mina.protocol.ProtocolHandler;
import org.apache.mina.protocol.ProtocolProvider;

import rtspproxy.rtsp.RtspDecoder;
import rtspproxy.rtsp.RtspEncoder;

/**
 * 
 */
public class ServerSideProvider implements ProtocolProvider
{
	private static ProtocolHandler serverSide = new ServerSide();

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

	public ProtocolCodecFactory getCodecFactory()
	{
		return codecFactory;
	}
	public ProtocolHandler getHandler()
	{
		return serverSide;
	}
}
