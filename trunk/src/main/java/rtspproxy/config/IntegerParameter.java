/**
 * 
 */
package rtspproxy.config;

/**
 * @author Matteo Merli
 */
public class IntegerParameter extends Parameter
{
	private Integer value = null;

	private Integer minValue;

	private Integer maxValue;

	private Integer defaultValue;

	public IntegerParameter( String name, Integer minValue, Integer maxValue,
			Integer defaultValue, boolean mutable, String description )
	{
		super( name, mutable, description );

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

		Integer tmpValue;
		try {
			tmpValue = Integer.valueOf( value );
		} catch ( NumberFormatException nfe ) {
			throw new IllegalArgumentException( "Integer value for " + name
					+ "not valid: " + value );
		}

		if ( minValue != null && (tmpValue.compareTo( minValue ) < 0) )
			throw new IllegalArgumentException( "Integer value for " + name
					+ " must be greater than " + minValue );
		if ( maxValue != null && (tmpValue.compareTo( maxValue ) > 0) )
			throw new IllegalArgumentException( "Integer value for " + name
					+ " must be lesser than " + maxValue );

		this.value = tmpValue;
		setChanged();
	}

	@Override
	public String getStringValue()
	{
		return (value != null ? value : defaultValue).toString();
	}

	/**
	 * @return Returns the defaultValue.
	 */
	public String getDefaultValue()
	{
		return defaultValue.toString();
	}

	public int getValue()
	{
		return value == null ? defaultValue.intValue() : value.intValue();
	}

	@Override
	public String getType()
	{
		return "Integer";
	}
}
