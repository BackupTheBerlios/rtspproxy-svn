/**
 * 
 */
package rtspproxy.filter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.Reactor;
import rtspproxy.config.AAAConfig;
import rtspproxy.config.Config;
import rtspproxy.filter.accounting.AccountingFilter;
import rtspproxy.filter.authentication.AuthenticationFilter;
import rtspproxy.filter.ipaddress.IpAddressFilter;
import rtspproxy.jmx.JmxAgent;
import rtspproxy.lib.Side;
import rtspproxy.lib.Singleton;

/**
 * Filter registry. This registry is populated from the configuration on reactor startup
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 */
public class FilterRegistry extends Singleton {

	private static Logger logger = LoggerFactory.getLogger(FilterRegistry.class);

	// client side address filters
	private LinkedList<IpAddressFilter> clientAddressFilters = new LinkedList<IpAddressFilter>();
	
	// server side address filters
	private LinkedList<IpAddressFilter> serverAddressFilters = new LinkedList<IpAddressFilter>();

	// client side authentication filters
	private LinkedList<AuthenticationFilter> clientAuthenticationFilters = new LinkedList<AuthenticationFilter>();
	
	// client side accounting filter
	private LinkedList<AccountingFilter> clientAccountingFilters = new LinkedList<AccountingFilter>();
	
	// server side accounting filter
	private LinkedList<AccountingFilter> serverAccountingFilters = new LinkedList<AccountingFilter>();
	
	/**
	 * 
	 */
	public FilterRegistry() {
	}

	/**
	 * get the active registry instance
	 */
	public static FilterRegistry getInstance() {
		return (FilterRegistry)Singleton.getInstance(FilterRegistry.class);
	}
	
	// flag to determine if already populated
	private boolean populated = false;
	
	/**
	 * populate from configuration
	 */
	public void populateRegistry() {
		if(this.populated) {
			logger.debug("filter registry already populated");
			return;
		}
		
		try {
			for(AAAConfig filterConfig : Config.getIpAddressFilters()) {
				IpAddressFilter ipAddressFilter = new IpAddressFilter(filterConfig.getImplClass(), 
						filterConfig.getConfigElements());
				
				ipAddressFilter.setSide(filterConfig.getSide());
				registerFilterMBean(ipAddressFilter);

				if(filterConfig.getSide() == Side.Client) {
					this.clientAddressFilters.add(ipAddressFilter);
				} else if(filterConfig.getSide() == Side.Server) {
					this.serverAddressFilters.add(ipAddressFilter);
				} else {
					this.clientAddressFilters.add(ipAddressFilter);
					this.serverAddressFilters.add(ipAddressFilter);
				}
			}
			
			for(AAAConfig filterConfig : Config.getAuthenticationFilters()) {
				if(filterConfig.getSide() == Side.Client) {
					AuthenticationFilter authFilter = new AuthenticationFilter(filterConfig.getImplClass(), 
							filterConfig.getAttribute("scheme", "Basic"),
							filterConfig.getConfigElements());
					
					authFilter.setSide(filterConfig.getSide());
					registerFilterMBean(authFilter);

					this.clientAuthenticationFilters.add(authFilter);
				}
			}
			
			for(AAAConfig filterConfig : Config.getAccountingFilters()) {
				AccountingFilter accountingFilter = new AccountingFilter(filterConfig.getImplClass(), 
						filterConfig.getConfigElements());
				
				accountingFilter.setSide(filterConfig.getSide());
				registerFilterMBean(accountingFilter);

				if(filterConfig.getSide() == Side.Client) {
					this.clientAccountingFilters.add(accountingFilter);
				} else if(filterConfig.getSide() == Side.Server) {
					this.serverAccountingFilters.add(accountingFilter);
				} else {
					this.clientAccountingFilters.add(accountingFilter);
					this.serverAccountingFilters.add(accountingFilter);
				}
			}

		} catch (Throwable t) {
			logger.error("failed to populate filter registry", t);
			
			Reactor.stop();
			System.exit(-1);
		}
		
		this.populated = true;
	}
	
	private void registerFilterMBean(FilterBase filter) {
		if(Config.proxyManagementEnable.getValue())
			JmxAgent.getInstance().registerFilter(filter);
	}

	/**
	 * @return Returns the clientAddressFilters.
	 */
	public List<IpAddressFilter> getClientAddressFilters() {
		return Collections.unmodifiableList(clientAddressFilters);
	}

	/**
	 * @return Returns the serverAddressFilters.
	 */
	public List<IpAddressFilter> getServerAddressFilters() {
		return Collections.unmodifiableList(serverAddressFilters);
	}

	/**
	 * @return Returns the clientAuthenticationFilters.
	 */
	public List<AuthenticationFilter> getClientAuthenticationFilters() {
		return Collections.unmodifiableList(clientAuthenticationFilters);
	}

	/**
	 * @return Returns the clientAccountingFilters.
	 */
	public LinkedList<AccountingFilter> getClientAccountingFilters() {
		return clientAccountingFilters;
	}

	/**
	 * @return Returns the serverAccountingFilters.
	 */
	public LinkedList<AccountingFilter> getServerAccountingFilters() {
		return serverAccountingFilters;
	}
	
}
