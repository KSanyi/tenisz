package hu.kits.tennis.infrastructure.ui.component;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.login.AbstractLogin.LoginEvent;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.KITSException;
import hu.kits.tennis.domain.user.AuthenticationException;
import hu.kits.tennis.domain.user.UserData;
import hu.kits.tennis.domain.user.UserService;
import hu.kits.tennis.infrastructure.ui.MainLayout;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class LoginDialog extends Dialog {
    
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final UserService userService;
    
    private final LoginForm loginForm = new LoginForm(createHungarianI18n());
    private final Button registerButton = UIUtils.createTertiaryButton("Regisztráció");
    
    public LoginDialog() {

        userService = Main.resourceFactory.getUserService();
        
        setModal(true);
        
        VerticalLayout layout = new VerticalLayout(loginForm, registerButton);
        layout.setAlignItems(Alignment.CENTER);
        layout.setPadding(false);
        layout.setSpacing(false);
        
        add(layout);
        
        loginForm.addLoginListener(this::logIn);
        registerButton.addClickListener(click -> {
            new RegistrationDialog(userService).open();
            close();
        });
        registerButton.setVisible(false);
        
        loginForm.addForgotPasswordListener(e -> KITSNotification.showInfo("Nincs még implementálva"));
    }
    
    private static LoginI18n createHungarianI18n() {
        final LoginI18n i18n = LoginI18n.createDefault();

        //i18n.setHeader(new LoginI18n.Header());
        //i18n.getHeader().setTitle("Bejelentkezés");
        //i18n.getHeader().setDescription("Bejelentkezés");
        i18n.getForm().setUsername("Felhasználónév vagy email");
        i18n.getForm().setTitle("Bejelentkezés");
        i18n.getForm().setSubmit("Belépés");
        i18n.getForm().setPassword("Jelszó");
        i18n.getForm().setForgotPassword("Elfelejtett jelszó");
        i18n.getErrorMessage().setTitle("Hiba");
        i18n.getErrorMessage().setMessage("Érvénytelen felhasználónév vagy jelszó");
        //i18n.setAdditionalInformation("Meccs eredmény beíráshoz be kell jeletkezni! Ha nincs még felhasználód, regisztrálj!");
        return i18n;
    }

    private void logIn(LoginEvent loginEvent) {
        try {
            UserData user = userService.authenticateUser(loginEvent.getUsername(), loginEvent.getPassword());
            MainLayout.get().userLoggedIn(user);
            KITSNotification.showInfo("Üdv " + user.name());
            close();
            logger.info(user.name() + " logged in");
        } catch(AuthenticationException ex) {
            loginForm.setError(true);
            if(!ex.getMessage().isEmpty()) {
                KITSNotification.showError(ex.getMessage());
            }
        } catch(KITSException ex) {
            KITSNotification.showError("Felhasználó státusza inaktív. Az admin fogja aktivizálni");
        }
    }
}
