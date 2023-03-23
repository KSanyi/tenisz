package hu.kits.tennis.infrastructure.web;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.websocket.server.ServerContainer;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import com.vaadin.flow.server.startup.ServletContextListeners;

import hu.kits.tennis.Main;

/*
 * If this server is used as the embedded Javalin server, then Vaadin apps can be run along with the standard Javalin http endpoints 
 */
public class VaadinJettyServer extends Server {

    public VaadinJettyServer(int port) {
        super(port);
        setHandler(createHandler());
    }
    
    private static Handler createHandler() {
        
        ContextHandlerCollection handlerCollection = new ContextHandlerCollection();
        handlerCollection.addHandler(createVaadinHandler());
        return handlerCollection;
    }
    
    private static Handler createVaadinHandler() {
        
        ContextHandlerCollection handlerCollection = new ContextHandlerCollection();
        
        WebAppContext context = new WebAppContext();
        context.setBaseResource(createBaseResource());
        context.setContextPath("/ui");
        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*");
        context.setConfigurationDiscovered(true);
        context.setConfigurations(new Configuration[]{
          new AnnotationConfiguration(),
          new WebInfConfiguration(),
          new WebXmlConfiguration(),
          new MetaInfConfiguration()
        });
        context.getServletContext().setExtendedListenerTypes(true);
        context.addEventListener(new ServletContextListeners());
        
        handleStupidJsr356Exception(context);
        
        handlerCollection.addHandler(context);
        
        return handlerCollection;
    }
    
    @SuppressWarnings("deprecation")
    private static void handleStupidJsr356Exception(WebAppContext context) {
        try {
            // to suppress this: java.lang.IllegalStateException: Unable to configure jsr356 at that stage. ServerContainer is null 
            context.getServletContext().setAttribute(ServerContainer.class.getName(), WebSocketServerContainerInitializer.configureContext(context));
        } catch(ServletException ex) {
            //logger.error(ex.getMessage());
        }
    }
    
    private static Resource createBaseResource() {
        URL webRootLocation = Main.class.getResource("/webapp/");
        try {
            URI webRootUri = webRootLocation.toURI();
            return Resource.newResource(webRootUri);
        } catch (URISyntaxException | MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
}