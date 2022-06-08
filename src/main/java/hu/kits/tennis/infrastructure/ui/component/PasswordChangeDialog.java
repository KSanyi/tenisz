package hu.kits.tennis.infrastructure.ui.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.user.AuthenticationException;
import hu.kits.tennis.domain.user.Requests.PasswordChangeRequest;
import hu.kits.tennis.domain.user.UserService;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class PasswordChangeDialog extends Dialog {
    
    private final UserService userService;
    
    private final PasswordField oldPasswordField = new PasswordField("Régi jelszó");
    private final PasswordField newPasswordField = new PasswordField("Új jelszó");
    private final PasswordField newPasswordConfirmationField = new PasswordField("Új jelszó megerősítése");
    private final Button changePasswordButton = UIUtils.createPrimaryButton("Változtat");
    private final Button cancelButton = UIUtils.createContrastButton("Mégsem");
    
    private final Binder<PasswordChangeRequestBean> binder = new Binder<>(PasswordChangeRequestBean.class);
    
    public PasswordChangeDialog() {

        userService = Main.resourceFactory.getUserService();
        
        setModal(true);
        
        VerticalLayout layout = new VerticalLayout(new H4("Jelszóváltoztatás"),
                oldPasswordField,
                newPasswordField, 
                newPasswordConfirmationField,
                new HorizontalLayout(changePasswordButton, cancelButton));
        
        layout.setAlignItems(Alignment.CENTER);
        
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
        
        bind();
        
        changePasswordButton.addClickListener(click -> changePassword());
        cancelButton.addClickListener(click -> close());
    }
    
    private void bind() {
        binder.forField(oldPasswordField)
            .asRequired("Kötelező mező")
            .bind("oldPassword");
        binder.forField(newPasswordField)
            .asRequired("Kötelező mező")
            .withValidator(Validator.from(password -> password.length() > 5, "A jelszónak legalább 6 karakter hosszúnak kell lennie"))
            .bind("newPassword");
        binder.forField(newPasswordConfirmationField)
            .asRequired("Kötelező mező")
            .withValidator(Validator.from(password -> password.equals(newPasswordField.getValue()), "Az új jelszónak és a megerősítésnek meg kell egyeznie!"))
            .bind("newPasswordConfirmation");
    }

    private void changePassword() {
        PasswordChangeRequestBean passwordChangeRequestBean = new PasswordChangeRequestBean();
        boolean valid = binder.writeBeanIfValid(passwordChangeRequestBean);
        if(valid) {
            try {
                userService.changePassword(VaadinUtil.getUser().userId(), passwordChangeRequestBean.toPasswordChangeRequest());
                KITSNotification.showInfo("Sikeres jelszóváltoztatás");
                close();
            } catch (AuthenticationException ex) {
                KITSNotification.showError("Hibás régi jelszó");
            }
        }
    }
    
    public static class PasswordChangeRequestBean {
        
        private String oldPassword;
        private String newPassword;
        private String newPasswordConfirmation;

        public PasswordChangeRequest toPasswordChangeRequest() {
            return new PasswordChangeRequest(oldPassword, newPassword);
        }
        
        public String getOldPassword() {
            return oldPassword;
        }
        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }
        public String getNewPassword() {
            return newPassword;
        }
        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
        public String getNewPasswordConfirmation() {
            return newPasswordConfirmation;
        }
        public void setNewPasswordConfirmation(String newPasswordConfirmation) {
            this.newPasswordConfirmation = newPasswordConfirmation;
        }
    }
}
