/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _RTSPPROT_H
#define _RTSPPROT_H

#include "sock.h"
#include "rtspmsg.h"
#include "tran.h"

class CRtspProtocol;

#define RTSP_DEFAULT_PORT 554

enum RtspErr {
	RTSPE_NONE,
	RTSPE_CLOSED,
	RTSPE_NOTRAN,
	RTSPE_MAX
};

// A "pseudo-socket" for RTSP/TCP interleaved data
class CRtspInterleavedSocket:public CSocket {
	friend class CRtspProtocol;

      private:
	 CRtspInterleavedSocket(const CRtspInterleavedSocket &);
	 CRtspInterleavedSocket & operator=(const CRtspInterleavedSocket
					    &);

      public:
	 CRtspInterleavedSocket(void);
	 CRtspInterleavedSocket(CStreamResponse * pResponse);
	 virtual ~ CRtspInterleavedSocket(void);

	virtual bool IsOpen(void);
	virtual void Close(void);
	virtual size_t Read(PVOID pbuf, size_t nLen);
	virtual size_t Write(CPVOID pbuf, size_t nLen);

	bool Connect(UINT chan, CRtspProtocol * pProt);

	// These functions should not be called
	UINT32 GetPeerIP(void) {
		assert(false);
		return 0;
	} void Select(UINT32) {
		assert(false);
	}

      protected:
	CRtspProtocol * m_pProt;
	UINT m_chan;
	CBuffer *m_pbuf;
};

class CRtspProtocolResponse {
      public:
	virtual void OnError(RtspErr err) = 0;

	virtual void OnDescribeRequest(CRtspRequestMsg * pmsg) = 0;
	virtual void OnAnnounceRequest(CRtspRequestMsg * pmsg) = 0;
	virtual void OnGetParamRequest(CRtspRequestMsg * pmsg) = 0;
	virtual void OnSetParamRequest(CRtspRequestMsg * pmsg) = 0;
	virtual void OnOptionsRequest(CRtspRequestMsg * pmsg) = 0;
	virtual void OnPauseRequest(CRtspRequestMsg * pmsg) = 0;
	virtual void OnPlayRequest(CRtspRequestMsg * pmsg) = 0;
	virtual void OnRecordRequest(CRtspRequestMsg * pmsg) = 0;
	virtual void OnRedirectRequest(CRtspRequestMsg * pmsg) = 0;
	virtual void OnSetupRequest(CRtspRequestMsg * pmsg) = 0;
	virtual void OnTeardownRequest(CRtspRequestMsg * pmsg) = 0;
	virtual void OnExtensionRequest(CRtspRequestMsg * pmsg) = 0;

	virtual void OnDescribeResponse(CRtspResponseMsg * pmsg) = 0;
	virtual void OnAnnounceResponse(CRtspResponseMsg * pmsg) = 0;
	virtual void OnGetParamResponse(CRtspResponseMsg * pmsg) = 0;
	virtual void OnSetParamResponse(CRtspResponseMsg * pmsg) = 0;
	virtual void OnOptionsResponse(CRtspResponseMsg * pmsg) = 0;
	virtual void OnPauseResponse(CRtspResponseMsg * pmsg) = 0;
	virtual void OnPlayResponse(CRtspResponseMsg * pmsg) = 0;
	virtual void OnRecordResponse(CRtspResponseMsg * pmsg) = 0;
	virtual void OnRedirectResponse(CRtspResponseMsg * pmsg) = 0;
	virtual void OnSetupResponse(CRtspResponseMsg * pmsg) = 0;
	virtual void OnTeardownResponse(CRtspResponseMsg * pmsg) = 0;
	virtual void OnExtensionResponse(CRtspResponseMsg * pmsg) = 0;
};

class CRtspProtocol:public CStreamResponse {
	//XXX: for access to m_psock, but should be more elegant
	friend class CRtspInterleavedSocket;

      public:
	 CRtspProtocol(CRtspProtocolResponse * pResponse);
	 virtual ~ CRtspProtocol(void);

	void Init(CSocket * pSock);
	UINT32 GetNextCseq(void) {
		return ++m_cseqSend;
	} void SendRequest(CRtspRequestMsg * pmsg);
	void SendResponse(CRtspResponseMsg * pmsg);

      protected:
	// These functions handle input
	char *parseLine(void);
	void handleReadCmd(void);
	void handleReadHdr(void);
	void handleReadBody(void);
	void handleReadPkt(void);
	void dispatchMessage(void);

      protected:
	virtual void OnConnectDone(int err);
	virtual void OnReadReady(void);
	virtual void OnWriteReady(void);
	virtual void OnExceptReady(void);
	virtual void OnClosed(void);

      private:
	class CRtspRequestTag {
	      public:
	      CRtspRequestTag(UINT cseq, RtspVerb verb):m_cseq(cseq),
		    m_verb(verb)
		{
		}

		//XXX: Keep rtsp version, url, etc...?
		UINT32 m_cseq;
		RtspVerb m_verb;
	};
	typedef TDoubleList < CRtspRequestTag > CRtspRequestTagList;

      private:
	CRtspProtocolResponse * m_pResponse;
	CRtspInterleavedSocket *m_ppSockets[256];

	enum ReadState {
		stFail,		// Unrecoverable error occurred
		stSync,		// Trying to resync
		stReady,	// Waiting for a command
		stPkt,		// Reading interleaved packet
		stCmd,		// Reading command (request or response line)
		stHdr,		// Reading headers
		stBody,		// Reading body (entity)
		stDispatch,	// Fully formed message
		stREADSTATE_LAST
	} m_state;

	CSocket *m_psock;
	size_t m_nBufLen;
	char *m_pReadBuf;
	char *m_pTail;

	UINT32 m_cseqSend;	// Our next cseq
	UINT32 m_cseqRecv;	// Peer's last cseq
	CRtspMsg *m_pmsg;	// Incoming message
	size_t m_nBodyLen;	// Length of body (entity)

	CRtspRequestTagList m_listRequestQueue;	// Requests awaiting responses
};

#endif				//ndef _RTSPPROT_H

/** LOG **
 *
 * $Log: rtspprot.h,v $
 * Revision 1.2  2003/11/17 16:14:08  mat
 * make-up
 *
 *
 */

