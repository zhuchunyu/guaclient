package com.habage.guaclient.servlet;

import com.google.inject.Provider;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.websocket.GuacamoleWebSocketTunnelEndpoint;

import java.util.Map;

import javax.websocket.EndpointConfig;
import javax.websocket.HandshakeResponse;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * @author yuz
 * @date 2019/1/7
 */
public class SampleWebSocketTunnelEndpoint extends GuacamoleWebSocketTunnelEndpoint {

    private static final String TUNNEL_REQUEST_PROPERTY = "WS_GUAC_TUNNEL_REQUEST";
    private static final String TUNNEL_REQUEST_SERVICE_PROPERTY = "WS_GUAC_TUNNEL_REQUEST_SERVICE";

    public static class Configurator extends ServerEndpointConfig.Configurator {

        private final Provider<TunnelRequestService> tunnelRequestServiceProvider;

        public Configurator(Provider<TunnelRequestService> tunnelRequestServiceProvider) {
            this.tunnelRequestServiceProvider = tunnelRequestServiceProvider;
        }

        @Override
        public void modifyHandshake(ServerEndpointConfig config,
                                    HandshakeRequest request, HandshakeResponse response) {
            super.modifyHandshake(config, request, response);

            Map<String, Object> userProperties = config.getUserProperties();
            userProperties.clear();
            userProperties.put(TUNNEL_REQUEST_PROPERTY, new WebSocketTunnelRequest(request));
            userProperties.put(TUNNEL_REQUEST_SERVICE_PROPERTY, tunnelRequestServiceProvider.get());
        }
    }

    @Override
    protected GuacamoleTunnel createTunnel(Session session, EndpointConfig config) throws GuacamoleException {

        Map<String, Object> userProperties = config.getUserProperties();

        // Get original tunnel request
        TunnelRequest tunnelRequest = (TunnelRequest) userProperties.get(TUNNEL_REQUEST_PROPERTY);
        if (tunnelRequest == null) {
            return null;
        }

        // Get tunnel request service
        TunnelRequestService tunnelRequestService = (TunnelRequestService) userProperties.get(TUNNEL_REQUEST_SERVICE_PROPERTY);
        if (tunnelRequestService == null) {
            return null;
        }

        return tunnelRequestService.createTunnel(tunnelRequest);
    }
}
