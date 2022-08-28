package hu.kits.tennis.infrastructure.ui.util;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.VaadinSession;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.KITSException;
import hu.kits.tennis.domain.user.Role;
import hu.kits.tennis.domain.user.UserData;
import hu.kits.tennis.infrastructure.web.CookieUtil;

public class VaadinUtil {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private static final AtomicInteger ANONYMUS_USER_COUNT = new AtomicInteger(1);
    
    public static final int MOBILE_BREAKPOINT = 1024;
    
    private static final String USER_KEY = "USER_KEY";
    
    public static UserData getUser() {
        VaadinSession vadinSession = VaadinSession.getCurrent();
        if(vadinSession != null) {
            UserData user = (UserData)VaadinSession.getCurrent().getAttribute(USER_KEY);
            if(user != null) {
                return user;
            } else {
                Optional<String> userFromCookie = CookieUtil.findUserFromCookie();
                if(userFromCookie.isPresent()) {
                    try {
                        logger.debug("User '{}' found in cookie", userFromCookie.get());
                        user = Main.resourceFactory.getUserService().findUser(userFromCookie.get());
                        setUser(user);
                        return user;
                    } catch(KITSException ex) {
                        logger.warn("User id '{}' not found in the database", userFromCookie.get());
                    }
                } else {
                    int id = ANONYMUS_USER_COUNT.getAndIncrement();
                    setUser(UserData.createAnonymus(id));
                }              
            }
        } else {
            logger.debug("ANONYMUS is using " + VaadinSession.getCurrent().getBrowser().getBrowserApplication());
        }
        
        return UserData.ANONYMUS;
    }
    
    public static void setUser(UserData user) {
        logger.debug(user + " is using " + VaadinSession.getCurrent().getBrowser().getBrowserApplication());
        VaadinSession.getCurrent().setAttribute(USER_KEY, user);
    }
    
    public static boolean isViewAllowed(Class<?> viewClass) {
        AllowedRoles allowedRolesAnnotation = viewClass.getAnnotation(AllowedRoles.class);
        return allowedRolesAnnotation == null || Arrays.asList(allowedRolesAnnotation.value()).contains(VaadinUtil.getUser().role());
    }
    
    public static void logUserAction(Logger logger, String log, Object ... args) {
        List<Object> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.add(0, getUser().name());
        logger.debug("{} " + log, argsList.toArray());
    }

    public static boolean isUserLoggedIn() {
        return getUser().role() != Role.ANONYMUS;
    }

}
