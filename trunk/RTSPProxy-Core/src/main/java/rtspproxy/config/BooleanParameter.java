/**
 * 
 */
package rtspproxy.config;

import org.apache.commons.configuration.Configuration;

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
    

    @Override
    public void readConfiguration( Configuration configuration )
    {
        boolean value = configuration.getBoolean( name );
        setObjectValue( value );
    }
}
