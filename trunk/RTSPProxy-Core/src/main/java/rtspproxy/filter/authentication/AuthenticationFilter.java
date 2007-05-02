/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   Copyright (C) 2005 - Matteo Merli - matteo.merli@gmail.com            *
 *                                                                         *
 ***************************************************************************/

/*
 * $Id$
 * 
 * $URL$
 * 
 */

package rtspproxy.filter.authentication;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import rtspproxy.IReactor;
import rtspproxy.Reactor;
import rtspproxy.config.Config;
import rtspproxy.filter.FilterBase;
import rtspproxy.filter.authentication.scheme.AuthenticationScheme;
import rtspproxy.filter.authentication.scheme.BasicAuthentication;
import rtspproxy.filter.authentication.scheme.Credentials;
import rtspproxy.filter.authentication.scheme.DigestAuthentication;
import rtspproxy.rtsp.RtspCode;
import rtspproxy.rtsp.RtspMessage;
import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;

/**
 * @author Matteo Merli
 */
public class AuthenticationFilter extends FilterBase<AuthenticationProvider>
{
    
    private static Logger log =
            LoggerFactory.getLogger( AuthenticationFilter.class );
    
    private static final String FilterNAME = "authenticationFilter";
    
    private static final String ATTR =
            AuthenticationFilter.class.getName() + "Attr";
    
    private static final Map<String, Class<? extends AuthenticationScheme>> schemeRegistry =
            new HashMap<String, Class<? extends AuthenticationScheme>>();
    
    @Inject
    private IReactor reactor;
    
    static
    {
        // Fill in known schemes
        schemeRegistry.put( "basic", BasicAuthentication.class );
        schemeRegistry.put( "digest", DigestAuthentication.class );
    }
    
    /** Backend provider. */
    private AuthenticationProvider provider;
    
    /** Different authentication schemes implementation */
    private AuthenticationScheme scheme = null;
    
    public static String getAttrName()
    {
        return ATTR;
    }
    
    /**
     * Construct a new AuthenticationFilter. Looks at the configuration to load
     * the choseen backend implementation.
     */
    public AuthenticationFilter()
    {
        // Validate the choosen authentication scheme
        String schemeName = Config.filtersAuthenticationScheme.getValue();
        Class<? extends AuthenticationScheme> schemeClass =
                schemeRegistry.get( schemeName.toLowerCase() );
        if ( schemeClass == null )
        {
            // scheme not found
            log.error(
                       "Authentication Scheme not found: {}. Valid values are: {}",
                       schemeName, schemeRegistry.keySet() );
            reactor.stop();
            return;
        }
        
        // Instanciate the selected scheme
        try
        {
            scheme = schemeClass.newInstance();
        } catch ( Exception e )
        {
            log.error( "Error instanciating class: {}", schemeClass );
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.FilterBase#getName()
     */
    @Override
    public String getName()
    {
        return FilterNAME;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.FilterBase#getProviderClassName()
     */
    @Override
    public String getProviderClassName()
    {
        return Config.filtersAuthenticationImplClass.getValue();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.FilterBase#getProviderInterface()
     */
    @Override
    protected Class<AuthenticationProvider> getProviderInterface()
    {
        return AuthenticationProvider.class;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see rtspproxy.filter.FilterBase#setProvider(rtspproxy.filter.GenericProvider)
     */
    @Override
    protected void setProvider( AuthenticationProvider provider )
    {
        this.provider = provider;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.mina.common.IoFilterAdapter#messageReceived(org.apache.mina.common.IoFilter.NextFilter,
     *      org.apache.mina.common.IoSession, java.lang.Object)
     */
    @Override
    public void messageReceived( NextFilter nextFilter, IoSession session,
            Object message ) throws Exception
    {
        if ( !(message instanceof RtspRequest) )
        {
            // Shouldn't happen
            log.warn( "Object message is not a RTSP request" );
            return;
        }
        
        if ( session.getAttribute( ATTR ) != null )
        {
            // Client already autheticated
            log.debug( "Already authenticaed: {}", session.getAttribute( ATTR ) );
            nextFilter.messageReceived( session, message );
        }
        
        String authString =
                ((RtspMessage) message).getHeader( "Proxy-Authorization" );
        
        if ( authString == null )
        {
            log.debug( "RTSP message: \n{}", message );
            final RtspResponse response =
                    RtspResponse.errorResponse( RtspCode.ProxyAuthenticationRequired );
            
            response.setHeader( "Proxy-Authenticate", scheme.getName() + " "
                    + scheme.getChallenge() );
            
            log.debug( "Client MUST athenticate to Proxy: \n{}", response );
            session.write( response );
            return;
        }
        
        if ( !validateAuthenticationScheme( authString ) )
        {
            log.debug( "Authentication scheme not valid: {}", authString );
            RtspResponse response =
                    RtspResponse.errorResponse( RtspCode.BadRequest );
            session.write( response );
            return;
        }
        
        log.debug( "RTSP message: \n{}", message );
        
        // Check the authentication credentials
        final Credentials credentials =
                scheme.getCredentials( (RtspMessage) message );
        
        boolean authenticationOk = false;
        if ( credentials != null )
        {
            String password = provider.getPassword( credentials.getUserName() );
            if ( password != null
                    && scheme.computeAuthentication( credentials, password ) )
            {
                authenticationOk = true;
            }
        }
        
        if ( !authenticationOk )
        {
            log.info( "Authentication failed for user: {}", credentials );
            RtspResponse response =
                    RtspResponse.errorResponse( RtspCode.ProxyAuthenticationRequired );
            response.setHeader( "Proxy-Authenticate", scheme.getName() + " "
                    + scheme.getChallenge() );
            
            session.write( response );
            return;
        }
        
        log.debug( "Authentication successfull for user: {}", credentials );
        
        /*
         * Mark the session with an "authenticated" attribute. This will prevent
         * the check for the credentials for every message received.
         */
        session.setAttribute( ATTR, credentials.getUserName() );
        
        // Forward message
        nextFilter.messageReceived( session, message );
    }
    
    /**
     * Gets the authentication scheme stated by the client.
     * 
     * @param authString
     * @return true if the authentication scheme selected is valid
     */
    private boolean validateAuthenticationScheme( String authString )
    {
        String schemeName;
        try
        {
            schemeName = authString.split( " " )[0];
        } catch ( IndexOutOfBoundsException e )
        {
            // Malformed auth string
            return false;
        }
        
        if ( schemeName.equalsIgnoreCase( scheme.getName() ) ) return true;
        
        // Scheme not valid
        return false;
    }
    
}
