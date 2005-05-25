#ifndef _TIMEOUT_H_
#define _TIMEOUT_H_

/**
 * Wrap the timeout concept for sockets.
 */
class Timeout {
public:

	/** 
	 * Constructor. Construct a null timeout.
	 */
	Timeout() : m_sec(0), m_usec(0) {}

	/** 
	 * Construct a timeout with the given length in seconds and
	 * optionally the addition of the given microseconds.
	 * 
	 * @param sec Number of seconds for the timeout
	 * @param usec Optional microseconds time value
	 */
	Timeout( long sec, long usec=0 ) : m_sec(sec), m_usec(usec) {}


	/** 
	 * Sets the timeout seconds value.
	 * 
	 * @param sec Number of seconds
	 */
	void setSec( long sec ) { m_sec = sec; }
	
	/**
	 * Get the seconds field of the timeout.
	 * 
	 * @return The timeout's seconds field
	 */
	long getSec( ) const { return m_sec; }
	
	/** 
	 * Sets the timeout micro-seconds value.
	 * 
	 * @param sec Number of micro-seconds
	 */
	void setUSec( long usec ) { m_usec = usec; }
	
	/**
	 * Get the micro-seconds field of the timeout.
	 * 
	 * @return The timeout's micro-seconds field
	 */
	long getUSec( ) const { return m_usec; }

	/**
	 * Test to see if this timeout is valid or null.
	 *
	 * @return True if this timeout has been set with a value.
	 * @return False if this timeout is NULL (set to zero).
	 */
	operator bool( ) const { return m_sec || m_usec; }

private:
	long m_sec;
	long m_usec;
};

#endif				/* //_TIMEOUT_H_ */
