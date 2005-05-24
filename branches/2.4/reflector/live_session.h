#ifndef _LIVE_SESSION_H_ 
#define _LIVE_SESSION_H_

/** 
 * A LiveSession manages an ingoing RTP/RTCP stream and forward it
 * to all the client that partecipate at the session.
 */
class LiveSession
{
public:
	LiveSession();
	~LiveSession();
	
private:
};

#endif // _LIVE_SESSION_H_
