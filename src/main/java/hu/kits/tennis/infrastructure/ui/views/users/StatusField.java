package hu.kits.tennis.infrastructure.ui.views.users;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import hu.kits.tennis.domain.user.UserData;
import hu.kits.tennis.domain.user.UserData.Status;
import hu.kits.tennis.domain.user.UserService;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

class StatusField extends CustomField<Status> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final UserService userService;
    
    private final HorizontalLayout layout = new HorizontalLayout();
    
    private UserData user;
    
    private final Runnable callback;
    
    StatusField(UserService userService, Runnable callback) {
        this.userService = userService;
        this.callback = callback;
        this.setLabel("Státusz");
        layout.setAlignItems(Alignment.CENTER);
        add(layout);
    }
    
    @Override
    protected Status generateModelValue() {
        return null;
    }

    @Override
    protected void setPresentationValue(Status status) {
        layout.removeAll();
        layout.add(new StatusBadge(status), createButton(status));
                
    }

    private Button createButton(Status status) {
        Button button;
        switch(status) {
            case ACTIVE: 
                button = UIUtils.createButton("Inaktivál", ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                button.addClickListener(click -> {
                    VaadinUtil.logUserAction(logger, "inactivating user: {}", user.userId());
                    userService.inactivateUser(user);
                    KITSNotification.showInfo("Felhasználó inaktiválva");
                    callback.run();
                });
                return button;
            case INACTIVE:
                button = UIUtils.createButton("Újraaktivál", ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
                button.addClickListener(click -> {
                    VaadinUtil.logUserAction(logger, "reactivating user: {}", user.userId());
                    userService.activateUser(user);
                    KITSNotification.showInfo("Felhasználó újra aktiválva");
                    callback.run();
                });
                return button;
            case REGISTERED:
                button = UIUtils.createButton("Aktivál", ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
                button.addClickListener(click -> {
                    userService.activateUser(user);
                    VaadinUtil.logUserAction(logger, "activating user: {}", user.userId());
                    KITSNotification.showInfo("Felhasználó aktiválva és emailben értesítve");
                    callback.run();
                });
                return button;
            case ANONYMUS:
            default: throw new IllegalArgumentException("Unhandled state: " + status);
        }
    }

    void setUser(UserData user) {
        this.user = user;
        
    }

}
