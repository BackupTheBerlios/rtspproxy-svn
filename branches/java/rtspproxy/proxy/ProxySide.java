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

import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;

/**
 * Interface that describe the event that must be implemented  
 * by both sides ( Client and Server ) of the proxy.
 */
public interface ProxySide
{

	void onRequestAnnounce( RtspRequest request );

	void onRequestDescribe( RtspRequest request );

	void onRequestGetParam( RtspRequest request );

	void onRequestOptions( RtspRequest request );

	void onRequestPause( RtspRequest request );

	void onRequestPlay( RtspRequest request );

	void onRequestRecord( RtspRequest request );

	void onRequestRedirect( RtspRequest request );

	void onRequestSetup( RtspRequest request );

	void onRequestSetParam( RtspRequest request );

	void onRequestTeardown( RtspRequest request );

	// responses
	void onResponseAnnounce( RtspResponse response );

	void onResponseDescribe( RtspResponse response );

	void onResponseGetParam( RtspResponse response );

	void onResponseOptions( RtspResponse response );

	void onResponsePause( RtspResponse response );

	void onResponsePlay( RtspResponse response );

	void onResponseRecord( RtspResponse response );

	void onResponseRedirect( RtspResponse response );

	void onResponseSetup( RtspResponse response );

	void onResponseSetParam( RtspResponse response );

	void onResponseTeardown( RtspResponse response );

}
