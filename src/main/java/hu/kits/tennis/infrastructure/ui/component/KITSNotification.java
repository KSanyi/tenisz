package hu.kits.tennis.infrastructure.ui.component;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;

public class KITSNotification {

    public static void showError(String message) {
        Notification notification = new Notification(message);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setDuration(3000);
        notification.setPosition(Position.MIDDLE);
        notification.open();
    }

    public static void showInfo(String message) {
        Notification notification = new Notification(message);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setDuration(2000);
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
