/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   Copyright (C) 2005 - Matteo Merli - matteo.merli@gmail.com            *
 *                                                                         *
 ***************************************************************************/

/*
 * $Id$
 * 
 * $URL$
 * 
 */

package rtspproxy.rtsp;

/**
 * State enumerator that indicates the reached state in the RTSP message
 * decoding process.
 */
enum ReadState {
    /** Unrecoverable error occurred */
    Failed,
    /** Trying to resync */
    Sync,
    /** Waiting for a command */
    Ready,
    /** Reading interleaved packet */
    Packet,
    /** Reading command (request or command line) */
    Command,
    /** Reading headers */
    Header,
    /** Reading body (entity) */
    Body,
    /** Fully formed message */
    Dispatch
}
