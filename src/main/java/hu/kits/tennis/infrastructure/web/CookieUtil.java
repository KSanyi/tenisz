package hu.kits.tennis.infrastructure.web;

import static java.util.stream.Collectors.joining;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.http.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;

public class CookieUtil {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final static String COOKIE_KEY = "TENISZ75465";
    
    public static Optional<String> findUserFromCookie() {
        VaadinRequest vaadinRequest = VaadinService.getCurrentRequest();
        if(vaadinRequest != null) {
            Cookie[] cookies = vaadinRequest.getCookies();
            if(cookies != null) {
                return Stream.of(cookies).filter(c -> COOKIE_KEY.equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst();
            }
        }
        return Optional.empty();
    }
    
    public static void createUserCookie(String memberId) {
        Cookie cookie = new Cookie(COOKIE_KEY, memberId);
        cookie.setPath("/");
        cookie.setMaxAge(100 * 24 * 60 * 60);
        
        log.debug("New user cookie created: {}: {}", COOKIE_KEY, printUserCookie(cookie));
        
        VaadinService.getCurrentResponse().addCookie(cookie);
    }
    
    private static String printUserCookie(Cookie cookie) {
        String[] parts = cookie.getValue().split("\\.");
        return Stream.of(parts).map(part -> part.substring(0, Math.min(20, part.length()))).collect(joining("..."));
    }
    
    public static void deleteUserCookie() {
        Cookie cookie = new Cookie(COOKIE_KEY, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        VaadinService.getCurrentResponse().addCookie(cookie);
    }
    
}
