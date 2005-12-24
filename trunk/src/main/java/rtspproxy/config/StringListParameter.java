/**
 * 
 */
package rtspproxy.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matteo Merli
 */
public class StringListParameter extends Parameter
{
	private List<String> values = null;

	private String defaultValue;

	public StringListParameter( String name, String defaultValue, boolean mutable,
			String description )
	{
		super( name, mutable, description );

		if ( defaultValue == null )
			throw new IllegalArgumentException( "Default value for " + name
					+ " must be not null." );
		this.defaultValue = defaultValue;
	}

	@Override
	public void setValue( String value ) throws IllegalArgumentException
	{
		this.values = new ArrayList<String>();
		String[] tokens = value.split( "," );

		for ( String token : tokens ) {
			this.values.add( token );
		}
		setChanged();
	}

	@Override
	public String getStringValue()
	{
		if ( values == null )
			return defaultValue.toString();

		StringBuilder sb = new StringBuilder();
		final int size = values.size();
		for ( int i = 0; i < size; i++ ) {
			sb.append( values.get( i ) );
			if ( i < size - 1 )
				sb.append( ", " );
		}
		return sb.toString();
	}

	/**
	 * @return Returns the defaultValue.
	 */
	public String getDefaultValue()
	{
		return defaultValue;
	}

	public String[] getValue()
	{
		if ( values == null )
			return new String[] { defaultValue };

		String[] v = new String[values.size()];
		for ( int i = values.size() - 1; i >= 0; i-- )
			v[i] = values.get( i );
		return v;
	}

	@Override
	public String getType()
	{
		return "[Ljava.lang.String";
	}

	@Override
	public Object getObjectValue()
	{
		return getValue();
	}

	@Override
	public void setObjectValue( Object object )
	{
		if ( !(object instanceof String[]) )
			throw new IllegalArgumentException( "Value must be a String[]" );

		this.values = new ArrayList<String>( ((String[]) object).length );
		for ( String s : (String[]) object )
			values.add( s );

		setChanged();
	}

}
