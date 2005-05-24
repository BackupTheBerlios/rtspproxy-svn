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
 
#ifndef _RTP_PACKET_H_
#define _RTP_PACKET_H_

#include <netinet/in.h>

#include "rtp.h"

#define RTP_HEADER_LEN 12

/*! This class is used to process incomings RTP packets and
 *  to create new packets.
 */
class RtpPacket
{
public:
	/*! Create a new RtpPacket using an existing
	 *  RTP packet.
	 *  @param	buf	pointer to the start of the packet
	 *  @param	size	size of the packet
	 */ 
	RtpPacket(void * buf, size_t size);
	
	/*! Create an empty RtpPacket.
	 */
	RtpPacket();
	
	/*! Destructor */
	~RtpPacket();
	
	/*! RTP Version. It _MUST_ be 2. */
	uint8_t version() {return m_header->version;}
	
	/*! If the padding bit is set, the packet contains one or more
	 *  additional padding octets at the end which are not part of the
	 *  payload.  The last octet of the padding contains a count of how
	 *  many padding octets should be ignored, including itself.  Padding
	 *  may be needed by some encryption algorithms with fixed block sizes
	 *  or for carrying several RTP packets in a lower-layer protocol data
	 *  unit. */
	bool padding() {return m_header->p;}
	
	/*! If the extension bit is set, the fixed header MUST be followed by
	 *  exactly one header extension, with a format defined in Section
	 *  5.3.1. */
	bool extension() {return m_header->x;}
	
	/*! The CSRC count contains the number of CSRC identifiers that follow
	 *  the fixed header. */
	uint8_t csrc_count() {return m_header->cc;}
	
	/*! Returns the marker. 
	 *  The interpretation of the marker is defined by a profile.  It is
	 *  intended to allow significant events such as frame boundaries to
	 *  be marked in the packet stream.  A profile MAY define additional
	 *  marker bits or specify that there is no marker bit by changing the
	 *  number of bits in the payload type field (see Section 5.3). */
	bool marker() {return m_header->m;}
	void set_marker(bool m) {m_header->m = m;}
	
	/*! Get the payload type. 
	 *  This field identifies the format of the RTP payload and determines
	 *  its interpretation by the application.  A profile MAY specify a
	 *  default static mapping of payload type codes to payload formats.
	 *  Additional payload type codes MAY be defined dynamically through
	 *  non-RTP means (see Section 3).  A set of default mappings for
	 *  audio and video is specified in the companion RFC 3551 [1].  An
	 *  RTP source MAY change the payload type during a session, but this
	 *  field SHOULD NOT be used for multiplexing separate media streams
	 *  (see Section 5.2). */
	uint8_t payload_type() {return m_header->pt;}
	void set_payload_type(uint8_t pt) {m_header->pt = pt;}
	
	/*! The sequence number increments by one for each RTP data packet
	 *  sent, and may be used by the receiver to detect packet loss and to
	 *  restore packet sequence.  The initial value of the sequence number
	 *  SHOULD be random (unpredictable) to make known-plaintext attacks
	 *  on encryption more difficult, even if the source itself does not
	 *  encrypt according to the method in Section 9.1, because the
	 *  packets may flow through a translator that does.*/
	uint16_t sequence() {return ntohs(m_header->seq);}
	void set_sequence(uint16_t seq) {m_header->seq = htons( seq );}
	
	/*! The timestamp reflects the sampling instant of the first octet in
	 *  the RTP data packet.  The sampling instant MUST be derived from a
	 *  clock that increments monotonically and linearly in time to allow
	 *  synchronization and jitter calculations (see Section 6.4.1). */
	uint32_t timestamp() {return ntohl(m_header->ts);}
	void set_timestamp(uint32_t ts) {m_header->ts = htonl(ts);}

	/*! The SSRC field identifies the synchronization source.  This
	 *  identifier SHOULD be chosen randomly, with the intent that no two
	 *  synchronization sources within the same RTP session will have the
	 *  same SSRC identifier. */
	uint32_t ssrc() {return ntohl(m_header->ssrc);}
	void set_ssrc(uint32_t ssrc) {m_header->ssrc = htonl(ssrc);}
	
	/*! The CSRC list identifies the contributing sources for the payload
	 *  contained in this packet.  The number of identifiers is given by
	 *  the CC field.  If there are more than 15 contributing sources,
	 *  only 15 can be identified. */
	uint32_t* csrc() {return m_header->csrc;}
	
	void set_payload(void *buf, uint32_t size); 
	
	/*! Returns a pointer to the beginning of data in the packet. */
	void* get_payload(); 
	
	/*! Return the lenght of data contained in the packet. 
	 *  This is not equal to the lenght of the packet.
	 */
	uint32_t get_payload_size();
	
	/*! Write the entire RTP packet in a buffer (already allocated)
	 *  and returns the size of the packet.
	 */
	uint32_t to_buffer(void *buf );
	
	/*! Returns the size of the entire packet. */
	uint32_t get_size() {return m_size; }
	
protected:
	/*! Pointer to the RTP packet. */
	rtp_header_t *m_header;
	
	/*! Packet size. */
	uint32_t m_size;
	
	/*! Pointer to data contained in the packet. */
	void *m_payload;
	
	/*! Size of data. */
	uint32_t m_payload_size;
	
	/*! Whether to delete or not the buffer. */ 
	bool m_delete;
	
};


#endif


/** LOG **
 *
 * $Log: rtp_packet.h,v $
 * Revision 1.3  2003/11/17 16:14:12  mat
 * make-up
 *
 *
 */

