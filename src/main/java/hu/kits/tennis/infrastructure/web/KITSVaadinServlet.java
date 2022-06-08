package hu.kits.tennis.infrastructure.web;

import java.lang.invoke.MethodHandles;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.SessionDestroyEvent;
import com.vaadin.flow.server.SessionDestroyListener;
import com.vaadin.flow.server.VaadinServlet;

import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;

@WebServlet(value = "/*")
public class KITSVaadinServlet extends VaadinServlet implements SessionDestroyListener {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    @Override
    protected void servletInitialized() throws ServletException {
        super.servletInitialized();
        getService().addSessionDestroyListener(this);
    }

    @Override
    public void sessionDestroy(SessionDestroyEvent event) {
        logger.trace("Session destroyed for {}", VaadinUtil.getUser().userId());
    }
}
