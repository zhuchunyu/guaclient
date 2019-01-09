package com.habage.guaclient.servlet;

import com.google.inject.Provider;
import com.google.inject.servlet.ServletModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

/**
 *
 * @author jerry
 * @date 2019/1/7
 */
public class WebSocketTunnelModule extends ServletModule {

    private final Logger logger = LoggerFactory.getLogger(WebSocketTunnelModule.class);

    @Override
    public void configureServlets() {
        logger.info("Loading JSR-356 WebSocket support...");

        // Get container
        ServerContainer container = (ServerContainer) getServletContext().getAttribute("javax.websocket.server.ServerContainer");
        if (container == null) {
            logger.warn("ServerContainer attribute required by JSR-356 is missing. Cannot load JSR-356 WebSocket support.");
            return;
        }

        Provider<TunnelRequestService> tunnelRequestServiceProvider = getProvider(TunnelRequestService.class);

        // Build configuration for WebSocket tunnel
        ServerEndpointConfig config =
                ServerEndpointConfig.Builder.create(SampleWebSocketTunnelEndpoint.class, "/websocket-tunnel")
                        .configurator(new SampleWebSocketTunnelEndpoint.Configurator(tunnelRequestServiceProvider))
                        .subprotocols(Arrays.asList(new String[]{"guacamole"}))
                        .build();

        try {

            // Add configuration to container
            container.addEndpoint(config);

        }
        catch (DeploymentException e) {
            logger.error("Unable to deploy WebSocket tunnel.", e);
        }
    }
}
