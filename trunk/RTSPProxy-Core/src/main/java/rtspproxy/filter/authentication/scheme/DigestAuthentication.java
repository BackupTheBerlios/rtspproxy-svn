package rtspproxy.filter.authentication.scheme;

import static rtspproxy.lib.StringUtil.getStringMap;
import static rtspproxy.lib.StringUtil.quote;
import static rtspproxy.lib.StringUtil.toByteArray;
import static rtspproxy.lib.StringUtil.toHexString;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.RtspService;
import rtspproxy.lib.number.UnsignedLong;
import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;

public class DigestAuthentication implements AuthenticationScheme
{

	private static Logger log = LoggerFactory.getLogger( DigestAuthentication.class );

	/** This is the value of the validity of a challenge response. */
	private static final int NONCE_TIMEOUT = 60 * 5; // 5 minutes

	private UnsignedLong privateKey;

	private String realm;

	private MessageDigest md5;

	public DigestAuthentication()
	{
		// Generate the private key
		Random random = new Random();
		privateKey = new UnsignedLong( random );

		try {
			md5 = MessageDigest.getInstance( "MD5" );
		} catch ( NoSuchAlgorithmException e ) {
		}

		// Initiazialize the realm string
		realm = "realm="
				+ quote( "RtspProxy@"
						+ RtspService.getInstance().getAddress().getHostAddress() );
	}

	public String getName()
	{
		return "Digest";
	}

	public Credentials getCredentials( RtspMessage message )
	{
		String authString = message.getHeader( "Proxy-Authorization" );
		authString = authString.split( " " )[1];
		Map<String, String> params = getStringMap( authString );
		if ( params == null )
			return null;

		String username = params.get( "username" );
		String response = params.get( "response" );
		String realm = params.get( "realm" );
		String nonce = params.get( "nonce" );
		String uri = params.get( "uri" );
		String cnonce = params.get( "cnonce" );
		String nc = params.get( "nc" );
		if ( username == null || response == null || realm == null || nonce == null
				|| uri == null || cnonce == null || nc == null )
			return null;

		// Check the validity of the nonce
		String sTimeStamp = nonce.substring( 0, 16 );
		long timestamp = Long.valueOf( sTimeStamp, 16 );
		long current = System.currentTimeMillis();
		long diff = current - timestamp;
		log.debug( "Time diff: " + ((float) diff / 1000 ) + " sec" );
		if ( diff < 0 || diff > NONCE_TIMEOUT * 1000 )
			// Timeout excedeed
			return null;

		DigestCredentials credentials = new DigestCredentials();
		credentials.setUserName( username );
		credentials.setResponse( response );
		credentials.setRealm( realm );
		credentials.setNonce( nonce );
		credentials.setUri( uri );
		credentials.setMethod( ( (RtspRequest) message ).getVerbString() );
		credentials.setCnonce( cnonce );
		credentials.setNc( nc );

		return credentials;
	}

	public String getChallenge()
	{
		StringBuilder challenge = new StringBuilder();
		challenge.append( realm ).append( "," );
		challenge.append( "nonce=" ).append( quote( newNonce() ) ).append( "," );
		challenge.append( "qop=auth," );
		challenge.append( "algorithm=" ).append( quote("MD5") );
		return challenge.toString();
	}

	/**
	 * Generate a new nonce, defined as
	 * <code>time-stamp H(time-stamp ":" private-key)</code>
	 * 
	 * encoded in Base64.
	 * 
	 * @return the nonce
	 */
	private String newNonce()
	{
		UnsignedLong timestamp = new UnsignedLong( System.currentTimeMillis() );

		byte[] firstPart = null;
		synchronized ( md5 ) {
			md5.update( timestamp.getBytes() );
			md5.update( (byte) ':' );
			md5.update( privateKey.getBytes() );
			firstPart = md5.digest();
		}

		return toHexString( timestamp.getBytes() ) + toHexString( firstPart );
	}

	public boolean computeAuthentication( Credentials credentials, String storedPassword )
	{
		if ( !( credentials instanceof DigestCredentials ) )
			return false;

		// response = KD ( H(A1), unq(nonce-value) ":" H(A2) )
		// A1 = unq(username-value) ":" unq(realm-value) ":" passwd
		// A2 = Method ":" digest-uri-value

		DigestCredentials creds = (DigestCredentials) credentials;
		String A1 = creds.getUserName() + ":" + creds.getRealm() + ":" + storedPassword;
		String A2 = creds.getMethod() + ":" + creds.getUri();

		byte[] response;
		synchronized ( md5 ) {
			md5.update( toByteArray( A1 ) );
			byte[] HA1 = md5.digest();
			md5.update( toByteArray( A2 ) );
			byte[] HA2 = md5.digest();

			String sHA1 = toHexString( HA1 );
			String sHA2 = toHexString( HA2 );

			md5.update( toByteArray( sHA1 ) );
			md5.update( (byte) ':' );
			md5.update( toByteArray( creds.getNonce() ) );
			md5.update( (byte) ':' );
			md5.update( toByteArray( creds.getNc() ) );
			md5.update( (byte) ':' );
			md5.update( toByteArray( creds.getCnonce() ) );
			md5.update( (byte) ':' );
			md5.update( toByteArray( "auth" ) );
			md5.update( (byte) ':' );
			md5.update( toByteArray( sHA2 ) );
			response = md5.digest();
		}

		String expectedResponse = toHexString( response );
		log.debug( "Expected: " + expectedResponse );
		log.debug( "Got:      " + creds.getResponse() );
		return expectedResponse.equals( creds.getResponse() );
	}

	/**
	 * Specialized credentials class that holds all the relevant digest data.
	 */
	private static class DigestCredentials extends Credentials
	{

		private String response;

		private String realm;

		private String nonce;

		private String uri;

		private String method;

		private String cnonce;

		private String nc;

		/**
		 * @return Returns the nonce.
		 */
		public String getNonce()
		{
			return nonce;
		}

		/**
		 * @param nonce
		 *            The nonce to set.
		 */
		public void setNonce( String nonce )
		{
			this.nonce = nonce;
		}

		/**
		 * @return Returns the realm.
		 */
		public String getRealm()
		{
			return realm;
		}

		/**
		 * @param realm
		 *            The realm to set.
		 */
		public void setRealm( String realm )
		{
			this.realm = realm;
		}

		/**
		 * @return Returns the response.
		 */
		public String getResponse()
		{
			return response;
		}

		/**
		 * @param response
		 *            The response to set.
		 */
		public void setResponse( String response )
		{
			this.response = response;
		}

		/**
		 * @return Returns the uri.
		 */
		public String getUri()
		{
			return uri;
		}

		/**
		 * @param uri
		 *            The uri to set.
		 */
		public void setUri( String uri )
		{
			this.uri = uri;
		}

		/**
		 * @return Returns the method.
		 */
		public String getMethod()
		{
			return method;
		}

		/**
		 * @param method
		 *            The method to set.
		 */
		public void setMethod( String method )
		{
			this.method = method;
		}

		/**
		 * @return Returns the cnonce.
		 */
		public String getCnonce()
		{
			return cnonce;
		}

		/**
		 * @param cnonce
		 *            The cnonce to set.
		 */
		public void setCnonce( String cnonce )
		{
			this.cnonce = cnonce;
		}

		/**
		 * @return Returns the nc.
		 */
		public String getNc()
		{
			return nc;
		}

		/**
		 * @param nc
		 *            The nc to set.
		 */
		public void setNc( String nc )
		{
			this.nc = nc;
		}

		public String toString()
		{
			return "(" + userName + ":" + response + ")";
		}

	}
}
