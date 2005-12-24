/**
 * 
 */
package rtspproxy.config;

import java.util.Observable;

/**
 * @author Matteo Merli
 */
public abstract class Parameter extends Observable
{

	protected final String name;

	protected final boolean mutable;

	protected final String description;

	protected Parameter( String name, boolean mutable, String description )
	{
		validateName( name );

		this.name = name;
		this.mutable = mutable;
		this.description = description;

		Config.addParameter( this );
	}

	/**
	 * Validate the name of the parameter. The name length must be > 0
	 */
	private void validateName( String name ) throws IllegalArgumentException
	{
		if ( (name == null) || (name.length() < 1) ) {
			throw new IllegalArgumentException(
					" A configuration parameter name can't be null or 0 length" );
		}
	}

	/**
	 * Changes the value of this parameter
	 * 
	 * @param value
	 * @throws IllegalArgumentException
	 */
	public abstract void setValue( String value ) throws IllegalArgumentException;

	/**
	 * @return a String representation of the value of this parameter.
	 */
	public abstract String getStringValue();

	public abstract Object getObjectValue();

	public abstract void setObjectValue( Object object );

	/**
	 * @return the name of this parameter
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return true if this parameter is "mutable" (so that it can be modified
	 *         at runtime)
	 */
	public boolean isMutable()
	{
		return mutable;
	}

	/**
	 * @return the description string of this parameter
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @return the string representation of the type for this parameter, such as
	 *         java.lang.String, java.lang.Integer...
	 */
	public abstract String getType();

	/**
	 * Marks the parameter as changed and notify all the Observers.
	 * 
	 * @see java.util.Observable#setChanged()
	 */
	@Override
	protected void setChanged()
	{
		super.setChanged();
		if ( mutable )
			notifyObservers();
	}

}