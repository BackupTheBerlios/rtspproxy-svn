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
 
#include "RtspTransport.h"
 
/**
	\code
   Transport           =    "Transport" ":"
                            1\#transport-spec
   transport-spec      =    transport-protocol/profile[/lower-transport]
                            *parameter
   transport-protocol  =    "RTP"
   profile             =    "AVP"
   lower-transport     =    "TCP" | "UDP"
   parameter           =    ( "unicast" | "multicast" )
                       |    ";" "destination" [ "=" address ]
                       |    ";" "interleaved" "=" channel [ "-" channel ]
                       |    ";" "append"
                       |    ";" "ttl" "=" ttl
                       |    ";" "layers" "=" 1*DIGIT
                       |    ";" "port" "=" port [ "-" port ]
                       |    ";" "client_port" "=" port [ "-" port ]
                       |    ";" "server_port" "=" port [ "-" port ]
                       |    ";" "ssrc" "=" ssrc
                       |    ";" "mode" = <"> 1\#mode <">
   ttl                 =    1*3(DIGIT)
   port                =    1*5(DIGIT)
   ssrc                =    8*8(HEX)
   channel             =    1*3(DIGIT)
   address             =    host
   mode                =    <"> *Method <"> | Method


   Example:
     Transport: RTP/AVP;multicast;ttl=127;mode="PLAY",
                RTP/AVP;unicast;client_port=3456-3457;mode="PLAY"
 */
 
#include <QtCore>
 
RtspTransports::RtspTransports( const QString& str )
{
	QStringList list = str.split( "," );
 	QStringListIterator it( list );
 	while ( it.hasNext() ) {
 		transports << *( new RtspTransport( it.next() ) );
 	}
}
 
RtspTransports::~RtspTransports()
{
}

QString RtspTransports::toString() const
{
	QStringList list;
	RtspTransportListIterator it( transports );
	while ( it.hasNext() ) {
		list << it.next().toString();
	}
	return list.join(",");
}

/////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////


RtspTransport::TransportProtocol RtspTransport::getTransportProtocol( const QString& str )
{
	if ( str == "RTP" ) return RTP;
	if ( str == "RDT" ) return RDT;
	
	return TransportProtocolNone;	
}

QString RtspTransport::getTransportProtocol( TransportProtocol p )
{
	switch ( p ) {
		case RTP : return "RTP";
		case RDT : return "RDT";
		default: return "";
	};	
}

RtspTransport::Profile RtspTransport::getProfile( const QString& str )
{
	if ( str == "AVP" ) return AVP;
	
	return ProfileNone;
}

QString RtspTransport::getProfile( Profile p )
{
	switch ( p ) {
		case AVP : return "AVP";
		default: return "";
	};
}

RtspTransport::LowerTransport RtspTransport::getLowerTransport( const QString& str )
{
	if ( str == "TCP" ) return TCP;
	if ( str == "UDP" ) return UDP;
	return LowerTransportNone;
}

QString RtspTransport::getLowerTransport( LowerTransport p )
{
	switch ( p ) {
		case TCP : return "TCP";
		case UDP : return "UDP";
		default  : return "";
	};
}

RtspTransport::DeliveryType RtspTransport::getDeliveryType( const QString& str )
{
	if ( str == "unicast" ) return UNICAST;
	if ( str == "multicast" ) return MULTICAST;
	return DeliveryTypeNone;
}

QString RtspTransport::getDeliveryType( DeliveryType p )
{
	switch ( p ) {
		case UNICAST : return "unicast";
		case MULTICAST : return "multicast";
		default: return "";
	};
}


QString _getStrValue( const QString& str )
{
	QStringList list = str.split( "=" );
	if ( list.count() != 2 )
		return QString::null;
		
	return list.at( 1 );
}

Int16Pair _getPairValue( const QString& str )
{
	Int16Pair pair;
	pair.first = 0; pair.second = 0;
	QStringList list = str.split( "=" );
	if ( list.count() != 2 )
		return pair;
	pair.first = list.at( 1 ).section( "-", 0, 0 ).toInt();
	pair.second = list.at( 1 ).section( "-", 1, 1 ).toInt();
	return pair;
}

/////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////


RtspTransport::RtspTransport( const QString& str )
{
	qDebug() << "New Transport:" << str;
	transport_protocol = TransportProtocolNone;
	profile = ProfileNone;
	lower_transport = LowerTransportNone;
	delivery_type = DeliveryTypeNone;
	destination = QString::null;
	interleaved = QString::null;
	layers = 0;
	append = false;
	ttl = 0;
	port.first = 0; port.second = 0;
	client_port.first = 0; client_port.second = 0;
	server_port.first = 0; server_port.second = 0;
	ssrc = QString::null;
	mode = QString::null;
	
	parseTransport( str );
}

RtspTransport::~RtspTransport()
{
}
 
void RtspTransport::parseTransport( const QString& transport )
{
	QStringList tokens = transport.split( ";" );
	QStringListIterator it( tokens );
	while ( it.hasNext() ) {
		QString tok = it.next();
		
		// First check for the transport protocol
		if ( tok.startsWith( "RTP" ) || tok.startsWith( "RDT" ) ) {
			QStringList tpl = tok.split( "/" );
			transport_protocol = getTransportProtocol( tpl.at( 0 ) );
			if ( tpl.count() > 1 )
				profile = getProfile( tpl.at( 1 ) );
			if ( tpl.count() > 2 )
				lower_transport = getLowerTransport( tpl.at( 2 ) );
			continue;
		}
		
		if ( tok == "unicast" )
			setDeliveryType( UNICAST );
		else if ( tok == "multicast" ) 
			setDeliveryType( MULTICAST );
		else if ( tok.startsWith( "destination" ) ) 
			setDestination( _getStrValue( tok ) );
		else if ( tok.startsWith( "interleaved" ) )
			setInterleaved( _getStrValue( tok ) );
		else if ( tok.startsWith( "append" ) )
			setAppend( true );
		else if ( tok.startsWith( "layers" ) )
			setLayers( _getStrValue( tok ).toInt() );
		else if ( tok.startsWith( "ttl" ) )
			setTTL( _getStrValue( tok ).toInt() );
		else if ( tok.startsWith( "port" ) )
			setPort( _getPairValue( tok ) );
		else if ( tok.startsWith( "client_port" ) )
			setClientPort( _getPairValue( tok ) );
		else if ( tok.startsWith( "server_port" ) )
			setServerPort( _getPairValue( tok ) );
		else if ( tok.startsWith( "ssrc" ) ) 
			setSSRC( _getStrValue( tok ) );
		else if ( tok.startsWith( "mode" ) ) 
			setMode( _getStrValue( tok ) );
	}
}

QString RtspTransport::toString() const
{
	QString res;
	QTextStream out( &res );
	out << getTransportProtocol( transport_protocol );
	if ( profile != ProfileNone )
		out << "/" << getProfile( profile );
	if ( lower_transport != LowerTransportNone )
		out << "/" << getLowerTransport( lower_transport );
	if ( delivery_type != DeliveryTypeNone )
		out << ";" << getDeliveryType( delivery_type );
	if ( destination != QString::null )
		out << ";destination=" << destination;
	if ( interleaved != QString::null )
		out << ";interleaved=" << interleaved;
	if ( append )
		out << ";append";
	if ( layers )
		out << ";layers=" << layers;
	if ( ttl )
		out << ";ttl=" << ttl;
	if ( port.first )
		out << ";port=" << port.first << "-" << port.second;
	if ( client_port.first )
		out << ";client_port=" << client_port.first << "-" << client_port.second;
	if ( server_port.first )
		out << ";server_port=" << server_port.first << "-" << server_port.second;
	if ( ssrc != QString::null )
		out << ";ssrc=" << ssrc;
	if ( mode != QString::null )
		out << ";mode=" << mode;
	return res;
}


#ifdef TEST_RTSP_TRANSPORT

int main( int argc, char** argv )
{
	if ( argc < 2 )
		return;
	RtspTransports *t = new RtspTransports( argv[ 1 ] );
	
	qDebug() << "RESULT:";
	qDebug() << t->toString();
}

#endif

 