package hu.kits.tennis.infrastructure.ui.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.EmailValidator;

import hu.kits.tennis.common.KITSException;
import hu.kits.tennis.domain.user.Requests.UserRegistrationRequest;
import hu.kits.tennis.domain.user.UserService;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class RegistrationDialog extends Dialog {
    
    private final UserService userService;
    
    private final Binder<UserRegistrationRequestBean> binder = new Binder<>(UserRegistrationRequestBean.class);
    
    private final TextField nameField = new TextField("Név");
    private final TextField phoneField = new TextField("Telefonszám");
    private final EmailField emailField = new EmailField("Email cím");
    private final PasswordField passwordField = new PasswordField("Jelszó");
    private final Button registerButton = UIUtils.createPrimaryButton("Regisztrálok");
    private final Button cancelButton = UIUtils.createContrastButton("Mégsem");
    
    public RegistrationDialog(UserService userService) {

        this.userService = userService;
        
        setModal(true);
        
        nameField.setWidth("300px");
        phoneField.setWidth("300px");
        emailField.setWidth("300px");
        passwordField.setWidth("300px");
        
        phoneField.setPattern("[0-9]*");
        phoneField.setPreventInvalidInput(true);
        
        VerticalLayout layout = new VerticalLayout(new H4("Regisztráció"),
                nameField,
                phoneField,
                emailField,
                passwordField,
                new HorizontalLayout(registerButton, cancelButton));
        
        layout.setAlignItems(Alignment.CENTER);
        
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
        
        bind();
        
        registerButton.addClickListener(click -> register());
        cancelButton.addClickListener(click -> close());
    }
    
    private void bind() {
        binder.forField(nameField)
            .asRequired("Kötelező mező")
            .bind("name");
        binder.forField(phoneField)
            .bind("phone");
        binder.forField(emailField)
            .asRequired("Kötelező mező")
            .withValidator(new EmailValidator("Helytelen email cím"))
            .bind("email");
        binder.forField(passwordField)
            .asRequired("Kötelező mező")
            .withValidator(Validator.from(password -> password.length() > 5, "A jelszónak legalább 6 karakter hosszúnak kell lennie"))
            .bind("password");
    }
    
    private void register() {
        
        UserRegistrationRequestBean userRegistrationRequestBean = new UserRegistrationRequestBean();
        boolean valid = binder.writeBeanIfValid(userRegistrationRequestBean);
        if(valid) {
            try {
                userService.register(userRegistrationRequestBean.toUserRegistrationRequest());
                KITSNotification.showInfo("Sikeres regisztráció. Hamarosan értesítünk emailben a regisztráció jóváhagyásáról!");
                close();
            } catch (KITSException ex) {
                KITSNotification.showError(ex.getMessage());
            }
        }
    }
    
    public static class UserRegistrationRequestBean {
        
        private String name;
        private String phone;
        private String email;
        private String password;
        
        public UserRegistrationRequest toUserRegistrationRequest() {
            return new UserRegistrationRequest(name, phone, email, password);
        }
        
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getPhone() {
            return phone;
        }
        public void setPhone(String phone) {
            this.phone = phone;
        }
        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
    }
    
}
