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

#include "Config.h"

#include <QtCore>

typedef QHash<QString, QString> ConfigHash;

static ConfigHash * configHash;

static QStringList * configFileList;

Config::Config( QObject* parent )
	: QObject( parent )
{
	configHash = new ConfigHash();
	
	
	// Set default values
	configHash->insert( "rtsp_port", "5540" );
	
	configFileList = new QStringList();
	*configFileList << QDir::currentPath().append("/rtspproxy.conf")
				<< QDir::homePath().append("/.rtspproxy.conf")
				<< "/etc/rtspproxy.conf";
				
	// Read data from config files
	QStringListIterator it( *configFileList );
	while ( it.hasNext() ) {
		readFromFile( it.next() );
	}
}

Config::~Config()
{
	delete configHash;
}

void Config::readFromFile( const QString& fileName )
{
	qWarning() << "Reading config from" << fileName;
	QSettings settings( fileName, QSettings::IniFormat );
	
	QStringList keys = settings.allKeys();
	
	QStringListIterator it( keys );
	while ( it.hasNext() ) {
		QString key = it.next();
		QString value = settings.value( key ).toString();
		
		configHash->insert( key, value );
	}
}

QStringList Config::getConfigFileList()
{
	return *configFileList;
}

qint32 Config::getIntValue( const QString& key )
{
	return configHash->value( key ).toInt();
}


