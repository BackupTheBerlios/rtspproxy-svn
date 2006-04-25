/**
 * 
 */
package rtspproxy.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 * 
 */
public class ListParameter<T extends ListElementParameter> extends Parameter
{

    private static Logger log = LoggerFactory.getLogger( ListParameter.class );

    private Class<T> parameterClass;

    private List<T> list = new ArrayList<T>();

    /**
     * @param name
     * @param mutable
     * @param description
     */
    public ListParameter( String name, boolean mutable, Class<T> parameterClass,
            String description )
    {
        super( name, mutable, description );

        this.parameterClass = parameterClass;
    }

    @Override
    public Object getObjectValue()
    {
        return list;
    }
    
    public List<T> getElementsList()
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
        boolean res;
        T element = null;
        String prefix;

        for ( int i = 0; /**/; i++ ) {
            
            try {
                element = parameterClass.newInstance();
            } catch ( Exception e ) {
                log.error( "Cannot instantiate class: {}", parameterClass.getName() );
                return;
            }
            
            prefix = name + "(" + i + ")";
            res = element.readConfiguration( configuration, prefix );

            if ( res == false )
                // end of list reached
                break;
            
            list.add( element );
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setObjectValue( Object object )
    {
        if ( !(object instanceof List) ) {
            throw new IllegalArgumentException( "Only accept a List parameter." );
        }

        list = (List) object;
    }
}
