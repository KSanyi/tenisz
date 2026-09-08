package hu.kits.tennis.infrastructure.web;

import java.net.URI;
import java.net.URL;

import org.eclipse.jetty.ee11.annotations.AnnotationConfiguration;
import org.eclipse.jetty.ee11.webapp.Configuration;
import org.eclipse.jetty.ee11.webapp.MetaInfConfiguration;
import org.eclipse.jetty.ee11.webapp.WebAppContext;
import org.eclipse.jetty.ee11.webapp.WebInfConfiguration;
import org.eclipse.jetty.ee11.webapp.WebXmlConfiguration;
import org.eclipse.jetty.ee11.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.ClassMatcher;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;

import com.vaadin.flow.server.startup.ServletContextListeners;

import hu.kits.tennis.Main;

/*
 * Mounts the Vaadin UI (as a Jetty WebAppContext at "/ui") onto the Server that Javalin creates,
 * so Vaadin and the Javalin REST endpoints can be served from the same embedded Jetty instance.
 */
public class VaadinJettyServer {

    public static void attachTo(Server server) {

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.addHandler(createVaadinHandler());
        server.setHandler(contexts);
    }

    private static Handler createVaadinHandler() {

        WebAppContext context = new WebAppContext();
        context.setBaseResource(createBaseResource());
        context.setContextPath("/ui");
        // Scan container jars for Vaadin's frontend/annotation resources, but keep out
        // Javalin's own (EE10) websocket jars: their ServletContainerInitializer would
        // otherwise get picked up by this (EE11) context too and fail to find an EE10
        // ServletContextHandler to attach to.
        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                "^(?!.*(javalin|jetty-ee10|jetty-websocket|websocket-jetty)).*$");
        context.setConfigurationDiscovered(true);
        context.setThrowUnavailableOnStartupException(true);
        context.setParentLoaderPriority(true);
        // The whole app (server + webapp) runs from a single flat classpath rather than a
        // real WAR with an isolated WEB-INF/lib, so Jetty's default "hide org.eclipse.jetty.*
        // from the webapp" classloader isolation just breaks its own webdefault-ee11.xml
        // listener (IntrospectorCleaner). Only un-hide what our own EE11 context needs.
        context.setHiddenClassMatcher(new ClassMatcher("org.eclipse.jetty.", "-org.eclipse.jetty.ee11."));
        context.setConfigurations(new Configuration[]{
          new AnnotationConfiguration(),
          new WebInfConfiguration(),
          new WebXmlConfiguration(),
          new MetaInfConfiguration()
        });
        context.addEventListener(new ServletContextListeners());

        JakartaWebSocketServletContainerInitializer.configure(context, null);

        return context;
    }

    private static Resource createBaseResource() {
        URL webRootLocation = Main.class.getResource("/webapp/");
        try {
            URI webRootUri = webRootLocation.toURI();
            return ResourceFactory.root().newResource(webRootUri);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
