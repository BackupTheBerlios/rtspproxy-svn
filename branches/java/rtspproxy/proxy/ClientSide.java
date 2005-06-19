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

import java.net.Socket;

import rtspproxy.lib.Debug;
import rtspproxy.rtsp.RtspProtocol;
import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;

/**
 * @author mat
 * 
 */
public class ClientSide implements ProxySide
{

	private ProxyConnection parent;
	private RtspProtocol rtspProtocol;

	/**
	 * 
	 */
	public ClientSide( ProxyConnection parent, Socket socket )
	{
		this.parent = parent;
		rtspProtocol = new RtspProtocol( this, socket );
	}

	public void sendRequest( RtspRequest request )
	{
		try {
			rtspProtocol.sendRequest( request );
		} catch ( Exception e ) {
			Debug.write( "ClientSide: Exception during sendResponse()"
					+ e.getStackTrace() );
		}
	}

	public void sendResponse( RtspResponse response )
	{
		try {
			rtspProtocol.sendResponse( response );
		} catch ( Exception e ) {
			Debug.write( "ClientSide: Exception during sendResponse()"
					+ e.getStackTrace() );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestAnnounce(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestAnnounce( RtspRequest request )
	{
		parent.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestDescribe(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestDescribe( RtspRequest request )
	{
		parent.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestGetParam(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestGetParam( RtspRequest request )
	{
		parent.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestOptions(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestOptions( RtspRequest request )
	{
		parent.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestPause(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestPause( RtspRequest request )
	{
		parent.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestPlay(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestPlay( RtspRequest request )
	{
		parent.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestRecord(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestRecord( RtspRequest request )
	{
		parent.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestRedirect(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestRedirect( RtspRequest request )
	{
		parent.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestSetParam(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestSetParam( RtspRequest request )
	{
		parent.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestSetup(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestSetup( RtspRequest request )
	{
		parent.passSetupRequestToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onRequestTeardown(rtspproxy.rtsp.RtspRequest)
	 */
	public void onRequestTeardown( RtspRequest request )
	{
		parent.passToServer( request );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponseAnnounce(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponseAnnounce( RtspResponse response )
	{
		parent.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponseDescribe(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponseDescribe( RtspResponse response )
	{
		parent.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponseGetParam(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponseGetParam( RtspResponse response )
	{
		parent.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponseOptions(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponseOptions( RtspResponse response )
	{
		parent.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponsePause(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponsePause( RtspResponse response )
	{
		parent.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponsePlay(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponsePlay( RtspResponse response )
	{
		parent.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponseRecord(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponseRecord( RtspResponse response )
	{
		parent.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponseRedirect(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponseRedirect( RtspResponse response )
	{
		parent.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponseSetParam(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponseSetParam( RtspResponse response )
	{
		parent.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponseSetup(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponseSetup( RtspResponse response )
	{
		parent.passToServer( response );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rtspproxy.proxy.ProxySide#onResponseTeardown(rtspproxy.rtsp.RtspResponse)
	 */
	public void onResponseTeardown( RtspResponse response )
	{
		parent.passToServer( response );
	}

}
