
#include "RtspHeader.h"

#include <QString>

RtspHeader::RtspHeader( const QString& key )
{
	this->key = key;
}

RtspHeader::RtspHeader( const QString& key, const QString& value )
{
	this->key = key;
	this->value = value;
}

RtspHeader::~RtspHeader()
{
}


