/**
 * 
 */
package rtspproxy.config;


/**
 * @author Matteo Merli
 */
public class StringParameter extends Parameter
{

	private String value = null;

	private String defaultValue;

	public StringParameter( String name, String defaultValue, boolean mutable,
			String description )
	{
		super( name, mutable, description );
		this.defaultValue = defaultValue;
	}

	@Override
	public void setValue( String value ) throws IllegalArgumentException
	{
		this.value = value;
		setChanged();
	}

	@Override
	public String getStringValue()
	{
		return getValue();
	}

	@Override
	public String getType()
	{
		return "String";
	}

	public String getValue()
	{
		return value == null ? defaultValue : value;
	}

	/**
	 * @return Returns the defaultValue.
	 */
	public String getDefaultValue()
	{
		return defaultValue;
	}

}
