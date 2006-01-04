package rtspproxy.lib;

import java.util.HashMap;
import java.util.Map;

/**
 * Random collection of strings utility function.
 * 
 * @author Matteo Merli
 */
public class StringUtil
{

	/**
	 * Return a the passed string double quoted. Eg: <code>
	 * String s = "test";
	 * StringUtil.quote( s ) == "\"test\""
	 * </code>
	 * 
	 * @param str
	 *            the string to be quoted
	 * @return the quoted string
	 */
	public static String quote( String str )
	{
		return "\"" + str + "\"";
	}

	/**
	 * Remove the quotation marks from the string
	 * 
	 * @param str
	 *            a quoted string
	 * @return the string unquoted
	 */
	public static String unquote( String str )
	{
		if ( str.charAt( 0 ) == '"' )
			str = str.substring( 1 );
		if ( str.charAt( str.length() - 1 ) == '"' )
			str = str.substring( 0, str.length() - 1 );
		return str;
	}

	public static String toString( byte[] bytes )
	{
		StringBuilder sb = new StringBuilder();
		for ( byte b : bytes )
			sb.append( (char) b );
		return sb.toString();
	}
	
	public static String toHexString( byte[] bytes )
	{
		StringBuilder sb = new StringBuilder();
		for ( byte b : bytes )
			sb.append( hexLetters[(byte) ((b >> 4) & 0x0F)] ).append(
					hexLetters[b & 0x0F] );
		if ( sb.length() == 0 )
			return "0";
		return sb.toString();
	}

	public static byte[] toByteArray( String str )
	{
		byte[] bytes = new byte[str.length()];
		for ( int i = str.length() - 1; i >= 0; i-- )
			bytes[i] = (byte) str.charAt( i );
		return bytes;
	}

	/**
	 * Transforms a comma separated couples of key-values into a string Map (key ->
	 * value) eg: key1:"value1",key2="value2",....
	 * 
	 * @param values
	 * @return
	 */
	public static Map<String, String> getStringMap( String values )
	{
		Map<String, String> map = new HashMap<String, String>();
		String key, value;

		try {
			for ( String tuple : values.split( "," ) ) {
				key = tuple.split( "=" )[0];
				value = unquote( tuple.split( "=" )[1] );
				map.put( key, value );
			}
		} catch ( Exception e ) {
			return null;
		}
		return map;
	}

	protected static final char[] hexLetters = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

}
