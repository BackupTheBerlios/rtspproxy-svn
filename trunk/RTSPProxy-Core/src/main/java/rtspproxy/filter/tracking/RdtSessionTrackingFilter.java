/**
 * 
 */
package rtspproxy.filter.tracking;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.config.Config;
import rtspproxy.filter.FilterBase;
import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspResponse;
import rtspproxy.rtsp.RtspTransport;
import rtspproxy.rtsp.RtspTransportList;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 *
 */
public abstract class RdtSessionTrackingFilter extends FilterBase {
	
	private static Logger logger = LoggerFactory.getLogger(RdtSessionTrackingFilter.class);

	public static final String FilterNAME = "rdtTrackingFilter";

	/**
	 * @param filterName
	 * @param className
	 * @param typeName
	 */
	public RdtSessionTrackingFilter(String typeName) {
		super(FilterNAME, typeName);
	}

	/**
	 * check the message for following conditions
	 */
	protected void handleMessage(IoSession session, Object message) {
		if(Config.proxyTransportRdtEnable.getValue() && message instanceof RtspResponse) {
			RtspResponse resp = (RtspResponse)message;
			
			logger.debug("analyzing RTSP response message");
			if(resp.getRequestVerb() == RtspRequest.Verb.SETUP) {
				logger.debug("found SETUP response");
				
				String transHdr = resp.getHeader("Transport");
				
				if(transHdr != null) {
					logger.debug("SETUP response has transport header: " + transHdr);
					
					RtspTransportList rtl = new RtspTransportList(transHdr);
					
					if(rtl.count() == 1) {
						// at this point we can only have one transport. Everything else is a protocol violation
						RtspTransport transport = rtl.get(0);
						
						if(transport.getTransportProtocol() == RtspTransport.TransportProtocol.RDT) {
							logger.debug("found RDT transport protocol");
							
							if(transport.getDeliveryType() == RtspTransport.DeliveryType.unicast
									&& transport.getLowerTransport() == RtspTransport.LowerTransport.UDP) {
								logger.debug("found RDT/UDP/unicast transport header, server_port=" 
										+ transport.getServerPort()[0] + ", client_port=" 
										+ transport.getClientPort()[0]);
								
								handleTransportRdtUdpUnicast(session, transport);
							}
						}
					} else 
						logger.error("found invalid transport header: " + transHdr);
				}
			}
		}
	}
	
	/**
	 * handle the extracted RTSP response RDT/UDP/unicast transport header
	 */
	protected abstract void handleTransportRdtUdpUnicast(IoSession session, RtspTransport transport);
	
}
