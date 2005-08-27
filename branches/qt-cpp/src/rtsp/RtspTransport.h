/**************************************************************************
 *   Copyright (C) 2005 Matteo Merli <matteo.merli@gmail.com>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *   $Id$
 * 
 *   $URL$
 * 
 *****************************************************************************/
 
#ifndef _RTSP_TRANSPORT_H_
#define _RTSP_TRANSPORT_H_

#include <QtCore>

typedef QPair<quint16,quint16> Int16Pair;

class RtspTransport
{
public:
	enum TransportProtocol {
		TransportProtocolNone,
		RTP,
		RDT
	};

	enum Profile {
		ProfileNone,
		AVP
	};
	
	enum LowerTransport {
		LowerTransportNone,
		TCP,
		UDP
	};
	
	enum DeliveryType {
		DeliveryTypeNone,
		UNICAST,
		MULTICAST
	};

	RtspTransport( const QString& str );
	~RtspTransport();
	
	// Getters
	TransportProtocol getTransportProtocol() const { return transport_protocol; }
	Profile getPofile() const { return profile; }
	LowerTransport getLowerTransport() const { return lower_transport; }
	DeliveryType getDeliveryType() const { return delivery_type; }
	QString getDestination() const { return destination; }
	QString getInterleaved() const { return interleaved; }
	bool getAppend() const { return append; }
	quint32 getLayers() const { return layers; }
	quint32 getTTL() const { return ttl; }
	Int16Pair getPort() const { return port; }
	Int16Pair getClientPort() const { return client_port; }
	Int16Pair getServerPort() const { return server_port; }
	QString getSSRC() const { return ssrc; }
	QString getMode() const { return mode; }
	
	// Setters
	void setTransportProtocol( TransportProtocol transport_protocol ) { this->transport_protocol = transport_protocol; }
	void setProfile( Profile profile ) { this->profile = profile; }
	void setLowerTransport( LowerTransport lower_transport ) { this->lower_transport = lower_transport; }
	void setDeliveryType( DeliveryType delivery_type ) { this->delivery_type = delivery_type; }
	void setDestination( const QString& destination ) { this->destination = destination; }
	void setInterleaved( const QString& interleaved ) { this->interleaved = interleaved; }
	void setAppend( bool append ) { this->append = append; }
	void setLayers( quint32 layers ) { this->layers = layers; }
	void setTTL( quint32 ttl ) { this->ttl = ttl; }
	void setPort( const Int16Pair& port ) { this->port = port; }
	void setClientPort( const Int16Pair& client_port ) { this->client_port = client_port; }
	void setServerPort( const Int16Pair& server_port ) { this->server_port = server_port; }
	void setSSRC( const QString& ssrc ) { this->ssrc = ssrc; }
	void setMode( const QString& mode ) { this->mode = mode; }
	
	QString toString() const;
	
private:
	void parseTransport( const QString& transport );

	TransportProtocol transport_protocol;
	Profile profile;
	LowerTransport lower_transport;
	DeliveryType delivery_type;
	
	QString destination;
	QString interleaved;
	qint32 layers;
	bool append;
	quint32 ttl;
	Int16Pair port;
	Int16Pair client_port;
	Int16Pair server_port;
	QString ssrc;
	QString mode;
	
	
	//// Converters between enums and QStrings
	static TransportProtocol getTransportProtocol( const QString& str );
	static QString getTransportProtocol( TransportProtocol p );
	static Profile getProfile( const QString& str );
	static QString getProfile( Profile p );
	static LowerTransport getLowerTransport( const QString& str );
	static QString getLowerTransport( LowerTransport p );
	static DeliveryType getDeliveryType( const QString& str );
	static QString getDeliveryType( DeliveryType p );
};

typedef QList<RtspTransport> RtspTransportList;
typedef QListIterator<RtspTransport> RtspTransportListIterator;
 
class RtspTransports
{
public:
	RtspTransports( const QString& str );
	~RtspTransports();
	
	QString toString() const;
	
private:
	RtspTransportList transports;
};
 
#endif // _RTSP_TRANSPORT_H_