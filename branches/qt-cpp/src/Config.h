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

#ifndef _RTSP_CONFIG_H_
#define _RTSP_CONFIG_H_

#include <QtCore>

class Config : public QObject
{
	Q_OBJECT

public:
	Config( QObject * parent );
	virtual ~Config();
	
	static qint32 getIntValue( const QString& key );
	// static QString getValue( const QString& key );

	static QStringList getConfigFileList();
	
private:
	void readFromFile( const QString& fileName );
};

#endif // _RTSP_CONFIG_H_
