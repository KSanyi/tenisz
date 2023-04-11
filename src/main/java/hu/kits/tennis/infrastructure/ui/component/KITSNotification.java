package hu.kits.tennis.infrastructure.ui.component;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;

import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;

import com.vaadin.flow.component.notification.NotificationVariant;

public class KITSNotification {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    public static void showError(String message) {
        Notification notification = new Notification(message);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setDuration(3000);
        notification.setPosition(Position.MIDDLE);
        notification.open();
        VaadinUtil.logUserAction(logger, " is shown an error '{}'", message);
    }

    public static void showInfo(String message) {
        showInfo(message, 2000);
    }
    
    public static void showInfo(String message, int duration) {
        Notification notification = new Notification(message);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setDuration(duration);
        notification.setPosition(Position.MIDDLE);
        notification.open();
    }
    
    public static void showWarning(String message) {
        Notification notification = new Notification(message);
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        notification.setDuration(3000);
        notification.setPosition(Position.MIDDLE);
        notification.open();
    }
    
}
