/**
 * 
 */
package rtspproxy.config;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 * 
 */
public class ListParameter<T> extends Parameter
{

    private static Logger log = LoggerFactory.getLogger( ListParameter.class );

    private List<T> list;

    /**
     * @param name
     * @param mutable
     * @param description
     */
    public ListParameter( String name, boolean mutable, String description )
    {
        super( name, mutable, description );
    }

    @Override
    public Object getObjectValue()
    {
        return list;
    }

    @Override
    public String getStringValue()
    {
        return list.toString();
    }

    @Override
    public String getType()
    {
        return "java.util.List";
    }

    @Override
    public void readConfiguration( Configuration configuration )
    {
        List elements = configuration.getList( name );
        if ( elements == null ) {
            log.debug( "Elements not found for key '{}'", name );
            return;
        }

        for ( Object element : elements ) {
            log.info( "ELEMENT: {}", element );
        }
    }

    @Override
    public void setObjectValue( Object object )
    {
        if ( !(object instanceof List) ) {
            throw new IllegalArgumentException( "Only accept a List parameter." );
        }
        
        list = (List<T>)object;
    }
}
