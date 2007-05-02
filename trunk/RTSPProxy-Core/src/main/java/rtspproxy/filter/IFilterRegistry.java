package rtspproxy.filter;

import com.google.inject.ImplementedBy;

import rtspproxy.filter.accounting.AccountingFilter;
import rtspproxy.filter.authentication.AuthenticationFilter;
import rtspproxy.filter.ipaddress.IpAddressFilter;
import rtspproxy.filter.rewrite.UrlRewritingFilter;

@ImplementedBy(FilterRegistry.class)
public interface IFilterRegistry
{
    
    /**
     * populate from configuration
     */
    public void populateRegistry();
    
    /**
     * @return the accountingFilter
     */
    public AccountingFilter getAccountingFilter();
    
    /**
     * @return the addressFilter
     */
    public IpAddressFilter getClientAddressFilter();
    
    /**
     * @return the server address filter
     */
    public IpAddressFilter getServerAddressFilter();
    
    /**
     * @return the authenticationFilter
     */
    public AuthenticationFilter getAuthenticationFilter();
    
    /**
     * @return the rewritingFilter
     */
    public UrlRewritingFilter getClientRewritingFilter();
    
    /**
     * @return the rewritingFilter
     */
    public UrlRewritingFilter getServerRewritingFilter();
    
}