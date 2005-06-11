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

#include "RtspProtocol.h"
#include "RtspRequest.h"
#include "RtspResponse.h"
 
RtspProtocol::RtspProtocol( QTcpSocket* socket )
	: QObject()
{
	cseqToSend = 0;
	// cseqReceived = 0;
	internalReadState = stCmd; 
	this->socket = socket;
	rtspMessage = NULL;
	rtspMessageCompleted = false;
	rtspMessageBodyCompleted = false;
	lastRequestType = RtspRequest::VerbNone;
	
	connect( this, SIGNAL( messageParsed() ), this, SLOT( dispatchMessage() ) );
}
 
RtspProtocol::~RtspProtocol()
{
}
 
void RtspProtocol::sendRequest( RtspRequest* request )
{
	QString cseq = request->getHeader( "CSeq" );
	if ( cseq == QString::null ) {
		// CSeq is not set
		request->setHeader( "CSeq", QString( getNextCSeq() ) );
	}
	
	socket->write( request->toString() );
	lastRequestType = request->getVerb();
	delete request;
}

void RtspProtocol::sendResponse( RtspResponse* response )
{
	QString cseq = response->getHeader( "CSeq" );
	if ( cseq == QString::null ) {
		// CSeq is not set
		response->setHeader( "CSeq", QString( cseqToSend ) );
	}
	
	socket->write( response->toString() );
	delete response;
} 

void RtspProtocol::readData() 
{
	buffer = socket->readAll();
	parse();
}

/**
 *	Do the parsing on the incoming stream. 
 *  If the stream does not contain the entire RTSP
 *  message wait for other data to arrive, before 
 *  dispatching the message.
 */
void RtspProtocol::parse()
{		
	// qDebug() << "BUFFER(" << buffer << ")";
	QTextStream stream( buffer );	
	
	while ( ! rtspMessageBodyCompleted ) {
		QString line = stream.readLine();
		// qDebug() << "LINE:" << line;
		
		if ( line.isEmpty() ) {
			if ( line.isNull() )
				return; // no more data to be read now
				
			// qDebug() << "empty line";
			rtspMessageBodyCompleted = true;
			break; 
		}

		if ( rtspMessage == NULL ) {
			rtspMessageCompleted = false;
			rtspMessageBodyCompleted = false;
			
			// we have to read the first line of the message
			if ( line.startsWith( "RTSP" ) ) {
				// this is a RTSP response
				int code = line.section( " ", 1, 1 ).toInt();
				rtspMessage = new RtspResponse();
				((RtspResponse*)rtspMessage)->setCode( code );
				// qDebug() << "Request - code:" << code;
			} else {
				// this is a RTSP request
				QString verb = line.section( " ", 0, 0 );
				QUrl url = line.section( " ", 1, 1 );
				rtspMessage = new RtspRequest();
				((RtspRequest*)rtspMessage)->setVerb( verb );
				((RtspRequest*)rtspMessage)->setUrl( url );
				// qDebug() << "Request - verb:" << verb << "url:" << url;
			}
		} else {
			// this is an header
			QString key = line.section( ": ", 0, 0 );
			QString value = line.section( ": ", 1, 1 );
			rtspMessage->setHeader( key, value );
			// qDebug() << "Header - key:" << key << "value:" << value;
		}
	}
	
	if ( rtspMessage == NULL ) {
		qWarning() << "RTSP message malformed";
		return;
	}
	
	if ( ! rtspMessageBodyCompleted ) 
		return; // wait to complete the message body before proceding
	
	qint32 bufferLen = rtspMessage->getHeader("Content-Length").toInt();
	if ( bufferLen == 0 ) {
		// there's no buffer to be read
		rtspMessageCompleted = true;
		emit messageParsed();
		return;
	}
	
	// read the content buffer 
	QByteArray content = stream.readAll().toAscii();
	if ( content.size() == 0 )
		return;
	
	int bytesToRead = bufferLen - rtspMessage->getBufferSize();
	if ( content.size() > bytesToRead ) 
		content.resize( bytesToRead );
			
	rtspMessage->addToBuffer( content );
	if ( rtspMessage->getBufferSize() >= bufferLen ) {
		rtspMessageCompleted = true;
		emit messageParsed();
	}
}

/** 
 * Dispatch the parsed RTSP message emitting the 
 * appropriate signal.
 */
void RtspProtocol::dispatchMessage()
{
	qDebug() << "MSG";
	qDebug() << rtspMessage->toString();
	
	if ( rtspMessage->getType() == RtspMessage::TypeRequest ) {
		// Dispatch a request message
		switch ( ((RtspRequest*)rtspMessage)->getVerb() ) {
			case RtspRequest::VerbAnnounce:
				emit requestAnnounce( (RtspRequest*)rtspMessage );
				break;
			case RtspRequest::VerbDescribe:
				emit requestDescribe( (RtspRequest*)rtspMessage );
				break;
			case RtspRequest::VerbGetParam:
				emit requestGetParam( (RtspRequest*)rtspMessage );
				break;
			case RtspRequest::VerbOptions:
				emit requestOptions( (RtspRequest*)rtspMessage );
				break;
			case RtspRequest::VerbPause:
				emit requestPause( (RtspRequest*)rtspMessage );
				break;
			case RtspRequest::VerbPlay:
				emit requestPlay( (RtspRequest*)rtspMessage );
				break;
			case RtspRequest::VerbRecord:
				emit requestRecord( (RtspRequest*)rtspMessage );
				break;
			case RtspRequest::VerbRedirect:
				emit requestRedirect( (RtspRequest*)rtspMessage );
				break;
			case RtspRequest::VerbSetup:
				emit requestSetup( (RtspRequest*)rtspMessage );
				break;
			case RtspRequest::VerbSetParam:
				emit requestTeardown( (RtspRequest*)rtspMessage );
				break;
			default:
				qWarning() << "Unknown request verb type";
		}
		
	} else if ( rtspMessage->getType() == RtspMessage::TypeResponse ) {
		// Dispatch a response message
		switch ( lastRequestType ) {
			case RtspRequest::VerbAnnounce:
				emit responseAnnounce( (RtspResponse*)rtspMessage );
				break;
			case RtspRequest::VerbDescribe:
				emit responseDescribe( (RtspResponse*)rtspMessage );
				break;
			case RtspRequest::VerbGetParam:
				emit responseGetParam( (RtspResponse*)rtspMessage );
				break;
			case RtspRequest::VerbOptions:
				emit responseOptions( (RtspResponse*)rtspMessage );
				break;
			case RtspRequest::VerbPause:
				emit responsePause( (RtspResponse*)rtspMessage );
				break;
			case RtspRequest::VerbPlay:
				emit responsePlay( (RtspResponse*)rtspMessage );
				break;
			case RtspRequest::VerbRecord:
				emit responseRecord( (RtspResponse*)rtspMessage );
				break;
			case RtspRequest::VerbRedirect:
				emit responseRedirect( (RtspResponse*)rtspMessage );
				break;
			case RtspRequest::VerbSetup:
				emit responseSetup( (RtspResponse*)rtspMessage );
				break;
			case RtspRequest::VerbSetParam:
				emit responseTeardown( (RtspResponse*)rtspMessage );
				break;
			default:
				qWarning() << "Unknown request verb type";
		}
	}
	
	// the message will be deleted when sent
	rtspMessage = NULL;
	rtspMessageCompleted = false;
	rtspMessageBodyCompleted = false;
}

 