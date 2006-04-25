/**
 * 
 */
package rtspproxy.filter.tracking;

import java.net.URL;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtspproxy.rtsp.RtspRequest;
import rtspproxy.rtsp.RtspTransport;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@vodafone.com)
 * 
 */
public class RdtSessionClientTrackingFilter extends RdtSessionTrackingFilter {

	private static Logger logger = LoggerFactory
			.getLogger(RdtSessionClientTrackingFilter.class);

	// session attribute
	private static final String SessionAttribute = "lastSetupURL";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.mina.common.IoFilterAdapter#messageSent(org.apache.mina.common.IoFilter.NextFilter,
	 *      org.apache.mina.common.IoSession, java.lang.Object)
	 */
	@Override
	public void messageSent(NextFilter nextFilter, IoSession session,
			Object message) throws Exception {
		handleMessage(session, message);

		logger.debug("sending response to client: " + message);

		nextFilter.messageSent(session, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.mina.common.IoFilterAdapter#messageReceived(org.apache.mina.common.IoFilter.NextFilter,
	 *      org.apache.mina.common.IoSession, java.lang.Object)
	 */
	@Override
	public void messageReceived(NextFilter nextFilter, IoSession session,
			Object message) throws Exception {
		if (message instanceof RtspRequest) {
			RtspRequest req = (RtspRequest) message;

			logger.debug("having RTSP request message, message=" + req);
			if (req.getVerb() == RtspRequest.Verb.SETUP) {
				logger.debug("having SETUP request");

				if (req.getUrl() != null) {
					URL url = req.getUrl();

					logger.debug("requesting setup for " + url);
					session.setAttribute(SessionAttribute, url);
				}
			}
		}

		nextFilter.messageReceived(session, message);
	}

	@Override
	protected void handleTransportRdtUdpUnicast(IoSession session,
			RtspTransport transport) {
		logger.debug("handling client-side RDT/UDP/unicast header, header="
				+ transport);

        logger.debug( "RDT tracking disabled. (merlimat)" );
        // TODO: RDT tracking disabled. (merlimat)
        /*
		try {
			if (ProxyHandler.containsSharedSessionAttribute(session,
					RdtSessionToken.SessionAttribute)) {
				RdtSessionToken token = (RdtSessionToken) ProxyHandler
						.getSharedSessionAttribute(session,
								RdtSessionToken.SessionAttribute);

				logger.debug("have session token, server_addr="
						+ token.getRemoteServer() + ", server_port="
						+ token.getRemotePort() + ", client_addr="
						+ session.getRemoteAddress() + ", client_port="
						+ transport.getClientPort()[0]);

				// now we can create and initialise
				URL url = (URL) session.getAttribute(SessionAttribute);

				InetSocketAddress serverAddr = new InetSocketAddress(
						((InetSocketAddress) token.getRemoteServer())
								.getAddress(), token.getRemotePort());

				RdtTrack track;
				if ((track = (RdtTrack) RdtTrack.getByServerAddress(serverAddr)) == null) {
					logger.debug("creating new RdtTrack");

					track = new RdtTrack(url.toString());

					track.setClientAddress(((InetSocketAddress) session
							.getRemoteAddress()).getAddress(), transport
							.getClientPort()[0]);
					track.setServerAddress(serverAddr.getAddress(), serverAddr
							.getPort());
				}
			}
		} catch (Throwable t) {
			logger.error("runtime exception in RDP session handling code", t);
		}
        */
	}
}
