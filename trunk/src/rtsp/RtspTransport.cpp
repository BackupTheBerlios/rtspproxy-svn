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
 