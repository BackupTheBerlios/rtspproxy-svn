/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#ifndef _RTSPMSG_H
#define _RTSPMSG_H

#include <sys/types.h>
#include "../libapp/str.h"
#include "../libapp/tlist.h"

// These must correspond with s_pVerbs indices
enum RtspVerb {
	VERB_NONE,
	VERB_ANNOUNCE,
	VERB_DESCRIBE,
	VERB_GETPARAM,
	VERB_OPTIONS,
	VERB_PAUSE,
	VERB_PLAY,
	VERB_RECORD,
	VERB_REDIRECT,
	VERB_SETUP,
	VERB_SETPARAM,
	VERB_TEARDOWN,
	// extensions here
	VERB_LAST
};

enum RtspMsgType {
	RTSP_TYPE_NONE,
	RTSP_TYPE_REQUEST,
	RTSP_TYPE_RESPONSE,
	RTSP_TYPE_LAST
};

class CRtspHdr {
      private:
	CRtspHdr(void);

      public:
	 CRtspHdr(const CString & strKey);
	 CRtspHdr(const CString & strKey, const CString & strVal);

	const CString & GetKey(void) const;
	const CString & GetVal(void) const;
	void SetVal(const CString & strVal);

      protected:
	 CString m_strKey;
	CString m_strVal;
};
typedef TDoubleList < CRtspHdr * >CRtspHdrList;

class CRtspMsg {
      private:
	bool operator==(const CRtspMsg & other) const;
	bool operator!=(const CRtspMsg & other) const;

      public:
	 CRtspMsg(void);
	 CRtspMsg(const CRtspMsg & other);
	 virtual ~ CRtspMsg(void);

	const CRtspMsg & operator=(const CRtspMsg & other);

	operator  CPBYTE(void) const;
	BYTE operator[] (UINT nPos) const;

	virtual RtspMsgType GetType(void) const;

	// Total header length for key/val pairs (incl. ": " and CRLF)
	// but NOT separator CRLF
	size_t GetHdrLen(void) const;
	size_t GetBufLen(void) const;

	BYTE GetAt(UINT nPos) const;
	void SetAt(UINT nPos, BYTE by);

	void GetRtspVer(UINT * puMajor, UINT * puMinor) const;
	void SetRtspVer(UINT uMajor, UINT uMinor);

	size_t GetHdrCount(void) const;
	CString GetHdr(const CString & strKey) const;
	CRtspHdr *GetHdr(UINT nIndex) const;
	void SetHdr(const CString & strKey, const CString & strVal);
	void SetHdr(const CRtspHdr & hdrNew);

	PBYTE GetBuf(void) const;
	void SetBuf(CPBYTE buf, size_t nLen);

      protected:
	 UINT32 m_nRtspVer;	// RTSP version (hiword.loword)
	UINT32 m_nSeq;
	CRtspHdrList m_listHdrs;
	size_t m_nBufLen;
	PBYTE m_pbuf;
};
typedef TDoubleList < CRtspMsg * >CRtspMsgList;

class CRtspRequestMsg:public CRtspMsg {
      public:
	CRtspRequestMsg(void);
	 CRtspRequestMsg(const CRtspRequestMsg & other);
	 virtual ~ CRtspRequestMsg(void);

	const CRtspRequestMsg & operator=(const CRtspRequestMsg & other);

	virtual RtspMsgType GetType(void) const;

	RtspVerb GetVerb(void) const;
	CPCHAR GetVerbStr(void) const;
	void SetVerb(RtspVerb verb);
	void SetVerb(CPCHAR szVerb);

	CPCHAR GetUrl(void) const;
	void SetUrl(const CString & strUrl);

      protected:
	 RtspVerb m_verb;
	CString m_strUrl;
};
typedef TDoubleList < CRtspRequestMsg * >CRtspRequestMsgList;

class CRtspResponseMsg:public CRtspMsg {
      public:
	CRtspResponseMsg(void);
	 CRtspResponseMsg(const CRtspResponseMsg & other);
	 virtual ~ CRtspResponseMsg(void);

	const CRtspResponseMsg & operator=(const CRtspResponseMsg & other);

	virtual RtspMsgType GetType(void) const;

	UINT GetStatusCode(void) const;
	const CString & GetStatusMsg(void) const;
	void SetStatus(UINT nCode, CPCHAR szMsg = NULL);

      protected:
	 UINT m_nCode;
	CString m_strStatusMsg;
};
typedef TDoubleList < CRtspResponseMsg * >CRtspResponseMsgList;

#endif				//ndef _RTSPMSG_H



