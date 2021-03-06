/**
 * 
 */
package rtspproxy.config;

import org.apache.commons.configuration.Configuration;

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
		return "java.lang.Integer";
	}

	@Override
	public Object getObjectValue()
	{
		return value == null ? defaultValue : value;
	}

	@Override
	public void setObjectValue( Object object )
	{
		if ( !(object instanceof Integer) )
			throw new IllegalArgumentException( "Value must be a Integer" );

		Integer intVal = (Integer) object;

		if ( minValue != null && (intVal.compareTo( minValue ) < 0) )
			throw new IllegalArgumentException( "Integer value for " + name
					+ " must be greater than " + minValue );
		if ( maxValue != null && (intVal.compareTo( maxValue ) > 0) )
			throw new IllegalArgumentException( "Integer value for " + name
					+ " must be lesser than " + maxValue );

		if ( !intVal.equals( getObjectValue() ) ) {
			// Only notify if the value is different
			this.value = intVal;
			setChanged();
		}
	}
    
    @Override
    public void readConfiguration( Configuration configuration )
    {
        int value = configuration.getInt( name );
        setObjectValue( value );
    }

}
