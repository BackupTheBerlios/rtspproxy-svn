/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */
 
/* Caching support is 
 * Copyright (C) 2003  Matteo Merli <matteo.merli@studenti.unipr.it>
 * 
 * $Id$
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <signal.h>
#include <stdarg.h>

#include <iostream>

#include "app.h"
#include "rtspproxy.h"
#include "tranhdr.h"

#include "time_range.h"

#include "dbg.h"

#ifdef HAVE_CONFIG_H
#include "../config.h"
#endif

extern config_t global_config;

/*
 * Rewrite the transporte header to check if it is present the RTP protocol.
 * In this case, other client supported protocols will be hidden to server.
 */
CString CClientCnx::rewrite_transport_header(CString str, CString url)
{
	if ( strstr( url, ".rn" )  ||
	     strstr( url, ".ram" ) ||
	     strstr( url, ".rm" ) ) {
		/* This is a RealMedia stream..
		 * It's better to leave untouched the transport header.
		 */
		dbg("This is a RealMedia stream...\n");
		char *str2 = strdup( str );
		m_pOwner->caching_packets = false;
		return str2;
	}

	str.ToLower();
	char *sub_str = strstr(str.m_sz, "rtp");

	if ( !sub_str ) {
		/*
		 * Client doesn't support RTP..
		 */
		return str;
	}

	if ( !(sub_str - str.m_sz ) ) {
		/*
		 * Ok, in this case RTP is already the first protocol in list,
		 * we don't have to do nothing.
		 */
		 return str;
	}

	/* Now, RTP is the first protocol in list */
	char *str2 = strdup( sub_str );
	return str2;
}


/**************************************
 *
 * CClientCnx class
 *
 **************************************/

CClientCnx::CClientCnx(CRtspProxyApp * pApp, CRtspProxyCnx * pOwner, CTcpSocket * psock):
	m_state( stClosed ),
	m_pOwner( pOwner ),
	m_pprot( NULL ),
	m_pSock( psock ),
	m_use_cache ( false ),
	m_cache_playback ( false ),
	m_app( pApp )
{
	m_pprot = new CRtspProtocol(this);
	m_pprot->Init(psock);
	m_addrClient = psock->GetPeerAddr();
	if (m_addrClient.IsValid()) {
		m_pSock = psock;
		m_state = stConnected;
	}
	m_addrSelf = psock->GetLocalAddr();

	if ( global_config.cache_enable )
		m_segment_list = new CacheSegmentList();

	m_sdp_info_list = new sdp_info_list_t();
	m_cache_play_list = new cache_play_list_t();
}

CClientCnx::~CClientCnx(void)
{
	delete m_sdp_info_list;
	delete m_cache_play_list;
}

void CClientCnx::Close(void)
{
	m_state = stClosed;
	if (m_pprot) {
		delete m_pprot;
		m_pprot = NULL;
	}
}

void CClientCnx::sendRequest(CRtspRequestMsg * pmsg)
{
	if (m_state == stClosed)
		return;
	m_pprot->SendRequest(pmsg);
}

void CClientCnx::sendResponse(CRtspResponseMsg * pmsg)
{
	if (m_state == stClosed)
		return;

	/*! We try to get the SDP description of the stream,
	 *  so that we can obtain informations about it.
	 */
	PBYTE sdp = pmsg->GetBuf();

    if ( global_config.cache_enable && sdp ) {
		dbg("--------------------------\n");
		parseSDP( (char *)sdp );
		dbg("--------------------------\n");

		char *url = strdup( pmsg->GetHdr("Content-Base") );
		url[ strlen(url) - 1 ] = '\0';
		m_app->cache()->sdp()->add( (const char*)url, (const char*)sdp );
		free( url );
	}

	m_pprot->SendResponse(pmsg);
}

void CClientCnx::parseSDP(char *sdp)
{
	u_int8_t stream_count = 0;
	sdp_info_t *sdp_info = NULL;

	char *str = strdup( sdp );
	char *line = strtok( str, "\n" );
	char range[50] = "";

	while ( (line = strtok( NULL, "\n" )) != NULL ) {

		if ( strstr( line, "m=" ) ) {
			sdp_info = (sdp_info_t *)malloc( sizeof(sdp_info_t) );
			sdp_info->range = NULL;
			sdp_info->length = NULL;
			sdp_info->mimetype = NULL;
			sdp_info->payload_type = 0;
			sdp_info->rtp_clock = 0;
			sdp_info->avg_bitrate = 0;
			sdp_info->track_id = stream_count;

			m_sdp_info_list->InsertTail( sdp_info );
			++stream_count;
			dbg("-- New Stream --\n");

		} else if ( strstr( line, "a=range:" ) ) {
			char *s = strstr( line, ":") + 1;
			if ( ! sdp_info ) {
				strncpy( range, s, 50 );
				continue;
			}
			sdp_info->range = strdup( s );
			dbg("Range: %s\n", sdp_info->range );
		}

		if ( ! sdp_info )
			continue;

		if ( strstr( line, "a=length:" ) ) {
			char *s = strstr( line, ":") + 1;
			sdp_info->length = strdup( s );
			dbg("Length: %s\n", sdp_info->length );

		} else if ( strstr( line, "a=rtpmap:" ) ) {
			char *s = strstr( line, ":") + 1;
			char *str = strstr( s, " ");
			str[0] = '\0';
			sdp_info->payload_type =  atoi( s );
			++str;
			sdp_info->rtp_clock = atoi( strstr(str, "/") + 1 );
			dbg("Payload Type: '%d'\n", sdp_info->payload_type );
			dbg("RTP Clock: '%d'\n", sdp_info->rtp_clock );

		} else if ( strstr( line, "a=mimetype:" ) ) {
			char *s = strstr( line, ";") + 1;
			sdp_info->mimetype = strdup( s );
			dbg("MimeType: %s\n", sdp_info->mimetype );

		} else if ( strstr( line, "a=AvgBitRate:" ) ) {
			sdp_info->avg_bitrate =  atoi( strstr( line, ";") + 1 );
			dbg("AvgBitRate: %d\n", sdp_info->avg_bitrate );

		} else if ( strstr( line, "a=control:" ) ) {
			char *s = rindex( line, '=') +1 ;
			sdp_info->track_id = atoi( s );
			dbg("Track Id: %d\n", sdp_info->track_id );
		}
	}

	if ( strlen( range ) > 1 ) {
		sdp_info_list_t::Iterator itr = m_sdp_info_list->Begin();
		while ( itr ) {
			sdp_info = *itr;
			sdp_info->range = strdup( range );
			dbg("Stream %d - range: %s\n", sdp_info->track_id, sdp_info->range );
			++itr;
		}
	}

	free( str );
}

void CClientCnx::sendSetupResponse(CRtspResponseMsg * pmsg)
{
	if (m_state == stClosed)
		return;

	m_pprot->SendResponse(pmsg);
}

void CClientCnx::sendResponse(uint32_t code, uint32_t cseq)
{
	if (m_state == stClosed)
		return;

	CRtspResponseMsg msg;
	msg.SetStatus(code);
	if ( cseq ) {
		char buf[20];
		sprintf(buf, "%d", cseq);
		msg.SetHdr("CSeq", buf);
	}
	sendResponse(&msg);
}

CRtspProtocol *CClientCnx::GetRtspProtocol(void) const
{
	return m_pprot;
}

const CSockAddr & CClientCnx::GetClientAddr(void) const
{
	return m_addrClient;
}

const CSockAddr & CClientCnx::GetSelfAddr(void) const
{
	return m_addrSelf;
}

void CClientCnx::OnError(RtspErr err)
{
	if (m_state == stConnected) {
		m_state = stClosed;
		if (err == RTSPE_CLOSED) {
			m_pOwner->OnClientCnxClosed();
		}
	}
}

/*** Requests ***/

void CClientCnx::OnDescribeRequest(CRtspRequestMsg * pmsg)
{
	if ( !global_config.cache_enable ) {
		m_pOwner->PassToServer(pmsg);
		return;
	}

	// Here we check for a previously saved SDP item.
#warning SDP items not cached
 	char *sdp = m_app->cache()->sdp()->get( pmsg->GetUrl() );
///	if ( sdp == NULL ) {
		/* Description is not saved.. */
		m_pOwner->PassToServer(pmsg);
		return;
///	}

	CRtspResponseMsg *response = new CRtspResponseMsg();
	response->SetStatus( 200 );
	response->SetHdr("CSeq", pmsg->GetHdr("CSeq") );
	response->SetHdr("Content-Type", "application/sdp" );
	char s[200];
	snprintf(s, 10, "%u", strlen( sdp ) );
	response->SetHdr("Content-Length", s );
	snprintf(s, 200, "%s/", pmsg->GetUrl() );
	response->SetHdr( "Content-base", s );
	response->SetHdr( "Via", CString( "RTSP/1.0 rtsp_proxy" ) );
	response->SetBuf( (const uint8_t*)sdp, strlen(sdp) );
	sendResponse( response );
	delete response;
}

void CClientCnx::OnAnnounceRequest(CRtspRequestMsg * pmsg)
{
	dbg("REQUEST ANNOUNCE\n");
	m_pOwner->PassToServer(pmsg);
}

void CClientCnx::OnGetParamRequest(CRtspRequestMsg * pmsg)
{
	dbg("REQUEST GET PARAM\n");
	m_pOwner->PassToServer(pmsg);
}

void CClientCnx::OnSetParamRequest(CRtspRequestMsg * pmsg)
{
	dbg("REQUEST SET PARAM\n");
	m_pOwner->PassToServer(pmsg);
}

void CClientCnx::OnOptionsRequest(CRtspRequestMsg * pmsg)
{
	if ( global_config.cache_enable ) {
		/* If we are playing back from cache, we want to
		 * reduce the OPTIONS set to the set supported by
		 * the proxy.
		 */
		CRtspResponseMsg *response = new CRtspResponseMsg();
		response->SetStatus( 200 );
		response->SetHdr( "CSeq", pmsg->GetHdr("CSeq") );
		response->SetHdr( "Server", "RTSP Proxy Version " VERSION  );
		response->SetHdr( "Public",
			"OPTIONS, DESCRIBE, PLAY, SETUP, TEARDOWN" );
		sendResponse( response );
		delete response;
	} else {
		m_pOwner->PassToServer(pmsg);
    }
}

void CClientCnx::OnPauseRequest(CRtspRequestMsg * pmsg)
{
	dbg("REQUEST PAUSE\n");
	m_pOwner->PassToServer(pmsg);
}

void CClientCnx::OnPlayRequest(CRtspRequestMsg * pmsg)
{
	if ( m_cache_playback ) {

		if ( strstr( pmsg->GetUrl(), "=") ) {
			/* There is a PLAY request for every stream.. */
			uint8_t idx;
			idx = atoi( strstr( pmsg->GetUrl(), "=") +1 );
			cache_play_list_t::Iterator it = m_cache_play_list->Begin();
			while ( it ) {
				CachePlay* cp = (*it);
				uint8_t track_id = cp->get_segment()->getSDPInfo()->track_id;
				if ( track_id == idx ) {
					cp->play( pmsg );
					return;
				}
				++it;
			}

		} else {
			/* There is only 1 PLAY command for all the streams.. */
			cache_play_list_t::Iterator it = m_cache_play_list->Begin();
			while ( it ) {
				CachePlay* cp = (*it);
				if ( it == m_cache_play_list->Begin() ) {
					cache_play_list_t::Iterator it2 = it;
					++it2;
					if ( it2 ) {
						CachePlay* cp2 = (*it2);
						cp->play( pmsg, cp2 );
					} else {
						cp->play( pmsg );
					}
				} else {
					/* Only start playing.. here we don't
					 * send any Play response...
					 */
					cp->get_transport()->start();
				}
				++it;
			}
		}



		return;
	}

	dbg("Client Play Request - Range: '%s'\n", pmsg->GetHdr("Range").to_str() );

	m_pOwner->PassToServer(pmsg);
}

void CClientCnx::OnRecordRequest(CRtspRequestMsg * pmsg)
{
	dbg("REQUEST RECORD\n");
	m_pOwner->PassToServer(pmsg);
}

void CClientCnx::OnRedirectRequest(CRtspRequestMsg * pmsg)
{
	dbg("REQUEST REDIRECT\n");
	m_pOwner->PassToServer(pmsg);
}

void CClientCnx::OnSetupRequest(CRtspRequestMsg * pmsg)
{
	dbg("Setup request for url: '%s'\n", pmsg->GetUrl());
	dbg("Client transport header: '%s'\n", pmsg->GetHdr("Transport").to_str() );

	if ( !global_config.cache_enable ) {
		m_pOwner->PassSetupRequestMsgToServer(pmsg);
		return;
	}

	// Here we check for a previously cached stream.

	if ( m_app->cache()->check( pmsg->GetUrl() ) == CACHE_HIT ) {
		dbg("\n====> CACHE_HIT ! Playback from cached streams...\n");
		/*
		 * The resource is present in cache, ok.. but we MUST check
		 * if there is a cached segment the cover all the requested
		 * stream...
		 * This will be done after every PLAY request.
		 */
		uint8_t idx;
		idx = atoi( strstr( pmsg->GetUrl(), "=") +1 );
		CachePlay *cache_play = new CachePlay(this,
					m_app->cache()->get_item( pmsg->GetUrl() ) );
		m_cache_playback = true;
		cache_play->setup( pmsg );

		m_cache_play_list->InsertTail( cache_play );

	} else {
	  	dbg("\n====> CACHE_MISS.. passing request to the server.\n");

		m_pOwner->caching_packets = true;
		CString tr_hdr = rewrite_transport_header(
			pmsg->GetHdr("Transport"), pmsg->GetUrl() );
		dbg("Rewrited Transport header to be sent to server: '%s'\n",
				(const char *)tr_hdr);
		pmsg->SetHdr("Transport", tr_hdr);

		if ( m_pOwner->caching_packets && strlen(pmsg->GetUrl())>2 ) {
			// Also we want to add the resource to the cache
			CacheSegment *seg = m_app->cache()->add( pmsg->GetUrl() );
			m_segment_list->InsertTail( seg );

			/* We check the id of the stream. */
			uint8_t idx;
			idx = atoi( strstr( pmsg->GetUrl(), "=") +1 );
			printf("\nSTREAM IDX: %u\n", idx );
			sdp_info_t *sdp = getSDPInfo( idx );
			seg->setSDPInfo( sdp );
		}

		m_pOwner->PassSetupRequestMsgToServer( pmsg );
	}
}

void CClientCnx::OnTeardownRequest(CRtspRequestMsg * pmsg)
{
	if ( m_cache_playback ) {
		cache_play_list_t::Iterator it = m_cache_play_list->Begin();
		for ( ; it; it++ ) {
			CachePlay* cp = (*it);
			if ( ! cp )
				continue;
			cp->teardown( pmsg );
		}
		return;
	}


	dbg("Teardown request\n");
	m_pOwner->PassToServer(pmsg);

	CSessionHdr sessionHdr(pmsg->GetHdr("Session"));
	m_pOwner->DeleteSessionByClientSessionID(sessionHdr.GetSessionID());

	/*! We close the segments.. */
    if ( ! m_segment_list )
    	return;

    if ( global_config.cache_enable ) {
        CacheSegmentList::Iterator it = m_segment_list->Begin();
        for ( ; it ; it++ ) {
            if (*it)
                (*it)->close();
        }
  }
}

void CClientCnx::OnExtensionRequest(CRtspRequestMsg * pmsg)
{
	dbg("REQUEST EXTENSION\n");
	m_pOwner->PassToServer(pmsg);
}

/*** Responses ***/

void CClientCnx::OnDescribeResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToServer(pmsg);
}

void CClientCnx::OnAnnounceResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToServer(pmsg);
}

void CClientCnx::OnGetParamResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToServer(pmsg);
}

void CClientCnx::OnSetParamResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToServer(pmsg);
}

void CClientCnx::OnOptionsResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToServer(pmsg);
}

void CClientCnx::OnPauseResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToServer(pmsg);
}

void CClientCnx::OnPlayResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToServer(pmsg);
}

void CClientCnx::OnRecordResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToServer(pmsg);
}

void CClientCnx::OnRedirectResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToServer(pmsg);
}

void CClientCnx::OnSetupResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToServer(pmsg);
}

void CClientCnx::OnTeardownResponse(CRtspResponseMsg * pmsg)
{
	dbg("Teardown response\n");
	m_pOwner->PassToServer(pmsg);
}

void CClientCnx::OnExtensionResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToServer(pmsg);
}


CacheSegment * CClientCnx::get_cache_segment(uint16_t idx)
{
	if ( !m_segment_list ) {
		dbg("!!!!! m_segment_list unitialized !!!!\n");
		return NULL;
	}

	// dbg("m_segment_list contiene %d elementi.\n", m_segment_list->GetCount() );

	uint16_t i = idx;
	CacheSegmentList::Iterator it = m_segment_list->Begin();
	while ( i ) {
		++it;
		--i;
	}

	if ( !(*it) ) {
		dbg("CClientCnx::get_cache_segment() Warning... cache segment is NULL...\n");
	}

	return (*it);
}

sdp_info_t *CClientCnx::getSDPInfo(u_int8_t idx)
{
	sdp_info_list_t::Iterator itr = m_sdp_info_list->Begin();
	sdp_info_t *sdp;

	while ( itr ) {
		sdp = *itr;
		if ( sdp->track_id == idx ) {
			return sdp;
		}
		++itr;
	}

	/* We create an empty SDP description. */
	dbg("warning: empty SDP description...\n");
	sdp = (sdp_info_t *) malloc( sizeof(sdp_info_t) );
	(*sdp).range = NULL;
	(*sdp).length = NULL;
	(*sdp).mimetype = NULL;
	(*sdp).payload_type = 0;
	(*sdp).rtp_clock = 0;
	(*sdp).avg_bitrate = 0;
	(*sdp).track_id = 0;
	return sdp;
}

/**************************************
 *
 * CServerCnx class
 *
 **************************************/

CServerCnx::CServerCnx(CRtspProxyCnx * pOwner, CString strHost, UINT16 uPort):
	m_state(stClosed),
	m_pOwner(pOwner),
	m_pprot(NULL),
	m_pSock(NULL),
	m_strHost(strHost),
	m_uPort(uPort)
{
	m_pResolver = CResolver::GetResolver();
}

CServerCnx::~CServerCnx(void)
{

}

void CServerCnx::Close(void)
{
	m_state = stClosed;

	if ( m_pResolver ) {
		//delete m_pResolver;
		m_pResolver = NULL;
	}

	if ( m_pprot ) {
		delete m_pprot;
		m_pprot = NULL;
	}
	// we havn't passed it to m_pprot
	if ( m_pSock ) {
		delete m_pSock;
		m_pSock = NULL;
	}

	while ( !m_RequestMsgQueue.IsEmpty() ) {
		CRtspRequestMsg *pmsg = m_RequestMsgQueue.RemoveHead();
		delete pmsg;
	}
}

void CServerCnx::sendRequest(CRtspRequestMsg * pmsg)
{
	if (m_state == stClosed) {
		return;
	}
	m_pprot->SendRequest(pmsg);
}

void CServerCnx::sendResponse(CRtspResponseMsg * pmsg)
{
	if (m_state == stClosed)
		return;
	m_pprot->SendResponse(pmsg);
}

void CServerCnx::sendResponse(UINT code, UINT cseq)
{
	if (m_state == stClosed)
		return;

	CRtspResponseMsg msg;
	msg.SetStatus(code);
	if (cseq) {
		msg.SetHdr("CSeq", cseq);
	}
	sendResponse(&msg);
}

CRtspProtocol *CServerCnx::GetRtspProtocol(void) const
{
	return m_pprot;
}

const CSockAddr & CServerCnx::GetServerAddr(void) const
{
	return m_addrServer;
}

const CString & CServerCnx::GetHostName(void) const
{
	return m_strHost;
}

UINT16 CServerCnx::GetPort(void) const
{
	return m_uPort;
}

void CServerCnx::AddRtspMsgToQueue(CRtspRequestMsg * pmsg)
{
	m_RequestMsgQueue.InsertTail(new CRtspRequestMsg(*pmsg));
}

void CServerCnx::ConnectToServer(CPCHAR szHost, UINT16 port)
{
	bool result;
	
	/** Hierarchical proxies support */
	if ( global_config.hierarchical_proxy ) {
		dbg("Using further RTSP proxy level.\n");
		m_addr.SetPort( (UINT16) global_config.hierarchical_proxy_port );
		result = m_pResolver->GetHost(this,  CString( global_config.hierarchical_proxy_addr ) );
		if ( !result )
			dbg("Error connecting to proxy.\n");
		return;
	}
	
	/** Normal mode */	
	m_addr.SetPort(port);
	result = m_pResolver->GetHost(this, szHost);
	if ( !result )
		dbg("Error connecting to RTSP server.\n");
}

void CServerCnx::OnError(RtspErr err)
{
	if (m_state == stConnected) {
		m_state = stClosed;
		dbg("CServerCnx::OnError: err=%i\n", err);
		if (err == RTSPE_CLOSED) {
			Close();
			m_pOwner->OnServerCnxClosed(this);
		}
	}
}

/*** Requests ***/

void CServerCnx::OnDescribeRequest(CRtspRequestMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnAnnounceRequest(CRtspRequestMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnGetParamRequest(CRtspRequestMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnSetParamRequest(CRtspRequestMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnOptionsRequest(CRtspRequestMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnPauseRequest(CRtspRequestMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnPlayRequest(CRtspRequestMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnRecordRequest(CRtspRequestMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnRedirectRequest(CRtspRequestMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnSetupRequest(CRtspRequestMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnTeardownRequest(CRtspRequestMsg * pmsg)
{
	m_pOwner->DeleteSessionByServerSessionID(pmsg->GetHdr("Session"),
						 GetHostName());
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnExtensionRequest(CRtspRequestMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

/*** Responses ***/

void CServerCnx::OnDescribeResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnAnnounceResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnGetParamResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnSetParamResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnOptionsResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnPauseResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnPlayResponse(CRtspResponseMsg * pmsg)
{
	dbg("We are playing!!!\n");
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnRecordResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnRedirectResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnSetupResponse(CRtspResponseMsg * pmsg)
{
	CString strSess = pmsg->GetHdr("Session");
	CString strTrans = pmsg->GetHdr("Transport");

	dbg("Setup response: \n\tserver Session = '%s'\n\tTransport = '%s'\n",
	    (CPCHAR) strSess, (CPCHAR) strTrans);
	if ( global_config.cache_enable )
		m_pOwner->set_transport( strTrans.to_str() );

	m_pOwner->PassSetupResponseToClient(pmsg, this);
}

void CServerCnx::OnTeardownResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::OnExtensionResponse(CRtspResponseMsg * pmsg)
{
	m_pOwner->PassToClient(pmsg, this);
}

void CServerCnx::GetHostDone(int err, const CString & strQuery,
			     in_addr addrResult)
{
	if (err) {
		SendClientConnectionError();
		return;
	}

	m_addr.SetHost(addrResult);
	m_pSock = new CTcpSocket(this);
	m_pSock->Connect(m_addr);
}

void CServerCnx::GetHostDone(int err, in_addr addrQuery,
			     const CString & strResult)
{
	assert(false);
}

void CServerCnx::OnConnectDone(int err)
{
	if (err) {
		delete m_pSock;
		m_pSock = NULL;
		SendClientConnectionError();
		return;
	}

	m_pprot = new CRtspProtocol(this);
	m_pprot->Init(m_pSock);
	m_addrServer = m_pSock->GetPeerAddr();
	if (m_addrServer.IsValid()) {
		m_state = stConnected;
	}

	while (!m_RequestMsgQueue.IsEmpty()) {
		CRtspRequestMsg *pmsg = m_RequestMsgQueue.RemoveHead();
		sendRequest(pmsg);
		delete pmsg;
	}
	m_pSock = NULL;		//we don't own it any more
}

void CServerCnx::OnReadReady(void)
{
	assert( false );
}

void CServerCnx::OnWriteReady(void)
{
	assert( false );
}

void CServerCnx::OnExceptReady(void)
{
	assert( false );
}

void CServerCnx::OnClosed(void)
{
}

void CServerCnx::SendClientConnectionError(void)
{
	int cseq = 0;
	if (!m_RequestMsgQueue.IsEmpty()) {
		CRtspRequestMsg *pmsg = m_RequestMsgQueue.RemoveHead();
		cseq = atoi(pmsg->GetHdr("CSeq"));
		delete pmsg;
	}
	dbg("warning: Bad getaway ..... \n");
	m_pOwner->OnServerConnectionError(502, cseq);
	Close();
}

/**************************************
 *
 * CSeqPair class
 *
 **************************************/
CCSeqPair::CCSeqPair(const CString & cseqToClient,
		     const CString & cseqToServer,
		     const CString & strHostName,
		     UINT16 uPort):
	m_cseqToClient( cseqToClient ),
	m_cseqToServer( cseqToServer ),
	m_strHost( strHostName ),
	m_uPort( uPort )
{
	// Empty
}

CCSeqPair::~CCSeqPair(void)
{
	// Empty
}


/**************************************
 *
 * CRtspProxyCnx class
 *
 **************************************/

CRtspProxyCnx::CRtspProxyCnx(CRtspProxyApp * pOwner, CTcpSocket * psock, CPCHAR viaHdrValue):
	caching_packets( false ),
	m_pClientCnx(NULL),
	m_clientChannel(0),
	m_pOwner(pOwner),
	m_viaHdrValue(viaHdrValue),
	m_cseqToClient(1),
	m_sessionIndex(0)

{
	m_pClientCnx = new CClientCnx(pOwner, this, psock);
}

CRtspProxyCnx::~CRtspProxyCnx(void)
{
	// Empty
}

void CRtspProxyCnx::PassToClient(CRtspRequestMsg * pmsg,
				 CServerCnx * pServerCnx)
{
	assert(pmsg);
	assert(m_pClientCnx);

	CSessionHdr sessionHdr(pmsg->GetHdr("Session"));
	CString strServerSessionID = sessionHdr.GetSessionID();
	if (!strServerSessionID.IsEmpty()) {
		CString strClientSessionID =
		    FindClientSessionID(strServerSessionID,
					pServerCnx->GetHostName());
		sessionHdr.SetSessionID(strClientSessionID);
		pmsg->SetHdr("Session", sessionHdr.GetSessionHdrString());
	}

	if (!SetViaHdr(pmsg)) {
		// if we got a loop
		dbg("warning: Bad gataway 2 loop.. \n");
		m_pClientCnx->sendResponse(502,
					   atoi(pmsg->GetHdr("CSeq")));
		return;
	}

	CString strCSeq = pmsg->GetHdr("CSeq");
	char buf[20];
	sprintf(buf, "%u", m_cseqToClient++);

	CCSeqPair *pPair =
	    new CCSeqPair(buf, strCSeq, pServerCnx->GetHostName(),
			  pServerCnx->GetPort());
	m_listCCSeqPairList.InsertTail(pPair);

	pmsg->SetHdr("CSeq", buf);
	m_pClientCnx->sendRequest(pmsg);

}

void CRtspProxyCnx::PassToClient(CRtspResponseMsg * pmsg,
				 CServerCnx * pServerCnx)
{
	assert(pmsg);
	assert(m_pClientCnx);

	CSessionHdr sessionHdr(pmsg->GetHdr("Session"));
	CString strServerSessionID = sessionHdr.GetSessionID();
	if (!strServerSessionID.IsEmpty()) {
		CString strClientSessionID =
		    FindClientSessionID(strServerSessionID,
					pServerCnx->GetHostName());
		sessionHdr.SetSessionID(strClientSessionID);
		pmsg->SetHdr("Session", sessionHdr.GetSessionHdrString());
	}

	if ( !SetViaHdr(pmsg) ) {
		// if we got a loop
		dbg("warning: Bad gataway 3.. \n");
		m_pClientCnx->sendResponse(502, atoi(pmsg->GetHdr("CSeq")));
		return;
	}

	m_pClientCnx->sendResponse(pmsg);
}

void CRtspProxyCnx::PassToServer(CRtspRequestMsg * pmsg)
{
	assert(pmsg);

	CServerCnx *pServerCnx;
	CUrl url(pmsg->GetUrl());
	CString strHost = url.GetHost();
	UINT16 uPort = url.GetPort();
	CSessionHdr sessionHdr(pmsg->GetHdr("Session"));
	CString strClientSessionID = sessionHdr.GetSessionID();
	CString strProxyRequire = pmsg->GetHdr("Proxy-Require");

	if (!strClientSessionID.IsEmpty()) {
		CString strServerSessionID =
		    FindServerSessionID(strClientSessionID);
		sessionHdr.SetSessionID(strServerSessionID);
		pmsg->SetHdr("Session", sessionHdr.GetSessionHdrString());
	}

	if (!url.IsValid()) {
		m_pClientCnx->sendResponse(451,
					   atoi(pmsg->GetHdr("CSeq")));
		return;
	}

	if (!SetViaHdr(pmsg)) {
		/* Here the proxy returns a 'Bad Gateway' error response. */
		dbg("warning: Bad gataway.. \n");
		m_pClientCnx->sendResponse(502,atoi(pmsg->GetHdr("CSeq")));
		return;
	}

	if (!strProxyRequire.IsEmpty()) {
		// currently we don't support any Proxy-Require features
		CRtspResponseMsg msg;

		msg.SetStatus(551);
		msg.SetHdr("CSeq", pmsg->GetHdr("CSeq"));
		msg.SetHdr("Unsupported", strProxyRequire);
		m_pClientCnx->sendResponse(&msg);
		return;
	}

	pServerCnx = FindServerCnx(strHost, uPort);
	if (pServerCnx) {
		pServerCnx->sendRequest(pmsg);
	} else {
		pServerCnx = new CServerCnx(this, strHost, uPort);
		pServerCnx->AddRtspMsgToQueue(pmsg);
		pServerCnx->ConnectToServer(strHost, uPort);

		m_listServerCnx.InsertTail(pServerCnx);
	}
}

void CRtspProxyCnx::PassToServer(CRtspResponseMsg * pmsg)
{
	assert(pmsg);

	CSessionHdr sessionHdr(pmsg->GetHdr("Session"));
	CString strClientSessionID = sessionHdr.GetSessionID();

	if (!strClientSessionID.IsEmpty()) {
		CString strServerSessionID =
		    FindServerSessionID(strClientSessionID);
		sessionHdr.SetSessionID(strServerSessionID);
		pmsg->SetHdr("Session", sessionHdr.GetSessionHdrString());
	}

	if (!SetViaHdr(pmsg)) {
		// if we got a loop
		dbg("warning: Bad gataway 4.. \n");
		m_pClientCnx->sendResponse(502,
					   atoi(pmsg->GetHdr("CSeq")));
		return;
	}

	CString strCSeq = pmsg->GetHdr("CSeq");
	CCseqPairList::Iterator itr(m_listCCSeqPairList.Begin());
	while (itr) {
		CCSeqPair *pPair = *itr;
		if (pPair->m_cseqToClient == strCSeq) {
			CServerCnx *pServerCnx =
			    FindServerCnx(pPair->m_strHost,
					  pPair->m_uPort);
			if (pServerCnx) {
				pmsg->SetHdr("CSeq",
					     pPair->m_cseqToServer);
				pServerCnx->sendResponse(pmsg);
			}

			m_listCCSeqPairList.Remove(itr);
			delete pPair;
			return;
		}
		itr++;
	}
}

void CRtspProxyCnx::PassSetupRequestMsgToServer(CRtspRequestMsg * pmsg)
{
	assert(pmsg);
	UINT16 clientPort, proxyToServerPort;
	int nPorts;
	bool bOldSession = false;
	bool bReuseTunnel = false;
	bool bSessionID = false;
	CString strCSeq = pmsg->GetHdr("CSeq");
	CString strTransport = pmsg->GetHdr("Transport");
	CSessionHdr sessionHdr(pmsg->GetHdr("Session"));
	CString strSessionID = sessionHdr.GetSessionID();
	CString strBlocksize = pmsg->GetHdr("Blocksize");
	CRequestTransportHdr rqtHdr(strTransport);

	CRtspProxySession *pSession = NULL;
	CProxyDataTunnel *pTunnel = NULL;

	rqtHdr.GetBasePort(&clientPort, &nPorts);
	bSessionID = !strSessionID.IsEmpty();
	
	// if it is tcp interleaved, we will delay the build up of the tunnel until
	// we get setup response
	if (!rqtHdr.IsInterleaved() && nPorts != 0) {
		CRtspProxySessionList::Iterator itr(m_listRtspProxySession.Begin());
		while (itr) {
			pSession = *itr;
			if ( bSessionID &&
			     !strcmp(pSession->GetClientSessionID(), strSessionID)) {
				bOldSession = true;
				break;
			}
			itr++;
		}

		itr = m_listRtspProxySession.Begin();
		while (itr) {
			pTunnel = (*itr)->FindTunnelByClientPort(clientPort);
			if (pTunnel) {
				bReuseTunnel = true;
				break;
			}
			itr++;
		}

		if (!bReuseTunnel) {
			printf("Creating new tunnel.. for '%s'\n\n", (CPCHAR)pmsg->GetUrl() );
			pTunnel = new CProxyDataTunnel();
			pTunnel->m_url = CString( pmsg->GetUrl() );

			if (!pTunnel->Init(nPorts)) {
				// we are running out of file descriptor
				m_pClientCnx->sendResponse(503, atoi(pmsg->GetHdr("CSeq")));
				dbg(" Tunnel initialization failed\n");
				delete pTunnel;
				return;
			}
			pTunnel->SetClientPort(clientPort);
		}

		if (!bOldSession) {
			pSession = new CRtspProxySession();

			pSession->SetSetupCSeq(strCSeq);
			m_listRtspProxySession.InsertTail(pSession);
		}
		// in case of oldSession and resueTunnel, we do nothing,
		// otherwise we link the session and the tunnel
		if (!(bOldSession && bReuseTunnel)) {
			pTunnel->AddRef();
			pSession->AddTunnel(pTunnel);
		}

		proxyToServerPort = pTunnel->GetProxyToServerPort();
		rqtHdr.SetPort(proxyToServerPort);
		pmsg->SetHdr("Transport", rqtHdr.GetHdrString());
		
		printf(" La porta del client è : %u\n proxytoServer: %u\nQuella riscritta da mandare al server: '%s'\n\n", 
		       clientPort, proxyToServerPort, (CPCHAR)pmsg->GetHdr( "Transport" ) );

		//we want to set the udp packet size to MAX_UDP_LEN
		int nBlocksize = MAX_UDP_LEN - 0x80;	//exclude ip, udp and rtp headers.
		char buf[20];
		if (!strBlocksize.IsEmpty()) {
			int nClientBlocksize = atoi(strBlocksize);
			if (nBlocksize > nClientBlocksize) {
				nBlocksize = nClientBlocksize;
			}
		}
		sprintf(buf, "%d", nBlocksize);
		pmsg->SetHdr("Blocksize", buf);
	}
	PassToServer(pmsg);
}

void CRtspProxyCnx::PassSetupResponseToClient(CRtspResponseMsg * pmsg,
					      CServerCnx * pServerCnx)
{
	UINT16 serverPort, proxyToServerPort;
	int nPorts;
	bool bOldSession = false;
	CString strTran = pmsg->GetHdr("Transport");
	CString strCSeq = pmsg->GetHdr("CSeq");
	CSessionHdr sessionHdr(pmsg->GetHdr("Session"));
	CString strSessionID = sessionHdr.GetSessionID();
	CSingleTransportHdr rtHdr( strTran );
	bool bInterleaved = rtHdr.IsInterleaved();

	CRtspProxySession *pSession = NULL;
	CProxyDataTunnel *pTunnel = NULL;

	rtHdr.GetServerBasePort(&serverPort, &nPorts);
	rtHdr.GetClientBasePort(&proxyToServerPort, &nPorts);
	
	printf("TRANSPORT STRING: '%s'\n", (CPCHAR)strTran);
	printf("PROXY TO SERVER PORT: %u\n", proxyToServerPort);

	if (nPorts != 0) {
		CRtspProxySessionList::Iterator itrs(m_listRtspProxySession.Begin());
		int i = 0;
		while (itrs) {
			pSession = *itrs;
			
			printf("PASS: %u\n", i++);

			if (pSession->GetServerSessionID() == strSessionID
			    && pSession->GetHost() == pServerCnx->GetHostName()) {
				bOldSession = true;
				pTunnel = pSession->FindTunnelByProxyPort( proxyToServerPort );
				
				// FIXME: CACHE
				/*
					When we add a cache segment here, it should be a secondary
					cache segment.
				 */
				if ( caching_packets ) {
					pTunnel->set_cache_segment( get_client_cnx()->get_cache_segment( 1 ) );
					get_client_cnx()->get_cache_segment( 1 )->set_transport_type( m_transport_type );
					// dbg("1 get_client_cnx()->get_cache_segment(1) : 0x%x\n",
					//   get_client_cnx()->get_cache_segment(1) );
				}
				break;
			} 

			if ( !strcmp( pSession->GetSetupCSeq(), strCSeq ) ) {
			  
				pSession->SetSessionID(strSessionID, pServerCnx->GetHostName(), m_sessionIndex++);
				pTunnel = pSession->FindTunnelByProxyPort( proxyToServerPort );
				
				if ( caching_packets ) {
				  	printf("\n\n\n SETTING CACHE SEGMENT \n\n\n");
					pTunnel->set_cache_segment( get_client_cnx()->get_cache_segment(0) );
					get_client_cnx()->get_cache_segment(0)->set_transport_type( m_transport_type );
					// dbg("2 get_client_cnx()->get_cache_segment(0) : 0x%x\n",
					//  get_client_cnx()->get_cache_segment(0) );
				}
				break;
			}

			itrs++;
		}

		if (bInterleaved && !bOldSession) {
			pSession = new CRtspProxySession;
			pSession->SetSetupCSeq(strCSeq);
			pSession->SetSessionID(strSessionID,
					       pServerCnx->GetHostName(),
					       m_sessionIndex++);
			m_listRtspProxySession.InsertTail(pSession);
		}
		// we create the interleaved tunnel in setup response
		if (bInterleaved) {
			//////// FIXME - Cache
			 if ( caching_packets ) {
				pTunnel = new CProxyDataTunnel( get_client_cnx()->get_cache_segment(0) );
				dbg("INTERLEAVED get_client_cnx()->get_cache_segment(0) \n"
					/* , sget_client_cnx()->get_cache_segment(0) */ );
			} else 
				pTunnel = new CProxyDataTunnel();

			pTunnel->Init(m_pClientCnx->GetRtspProtocol(),
				      pServerCnx->GetRtspProtocol(),
				      nPorts, serverPort, m_clientChannel);
			m_clientChannel += nPorts;
			pTunnel->AddRef();
			pSession->AddTunnel(pTunnel);
		}
		//for udp, the tunnel should be created in setup request msg
		if (!pTunnel) {
			// something is wrong
			m_pClientCnx->sendResponse(500, atoi(pmsg->GetHdr("CSeq")));
			return;
		}
		// we create the interleaved session in setup response
		if (!pTunnel->IsSetup()) {
			pTunnel->SetServerPort(serverPort);
			pTunnel->SetClientAddr(m_pClientCnx->GetClientAddr());
			pTunnel->SetServerAddr(pServerCnx->GetServerAddr());

			//we got all the info, connect them
			pTunnel->SetupTunnel();
		}

		rtHdr.SetServerBasePort( pTunnel->GetProxyToClientPort() );
		rtHdr.SetClientBasePort( pTunnel->GetClientPort() );
		rtHdr.SetSourceAddr( m_pClientCnx->GetSelfAddr() );
	}

	pmsg->SetHdr("Transport", rtHdr.GetHdrString());
	PassToClient(pmsg, pServerCnx);
}

void CRtspProxyCnx::
DeleteSessionByClientSessionID(const CString & strClientSessionID)
{
	CRtspProxySession *pSession = NULL;
	CRtspProxySessionList::Iterator itrs(m_listRtspProxySession.
					     Begin());

	while (itrs) {
		pSession = *itrs;
		if (pSession->GetClientSessionID() == strClientSessionID) {
			pSession->ReleaseAllTunnels();
			m_listRtspProxySession.Remove(itrs);
			delete pSession;
			break;
		}

		itrs++;
	}
}

void CRtspProxyCnx::
DeleteSessionByServerSessionID(const CString & strServerSessionID,
			       const CString & strHost)
{
	CRtspProxySession *pSession = NULL;
	CRtspProxySessionList::Iterator itrs(m_listRtspProxySession.
					     Begin());
	while (itrs) {
		pSession = *itrs;
		if (pSession->GetServerSessionID() == strServerSessionID &&
		    pSession->GetHost() == strHost) {
			pSession->ReleaseAllTunnels();
			m_listRtspProxySession.Remove(itrs);
			delete pSession;
			break;
		}

		itrs++;
	}
}

CString
CRtspProxyCnx::FindClientSessionID(const CString & strServerSessionID,
				   const CString & strHost)
{
	CRtspProxySession *pSession = NULL;
	CRtspProxySessionList::Iterator itrs(m_listRtspProxySession.
					     Begin());

	while (itrs) {
		pSession = *itrs;
		if (pSession->GetServerSessionID() == strServerSessionID &&
		    pSession->GetHost() == strHost) {
			return pSession->GetClientSessionID();
		}

		itrs++;
	}
	return "";
}

CString CRtspProxyCnx::FindServerSessionID(const CString & strClientSessionID)
{
	CRtspProxySession *pSession = NULL;
	CRtspProxySessionList::Iterator itrs(m_listRtspProxySession.
					     Begin());

	while (itrs) {
		pSession = *itrs;
		if (pSession->GetClientSessionID() == strClientSessionID) {
			return pSession->GetServerSessionID();
		}

		itrs++;
	}
	return "";
}

CServerCnx *CRtspProxyCnx::FindServerCnx(const CString & strHost,
					 UINT16 uHost)
{
	CServerCnxList::Iterator itr(m_listServerCnx.Begin());
	while (itr) {
		CServerCnx *pServerCnx = *itr;
		if (pServerCnx->GetHostName() == strHost
		    && pServerCnx->GetPort() == uHost) {
			return pServerCnx;
		}
		itr++;
	}
	return NULL;
}

void CRtspProxyCnx::OnServerConnectionError(UINT code, UINT cseq)
{
	m_pClientCnx->sendResponse(code, cseq);
}

void CRtspProxyCnx::OnClientCnxClosed(void)
{
	while (!m_listServerCnx.IsEmpty()) {
		CServerCnx *pServerCnx = m_listServerCnx.RemoveHead();
		pServerCnx->Close();
		delete pServerCnx;
	}

	while (!m_listRtspProxySession.IsEmpty()) {
		CRtspProxySession *pSession =
		    m_listRtspProxySession.RemoveHead();
		pSession->ReleaseAllTunnels();
		delete pSession;
	}

	while (!m_listCCSeqPairList.IsEmpty()) {
		CCSeqPair *pPair = m_listCCSeqPairList.RemoveHead();
		delete pPair;
	}

	m_pClientCnx->Close();
	delete m_pClientCnx;
	m_pOwner->DeleteProxyCnx(this);
}

void CRtspProxyCnx::OnServerCnxClosed(CServerCnx * pServerCnx)
{
	CServerCnxList::Iterator itr(m_listServerCnx.Begin());
	while (itr) {
		CServerCnx *pSrvCnx = *itr;
		if (pServerCnx == pSrvCnx) {
			// remove all server request msg cseq pair related to this server
			CCseqPairList::Iterator itrp(m_listCCSeqPairList.
						     Begin());
			while (itrp) {
				CCSeqPair *pPair = *itrp;
				if (pPair->m_strHost ==
				    pServerCnx->GetHostName()
				    && pPair->m_uPort ==
				    pServerCnx->GetPort()) {
					m_listCCSeqPairList.Remove(itrp);
					delete pPair;
					break;
				}
				itrp++;
			}

			m_listServerCnx.Remove(itr);
			delete pSrvCnx;
			break;
		}
		itr++;
	}
}

bool CRtspProxyCnx::SetViaHdr(CRtspMsg * pMsg)
{
	CString strValue = pMsg->GetHdr("Via");

	if (strValue.IsEmpty()) {
		strValue = m_viaHdrValue;
	} else {
		if (strstr(strValue, m_viaHdrValue)) {
			// a loop here, we need to inform the client
			return false;
		}
		strValue.Append(", ");
		strValue.Append(m_viaHdrValue);
	}
	pMsg->SetHdr("Via", strValue);
	return true;
}

void CRtspProxyCnx::set_transport(const char *str)
{
	char *str2 = strdup( str );
	char *s;
	int idx;

	s = strtok(str2, ",");
	while (s != NULL) {
		idx = index(s, ';') - s;
		s[idx] = '\0';
		dbg("\t%s\n", s);

		if ( ! strcmp(str, "rtp/avp") ) {
			m_transport_type = TRANSPORT_RTP_UDP;
			return;
		}
		if ( ! strcmp(str, "rtp/avp/tcp") ) {
			m_transport_type = TRANSPORT_RTP_TCP;
			return;
		}
		if ( ! strcmp(str, "x-real-rdt/udp") ) {
			m_transport_type = TRANSPORT_RDT_UDP;
			return;
		}
		if ( ! strcmp(str, "x-real-rdt/tcp") ) {
			m_transport_type = TRANSPORT_RDT_TCP;
			return;
		}

		s = strtok(0 , ",");
	}

	m_transport_type = TRANSPORT_UNKNOWN;
}

/**************************************
 *
 * CRtspProxyApp class
 *
 **************************************/

CRtspProxyApp::CRtspProxyApp(int argc, char **argv) :
	CApp( argc, argv ),
        m_sock( this ),
        m_port( RTSP_DEFAULT_PORT ),
        m_use_cache( false )

{
}

CRtspProxyApp::~CRtspProxyApp(void)
{
	// Empty
}

bool CRtspProxyApp::Init(void)
{
	if (!CApp::Init())
		return false;

	CSockAddr addr(CInetAddr::Any(), m_port);
	if (!m_sock.Listen(addr)) {
		fprintf(stderr, "Port %d not available. \n", m_port);
		return false;
	}
	dbg("Listening on port %hu\n", m_port);

	if ( global_config.deamon_mode )
		Daemonize();

	addr = m_sock.GetLocalAddr();
	sprintf(m_viaHdrValue, "RTSP/1.0 %lx",
		addr.GetHost().s_addr ^ time(NULL) ^ rand());

	return true;
}

void CRtspProxyApp::UseCache(bool val)
{
	m_use_cache = val;
	global_config.cache_enable = val;

	if ( val )
		m_cache = new Cache();
}

bool CRtspProxyApp::UseCache()
{
	if ( m_use_cache && !m_cache) {
		dbg("Error: cache object unitialized!\n");
		exit(-1);
	}

	return m_use_cache;
}

int CRtspProxyApp::Exit(void)
{
	return 0;
}

void CRtspProxyApp::OnConnection(CTcpSocket * psock)
{
	dbg("New client connection\n");
	CRtspProxyCnx *pCnx =
	    new CRtspProxyCnx(this, psock, m_viaHdrValue);
	m_listProxyCnx.InsertTail(pCnx);
}

void CRtspProxyApp::OnClosed(void)
{
	while ( !m_listProxyCnx.IsEmpty() ) {
		CRtspProxyCnx *pCnx = m_listProxyCnx.RemoveHead();
		//pCnx->OnClosed();
		delete pCnx;
	}
}

void CRtspProxyApp::SetPort(UINT16 port)
{
	m_port = port;
}

void CRtspProxyApp::DeleteProxyCnx(CRtspProxyCnx * proxyCnx)
{
	CRtspProxyCnxList::Iterator itr(m_listProxyCnx.Begin());
	while (itr) {
		CRtspProxyCnx *pCnx = *itr;
		if (pCnx == proxyCnx) {
			m_listProxyCnx.Remove(itr);
			delete proxyCnx;
			return;
		}
		itr++;
	}
}


/** LOG **
 *
 * $Log: rtspproxy.cpp,v $
 * Revision 1.3  2003/11/17 16:14:16  mat
 * make-up
 *
 *
 */

