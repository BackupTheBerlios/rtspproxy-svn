/**************************************************************************
 *   Copyright (C) 2003 Matteo Merli <matteo.merli@studenti.unipr.it>
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
 *****************************************************************************/

#ifndef CACHE_SEGMENT_H
#define CACHE_SEGMENT_H

#include <types.h>
#include <stdio.h>
#include <pthread.h>

#include "tlist.h"

class CacheItem;
class TimeRange;

/*!
 * Data structure of cached packets.
 */
typedef struct s_packet
{
	/*! Position of the packet in the file. */
	uint32_t offset;
	
         /*! Size of the packet. */
	uint16_t size;

	/*! Time Stamp.  */
	uint32_t timestamp;
} packet_t;

/*! Simply concatenated list of packets. */
typedef TSingleList < packet_t * > packet_list_t;


/*! This structure holds the information parsed
 *  on the SDP description returned by the server..
 */
typedef struct _sdp_info {
	char *range;
	char *length;
	char *mimetype;

	uint8_t payload_type;
	uint32_t rtp_clock;
	uint32_t avg_bitrate;

	uint8_t track_id;
} sdp_info_t;

typedef TSingleList < sdp_info_t* > sdp_info_list_t;


/**
 * The class CacheSegment represent the abstraction of a cached stream
 * segment on disk.
 *
 * There are functions to add packets to the segment.
 * Functions to get next packet.
 * There is a list with all the sizes of cached packets.
 */
class CacheSegment
{

 public:
	/*! Constructor */
	CacheSegment(CacheItem *parent);
	/*! Destructor */
	~CacheSegment();

	/*! Adds a packet to the stream segment.
	 * @param buf   Pointer to the buffer
	 * @param size  Size of the buffer
	 */
	void add_packet( const void *buf, uint16_t size, 
			bool adjust_ts=false, bool set_offset=false );
	
	void open(bool truncate);
	
	/*! Close the segment and free resources... 
	 */
	void close();

	/*!
	 * Returns the size of the whole segment.
	 */
	uint32_t get_size() {return m_size;}

	/*!
	 * Number of packets in segment.
	 */
	uint16_t get_num_packets() {return m_num_packets;}
	
	/*! Returns a new file stream associated with the segment. */
	FILE* get_file_desc();
	
	const char* get_file_name() {return (const char*)m_file_name; }

	/*!
	 * Returns a pointer to the CacheItem parent object..
	 */
	CacheItem *parent() {return m_parent;}

	/*! Returns a pointer to the TimeRange object. */
	TimeRange *time_range();

	/*!
	 * Set the type of the packet that the segment will contain.
	 * @param type can be TRANSPORT_RTP, TRANSPORT_RDT ...
	 */
	void set_transport_type(uint8_t type);

	/*! Returns the type of used transport. */
	uint8_t get_transport_type() {return m_transport_type;}
	
	/*! Returns a reference to the private packet list. */
	packet_list_t* get_packet_list() {return m_packet_list;}
	
	void setSDPInfo( sdp_info_t* sdp );
	sdp_info_t* getSDPInfo() { return &m_sdp_info; }

	/*! Signal handler, for the ALARM signal.. */
	void handler(int sig);

	/*! Update the bandwidth guess. */
	void update_bandwidth_data();
	
	uint32_t get_guessed_bitrate() { return m_guessed_bitrate; }
	
	void lock() { pthread_mutex_lock( &m_mutex ); }
	
	void unlock() { pthread_mutex_unlock( &m_mutex ); }

 private:
	/*! This list contains all the sizes of the cached packets. */
	packet_list_t * m_packet_list;

	/*! Total size of the segment. */
	uint32_t m_size;

	/*! Number of packets in segment. */
	uint16_t m_num_packets;

	/*! File descriptor associated with the segment when writing. */
	FILE* m_fd;

	/*! Path of the file associated with the segment. */
	char m_file_name[100];

	/*! CacheItem object which contains this CacheSegment. */
	CacheItem * m_parent;

	/*! TimeRange that describe the range included in this segment. */
	TimeRange *m_time_range;

	/*! Transport type of the stored packets. */
	uint8_t m_transport_type;

	/*! SDP information associated with the segment. */
	sdp_info_t m_sdp_info;

	/*! */
	uint32_t m_guessed_bitrate;

	/*! */
	pthread_t m_thread;
	
	pthread_mutex_t m_mutex;
	
	/*! */
	bool m_first_time;
	
	uint32_t m_prev_offset;
	
	uint32_t m_prev_timestamp;
	
	uint32_t m_ts_offset;
	
};

typedef TDoubleList < CacheSegment *>CacheSegmentList;


#endif

/** LOG **
 *
 * $Log: cache_segment.h,v $
 * Revision 1.3  2003/11/17 16:13:45  mat
 * make-up
 *
 *
 */

