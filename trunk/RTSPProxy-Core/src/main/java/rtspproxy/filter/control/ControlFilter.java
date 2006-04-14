/**
 * 
 */
package rtspproxy.filter.control;

import java.util.List;

import org.apache.mina.common.IoSession;
import org.dom4j.Element;

import rtspproxy.filter.FilterBase;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public class ControlFilter extends FilterBase 
{	
	public static final String FilterNAME = "controlFilter";

	protected ControlProvider provider;
	
	/**
	 * @param filterName
	 * @param className
	 * @param typeName
	 */
	protected ControlFilter(String className, List<Element> configElements, String typeName) {
		super(FilterNAME, className, typeName);
		
		this.provider = (ControlProvider)loadConfigInitProvider(className, ControlProvider.class, configElements);
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.common.IoFilterAdapter#sessionClosed(org.apache.mina.common.IoFilter.NextFilter, org.apache.mina.common.IoSession)
	 */
	@Override
	public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
		if ( provider != null  && isRunning())
			provider.sessionClosed( session );

		nextFilter.sessionClosed(session);
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.common.IoFilterAdapter#sessionOpened(org.apache.mina.common.IoFilter.NextFilter, org.apache.mina.common.IoSession)
	 */
	@Override
	public void sessionOpened(NextFilter nextFilter, IoSession session) throws Exception {
		if ( provider != null && isRunning() )
			provider.sessionOpened( session );

		nextFilter.sessionOpened(session);
	}

}
