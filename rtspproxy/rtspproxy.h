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

#ifndef RTSPPROXY_H
#define RTSPPROXY_H

#include <types.h>

#include "sock.h"
#include "resolver.h"
#include "rtspprot.h"
#include "proxytran.h"
#include "url.h"
#include "proxysession.h"

#include "app.h"
#include "cache.h"
#include "cache_play.h"
#include "cache_segment.h"
#include "config_parser.h"

class CRtspProxyCnx;
class CRtspProxyApp;

#define TRANSPORT_RTP     0x80
#define TRANSPORT_RTP_UDP 0x81
#define TRANSPORT_RTP_TCP 0x82
#define TRANSPORT_RDT     0x40
#define TRANSPORT_RDT_UDP 0x41
#define TRANSPORT_RDT_TCP 0x42
#define TRANSPORT_UNKNOWN 0xFF

#define IS_RTP(p) (p & TRANSPORT_RTP) ? 1 : 0
#define IS_RDT(p) (p & TRANSPORT_RDT) ? 1 : 0

#define IS_UDP(p) (p & 0x01) ? 1 : 0
#define IS_TCP(p) (p & 0x02) ? 1 : 0

class CClientCnx : public CRtspProtocolResponse 
{
 public:			// Unimplemented
	CClientCnx();
	CClientCnx(CClientCnx &);
	CClientCnx & operator=(const CClientCnx &);


	CClientCnx(CRtspProxyApp * pApp, CRtspProxyCnx * pOwner, CTcpSocket * psock);
	virtual ~ CClientCnx(void);

	void Close(void);

	 void sendRequest(CRtspRequestMsg * pmsg);
	 void sendResponse(CRtspResponseMsg * pmsg);
	 void sendResponse(UINT code, UINT cseq);
	 void sendSetupResponse(CRtspResponseMsg * pmsg);

	 CRtspProtocol *GetRtspProtocol(void) const;

	 const CSockAddr & GetClientAddr(void) const;
	 const CSockAddr & GetSelfAddr(void) const;

 	 CacheSegment *get_cache_segment(u_int16_t idx);

	 /*! Compute the parsing of the SDP description of
	  *  the stream(s).
	  */
	 void parseSDP(char *sdp);

	 sdp_info_t* getSDPInfo(u_int8_t idx);

	 enum {
		 stConnected,
		 stClosed,
		 stLAST
	 } m_state;

	 /*! Returns a reference to the TCP socket used for RTSP communications.. */
	 CSocket *get_socket() {return m_pSock;}
	 
	 CString rewrite_transport_header(CString str, CString url);

 protected:
	 virtual void OnError(RtspErr err);

	 virtual void OnDescribeRequest(CRtspRequestMsg * pmsg);
	 virtual void OnAnnounceRequest(CRtspRequestMsg * pmsg);
	 virtual void OnGetParamRequest(CRtspRequestMsg * pmsg);
	 virtual void OnSetParamRequest(CRtspRequestMsg * pmsg);
	 virtual void OnOptionsRequest(CRtspRequestMsg * pmsg);
	 virtual void OnPauseRequest(CRtspRequestMsg * pmsg);
	 virtual void OnPlayRequest(CRtspRequestMsg * pmsg);
	 virtual void OnRecordRequest(CRtspRequestMsg * pmsg);
	 virtual void OnRedirectRequest(CRtspRequestMsg * pmsg);
	 virtual void OnSetupRequest(CRtspRequestMsg * pmsg);
	 virtual void OnTeardownRequest(CRtspRequestMsg * pmsg);
	 virtual void OnExtensionRequest(CRtspRequestMsg * pmsg);

	 virtual void OnDescribeResponse(CRtspResponseMsg * pmsg);
	 virtual void OnAnnounceResponse(CRtspResponseMsg * pmsg);
	 virtual void OnGetParamResponse(CRtspResponseMsg * pmsg);
	 virtual void OnSetParamResponse(CRtspResponseMsg * pmsg);
	 virtual void OnOptionsResponse(CRtspResponseMsg * pmsg);
	 virtual void OnPauseResponse(CRtspResponseMsg * pmsg);
	 virtual void OnPlayResponse(CRtspResponseMsg * pmsg);
	 virtual void OnRecordResponse(CRtspResponseMsg * pmsg);
	 virtual void OnRedirectResponse(CRtspResponseMsg * pmsg);
	 virtual void OnSetupResponse(CRtspResponseMsg * pmsg);
	 virtual void OnTeardownResponse(CRtspResponseMsg * pmsg);
	 virtual void OnExtensionResponse(CRtspResponseMsg * pmsg);


	 CRtspProxyCnx *m_pOwner;
	 CRtspProtocol *m_pprot;
	 CSockAddr m_addrClient;
	 CSockAddr m_addrSelf;
	 CSocket *m_pSock;

	 /*! List of the cache segments associated with the client
	  *  in this RTSP session.
	  */
	 CacheSegmentList *m_segment_list;

	 /*! Flag associated with the current session. */
	 bool m_use_cache;

	 /*! List of CachePlay objects. */
	 cache_play_list_t *m_cache_play_list;

	 /*! */
	 u_int8_t m_cache_playback;

	 CRtspProxyApp *m_app;

	 /*! List of SDP descriptions.. */
	 sdp_info_list_t *m_sdp_info_list;
};

class CServerCnx:public CRtspProtocolResponse,
		 public CResolverResponse, public CStreamResponse
{
 private:
	CServerCnx(void);
	CServerCnx(CServerCnx &);
	CServerCnx & operator=(const CServerCnx &);

 public:
	CServerCnx(CRtspProxyCnx * pOwner, CString strUrl, UINT16 uHost);
	virtual ~ CServerCnx(void);

	void Close(void);

	void sendRequest(CRtspRequestMsg * pmsg);
	void sendResponse(CRtspResponseMsg * pmsg);
	void sendResponse(UINT code, UINT cseq);

	CRtspProtocol *GetRtspProtocol(void) const;
	const CSockAddr & GetServerAddr(void) const;
	const CString & GetHostName(void) const;
	UINT16 GetPort(void) const;

	void AddRtspMsgToQueue(CRtspRequestMsg * pmsg);
	void ConnectToServer(CPCHAR szHost, UINT16 port);

 protected:
	virtual void OnError(RtspErr err);

	virtual void OnDescribeRequest(CRtspRequestMsg * pmsg);
	virtual void OnAnnounceRequest(CRtspRequestMsg * pmsg);
	virtual void OnGetParamRequest(CRtspRequestMsg * pmsg);
	virtual void OnSetParamRequest(CRtspRequestMsg * pmsg);
	virtual void OnOptionsRequest(CRtspRequestMsg * pmsg);
	virtual void OnPauseRequest(CRtspRequestMsg * pmsg);
	virtual void OnPlayRequest(CRtspRequestMsg * pmsg);
	virtual void OnRecordRequest(CRtspRequestMsg * pmsg);
	virtual void OnRedirectRequest(CRtspRequestMsg * pmsg);
	virtual void OnSetupRequest(CRtspRequestMsg * pmsg);
	virtual void OnTeardownRequest(CRtspRequestMsg * pmsg);
	virtual void OnExtensionRequest(CRtspRequestMsg * pmsg);

	virtual void OnDescribeResponse(CRtspResponseMsg * pmsg);
	virtual void OnAnnounceResponse(CRtspResponseMsg * pmsg);
	virtual void OnGetParamResponse(CRtspResponseMsg * pmsg);
	virtual void OnSetParamResponse(CRtspResponseMsg * pmsg);
	virtual void OnOptionsResponse(CRtspResponseMsg * pmsg);
	virtual void OnPauseResponse(CRtspResponseMsg * pmsg);
	virtual void OnPlayResponse(CRtspResponseMsg * pmsg);
	virtual void OnRecordResponse(CRtspResponseMsg * pmsg);
	virtual void OnRedirectResponse(CRtspResponseMsg * pmsg);
	virtual void OnSetupResponse(CRtspResponseMsg * pmsg);
	virtual void OnTeardownResponse(CRtspResponseMsg * pmsg);
	virtual void OnExtensionResponse(CRtspResponseMsg * pmsg);

	virtual void GetHostDone(int err, const CString & strQuery,
				 in_addr addrResult);
	virtual void GetHostDone(int err, in_addr addrQuery,
				 const CString & strResult);

	virtual void OnConnectDone(int err);
	virtual void OnReadReady(void);
	virtual void OnWriteReady(void);
	virtual void OnExceptReady(void);
	virtual void OnClosed(void);

 protected:
	enum {
		stConnected,
		stClosed,
		stLAST
	} m_state;

	CRtspProxyCnx *m_pOwner;
	CRtspProtocol *m_pprot;
	CSockAddr m_addrServer;

	CResolver *m_pResolver;
	CSockAddr m_addr;
	CTcpSocket *m_pSock;

	//per server info
	CString m_strHost;
	UINT16 m_uPort;
	CRtspRequestMsgList m_RequestMsgQueue;

	void SendClientConnectionError(void);

};

typedef TDoubleList < CServerCnx * >CServerCnxList;


class CCSeqPair
{
 private:
	CCSeqPair(void);

 public:
	CCSeqPair(const CString & cseqToClient,
		  const CString & cseqToServer,
		  const CString & strHostName, UINT16 uPort);
	~CCSeqPair(void);

	CString m_cseqToClient;
	CString m_cseqToServer;
	CString m_strHost;
	UINT m_uPort;
};

typedef TDoubleList < CCSeqPair * >CCseqPairList;

class CRtspProxyApp;

class CRtspProxyCnx
{
 private:			// Unimplemented
	CRtspProxyCnx(void);
	CRtspProxyCnx(CRtspProxyCnx &);
	CRtspProxyCnx & operator=(const CRtspProxyCnx &);

 public:
	CRtspProxyCnx(CRtspProxyApp * pOwner, CTcpSocket * psock,
		      CPCHAR viaHdrValue);
	virtual ~ CRtspProxyCnx(void);

	void PassToClient(CRtspRequestMsg * pmsg, CServerCnx * pServerCnx);
	void PassToClient(CRtspResponseMsg * pmsg,
			  CServerCnx * pServerCnx);
	void PassToServer(CRtspRequestMsg * pmsg);
	void PassToServer(CRtspResponseMsg * pmsg);

	void PassSetupRequestMsgToServer(CRtspRequestMsg * pmsg);
	void PassSetupResponseToClient(CRtspResponseMsg * pmsg,
				       CServerCnx * pServerCnx);

	void DeleteSessionByServerSessionID(const CString &
					    strServerSessionID,
					    const CString & strHost);
	void DeleteSessionByClientSessionID(const CString &
					    strClientSessionID);
	CString FindClientSessionID(const CString & strServerSessionID,
				    const CString & strHost);
	CString FindServerSessionID(const CString & strClientSessionID);

	CServerCnx *FindServerCnx(const CString & strHost, UINT16 uHost);

	void OnServerConnectionError(UINT code, UINT cseq);
	void OnClientCnxClosed(void);
	void OnServerCnxClosed(CServerCnx * pServerCnx);

	CClientCnx * get_client_cnx() {return m_pClientCnx;}


	/*!
	 * Set the transport protocol used in this connection.
	 */
	void set_transport(const char *str);


	bool caching_packets;

 protected:

	CClientCnx * m_pClientCnx;
	CServerCnxList m_listServerCnx;

	CRtspProxySessionList m_listRtspProxySession;
	UINT16 m_clientChannel;	// for interleaved data only

	CRtspProxyApp *m_pOwner;

	CString m_viaHdrValue;

	UINT32 m_cseqToClient;
	UINT16 m_sessionIndex;

	CCseqPairList m_listCCSeqPairList;

	bool SetViaHdr(CRtspMsg * pMsg);

	uint8_t m_transport_type;
};

typedef TDoubleList < CRtspProxyCnx * >CRtspProxyCnxList;


class CRtspProxyApp :
       public CApp,
       public CListenSocketResponse
{
 public:
	CRtspProxyApp(int argc, char **argv);
	virtual ~ CRtspProxyApp(void);

	void SetPort(UINT16 port);
	/**
	 * Enable or disable the cache utilisation.
	 */
	void UseCache(bool val);

	/**
	 * Returns wheter we are using the cache system or not.
	 */
	bool UseCache();

	/**
	 * Returns the instance of the cache manager.
	 */
	Cache * cache() {return m_cache;}

	void DeleteProxyCnx(CRtspProxyCnx * proxyCnx);

 protected:
	virtual bool Init(void);
	virtual int Exit(void);

	virtual void OnConnection(CTcpSocket * psock);
	virtual void OnClosed(void);

 protected:
	CListenSocket m_sock;
	UINT16 m_port;
	CRtspProxyCnxList m_listProxyCnx;
	char m_viaHdrValue[25];

 private:
	bool m_use_cache;
	Cache *m_cache;
};

#endif	//ndef _RTSPPROXY_H

/** LOG **
 *
 * $Log: rtspproxy.h,v $
 * Revision 1.3  2003/11/17 16:14:16  mat
 * make-up
 *
 *
 */

