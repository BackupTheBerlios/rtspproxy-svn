/**
 * 
 */
package rtspproxy.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matteo Merli
 */
public class IntegerListParameter extends ListParameter
{
	private List<Integer> values = null;

	private Integer minValue;

	private Integer maxValue;

	private Integer defaultValue;

	public IntegerListParameter( String name, Integer minValue, Integer maxValue,
			Integer defaultValue, boolean mutable, String description, String xpathExpr )
	{
		super( name, mutable, description, xpathExpr );

		if ( defaultValue == null )
			throw new IllegalArgumentException( "Default value for " + name
					+ " must be not null." );
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	public void setValue( String value ) throws IllegalArgumentException
	{
		this.values = new ArrayList<Integer>();
		Integer tmpValue = null;
		String[] tokens = value.split( "," );

		for ( String token : tokens ) {

			token = token.trim();
			try {
				tmpValue = Integer.valueOf( token );
			} catch ( NumberFormatException nfe ) {
				throw new IllegalArgumentException( "Integer value for " + name
						+ " not valid: " + token );
			}

			if ( minValue != null && (tmpValue.compareTo( minValue ) < 0) )
				throw new IllegalArgumentException( "Integer value for " + name
						+ " must be greater than " + minValue );
			if ( maxValue != null && (tmpValue.compareTo( maxValue ) > 0) )
				throw new IllegalArgumentException( "Integer value for " + name
						+ " must be lesser than " + maxValue );

			this.values.add( tmpValue );
		}

		setChanged();
	}

	@Override	
	public void addValue(String value) {
		try {
			Integer tmpValue = Integer.valueOf(value);

			this.values.add(tmpValue);
		} catch ( NumberFormatException nfe ) {
			throw new IllegalArgumentException( "Integer value for " + name
					+ " not valid: " + value );
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
			sb.append( values.get( i ).toString() );
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
		return defaultValue.toString();
	}

	public int[] getValue()
	{
		if ( values == null )
			return new int[] { defaultValue };

		int[] v = new int[values.size()];
		for ( int i = values.size() - 1; i >= 0; i-- )
			v[i] = values.get( i );
		return v;
	}

	@Override
	public String getType()
	{
		return "[I";
	}

	@Override
	public Object getObjectValue()
	{
		return getValue();
	}

	@Override
	public void setObjectValue( Object object )
	{
		if ( !(object instanceof int[]) )
			throw new IllegalArgumentException( "Value must be a int[]" );
		
		this.values = new ArrayList<Integer>( ((int[]) object).length );
		for ( int v : (int[]) object )
			values.add( v );

		setChanged();
	}

}
