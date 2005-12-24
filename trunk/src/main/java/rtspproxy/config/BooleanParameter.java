/**
 * 
 */
package rtspproxy.config;

/**
 * @author Matteo Merli
 */
public class BooleanParameter extends Parameter
{

	private Boolean value = null;

	private boolean defaultValue;

	public BooleanParameter( String name, boolean defaultValue, boolean mutable,
			String description )
	{
		super( name, mutable, description );
		this.defaultValue = defaultValue;
	}

	public void setValue( String value ) throws IllegalArgumentException
	{
		value = value.trim().toLowerCase();
		if ( "true".equals( value ) || "yes".equals( value ) )
			this.value = true;
		else if ( "false".equals( value ) || "no".equals( value ) )
			this.value = false;
		else
			throw new IllegalArgumentException( "Boolean value not valid: " + value );

		setChanged();
	}

	/**
	 * @return Returns the defaultValue.
	 */
	public String getDefaultValue()
	{
		return defaultValue ? "true" : "false";
	}

	@Override
	public String getStringValue()
	{
		return getValue() ? "true" : "false";
	}

	@Override
	public String getType()
	{
		return "java.lang.Boolean";
	}

	public void setValue( boolean value )
	{
		this.value = value;
	}

	public boolean getValue()
	{
		return value == null ? defaultValue : value.booleanValue();
	}

	@Override
	public Object getObjectValue()
	{
		return value == null ? defaultValue : value;
	}

	@Override
	public void setObjectValue( Object object )
	{
		if ( !(object instanceof Boolean) )
			throw new IllegalArgumentException( "Value must be a Boolean" );

		if ( !object.equals( getObjectValue() ) ) {
			this.value = (Boolean) object;
			setChanged();
		}
	}
}
