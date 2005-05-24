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

#include "str.h"

#define RTSP_TRANSPORTHDR_OTHER        0x00
#define RTSP_TRANSPORTHDR_CLIENTPORT   0x01
#define RTSP_TRANSPORTHDR_SERVERPORT   0x02
#define RTSP_TRANSPORTHDR_SOURCE       0x04
#define RTSP_TRANSPORTHDR_INTERLEAVED  0x08


class CRtspTransportHdrField {
      public:

	int type;
	CString strField;
};

typedef TDoubleList < CRtspTransportHdrField > CRtspTransportHdrFieldList;

class CSingleTransportHdr {
      private:
	CSingleTransportHdr(void);

      public:
	 CSingleTransportHdr(const CString & strValue);

	void GetServerBasePort(UINT16 * pBasePort, int *nPorts);
	void GetClientBasePort(UINT16 * pBasePort, int *nPorts);
	void SetServerBasePort(UINT16 basePort);
	void SetClientBasePort(UINT16 basePort);

	void SetSourceAddr(const CInetAddr & addrSource);

	bool IsInterleaved(void);
	CPCHAR GetHdrString(void);

      protected:
	 CString m_strHdr;
	int m_nServerPorts;
	int m_nClientPorts;
	UINT16 m_serverBasePort;
	UINT16 m_clientBasePort;
	CInetAddr m_addrSource;
	bool m_bInterleaved;

	CRtspTransportHdrFieldList m_listFields;

	void Parse(const CString & strValue);
};

typedef TDoubleList < CSingleTransportHdr * >CTransportHdrList;

class CRequestTransportHdr {
      private:
	CRequestTransportHdr(void);

      public:
	 CRequestTransportHdr(const CString & strValue);
	~CRequestTransportHdr(void);

	void GetBasePort(UINT16 * pBasePort, int *nPorts);
	void SetPort(UINT16 basePort);

	bool IsInterleaved(void);
	CPCHAR GetHdrString(void);

      protected:
	 CString m_strHdr;
	CTransportHdrList m_listTransports;

	// if one or more of the transports is not interleaved, this value is set to
	// false, since we need to create udp sockets.
	bool m_bIsInterleaved;

	void Parse(void);
};

/** LOG **
 *
 * $Log: tranhdr.h,v $
 * Revision 1.3  2003/11/17 16:14:16  mat
 * make-up
 *
 *
 */

