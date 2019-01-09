package com.habage.guaclient.servlet;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebListener;

/**
 *
 * @author jerry
 * @date 2019/1/7
 */
@WebListener
public class GuacamoleServletContextListener extends GuiceServletContextListener {

    private final Logger logger = LoggerFactory.getLogger(GuacamoleServletContextListener.class);

    @Override
    protected Injector getInjector() {
        System.out.println("start GuacamoleServletContextListener...");
        Injector injector = Guice.createInjector(Stage.PRODUCTION,
                new WebSocketTunnelModule()
        );

        // Inject any annotated members of this class
        injector.injectMembers(this);

        return injector;
    }
}
