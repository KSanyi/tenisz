package hu.kits.tennis.infrastructure.ui.component;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.login.AbstractLogin.LoginEvent;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.VaadinSession;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.KITSException;
import hu.kits.tennis.domain.user.AuthenticationException;
import hu.kits.tennis.domain.user.UserData;
import hu.kits.tennis.domain.user.UserService;
import hu.kits.tennis.infrastructure.ui.MainLayout;

public class LoginDialog extends Dialog implements RequestHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final UserService userService = Main.applicationContext.getUserService();
    
    private final LoginForm loginForm = new LoginForm(createHungarianI18n());
    private final Anchor registerButton = new Anchor("registration", "Regisztráció", AnchorTarget.BLANK);
    private final Anchor googleAuthButton;
    
    public LoginDialog() {
        setModal(true);
        
        googleAuthButton = new Anchor(userService.getAuthorizationUrl(), "Bejelentkezés Gmail-lel");
        
        VerticalLayout layout = new VerticalLayout(loginForm, googleAuthButton, registerButton);
        layout.setAlignItems(Alignment.CENTER);
        layout.setPadding(false);
        layout.setSpacing(false);
        
        add(layout);
        
        loginForm.addLoginListener(this::logIn);
        
        googleAuthButton.setVisible(false);
        
        loginForm.addForgotPasswordListener(e -> KITSNotification.showInfo("Nincs még implementálva"));
        
        VaadinSession.getCurrent().addRequestHandler(this);
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

    @Override
    public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response) throws IOException {
        if (request.getParameter("code") != null) {
            String code = request.getParameter("code");
            try {
                UserData user = userService.authenticateWithOAuth(code);
                getUI().ifPresent(ui -> ui.access(() -> {
                    MainLayout.get().userLoggedIn(user);
                    KITSNotification.showInfo("Üdv " + user.name());
                    close();
                    logger.info(user.name() + " logged in");
                    VaadinSession.getCurrent().removeRequestHandler(this);
                }));
            } catch(AuthenticationException ex) {
                loginForm.setError(true);
                if(!ex.getMessage().isEmpty()) {
                    KITSNotification.showError(ex.getMessage());
                }
            } catch(KITSException ex) {
                KITSNotification.showError("Felhasználó státusza inaktív. Az admin fogja aktivizálni");
            }

            ((VaadinServletResponse) response).getHttpServletResponse().sendRedirect("http://localhost:7979/ui/");
            return true;
        }

        return false;
    }
    
}
