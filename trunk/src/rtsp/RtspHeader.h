
#ifndef _RTSP_HEADER_H_
#define _RTSP_HEADER_H_

#include <QString>
#include <QList>

class RtspHeader
{
public:
	RtspHeader( const QString& key );
	RtspHeader( const QString& key, const QString& value );
	
	virtual ~RtspHeader();
	
	const QString& getKey() const { return key; }
	const QString& getValue() const { return value; }
	
	void setValue( const QString& value ) { this->value = value; }
	
private:
	QString key;
	QString value;
};

typedef QList<RtspHeader> RtspHeaderList;

#endif // _RTSP_HEADER_H
