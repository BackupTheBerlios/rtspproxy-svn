package rtspproxy;

import com.google.inject.ImplementedBy;

@ImplementedBy(Reactor.class)
public interface IReactor
{
    
    public void setStandalone( boolean standalone );
    
    /**
     * Constructor. Creates a new Reactor and starts it. The reactor relies on
     * configuration info that has to be provided <b>before</b> starting the
     * reactor.
     * 
     * @exception Exception
     *                reactor startup failed.
     */
    public void start() throws Exception;
    
    public void stop();
    
}