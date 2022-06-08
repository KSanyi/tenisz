package hu.kits.tennis.infrastructure.ui.views.users;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;

import hu.kits.tennis.common.KITSException;
import hu.kits.tennis.domain.user.Requests.UserCreationRequest;
import hu.kits.tennis.domain.user.Requests.UserDataUpdateRequest;
import hu.kits.tennis.domain.user.Role;
import hu.kits.tennis.domain.user.UserData;
import hu.kits.tennis.domain.user.UserData.Status;
import hu.kits.tennis.domain.user.UserService;
import hu.kits.tennis.infrastructure.ui.component.ConfirmationDialog;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.components.detailsdrawer.DetailsDrawer;
import hu.kits.tennis.infrastructure.ui.vaadin.components.detailsdrawer.DetailsDrawerHeader;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

class UserDetailsDrawer extends DetailsDrawer {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final UserService userService;
    
    private final TextField idField = new TextField("Azonosító");
    private final TextField nameField = new TextField("Név");
    private final EmailField emailField = new EmailField("Email");
    private final TextField phoneField = new TextField("Telefon");
    private final ComboBox<Role> roleCombo = new ComboBox<>("Típus", Role.all());
    private final StatusField statusField;
    private final Binder<UserDataBean> binder = new Binder<>(UserDataBean.class);

    private final Button generatePasswordButton = UIUtils.createButton("Új jelszó generálása", ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
    private final Button saveButton = UIUtils.createPrimaryButton("Mentés");
    private final Button deleteButton = UIUtils.createErrorButton(VaadinIcon.TRASH);
    
    private final UsersView usersView;
    
    UserDetailsDrawer(UserService userService, DetailsDrawer.Position position, UsersView usersView) {
        super(position);
        
        this.userService = userService;
        statusField = new StatusField(userService, () -> refresh());
        this.usersView = usersView;
        
        setHeader(createHeader());
        setContent(createContent());
        setFooter(createFooter());
        
        bind();
        
        binder.addValueChangeListener(e -> saveButton.setVisible(binder.hasChanges()));
        saveButton.addClickListener(click -> save());
        deleteButton.addClickListener(click -> delete());
        generatePasswordButton.addClickListener(click -> generateNewPassword());
    }
    
    private void generateNewPassword() {
        new ConfirmationDialog("Biztosan új jelszót generálsz a felhasználónak?", () -> {
            userService.generateNewPassword(idField.getValue());
            KITSNotification.showInfo("Új jelszó generálva és emailben elküldve");
        }).open();
    }

    private void bind() {
        binder.bind(idField, "userId");
        binder.forField(nameField)
            .asRequired("Kötelező mező")
            .bind("name");
        binder.forField(emailField)
            .withValidator(new EmailValidator("Helytelen email cím"))
            .bind("email");
        binder.bind(phoneField, "phone");
        binder.bind(roleCombo, "role");
        binder.bind(statusField, "status");
    }

    private void save() {
        String id = idField.getValue();
        UserDataBean userDataBean = new UserDataBean();
        boolean valid = binder.writeBeanIfValid(userDataBean);
        if(valid) {
            try {
                if(idField.isReadOnly()) {
                    VaadinUtil.logUserAction(logger, "updating new user: {}", userDataBean.userId);
                    userService.updateUser(id, new UserDataUpdateRequest(userDataBean.toUserData()));
                    KITSNotification.showInfo("Felhasználó adatok frissítve");    
                } else {
                    VaadinUtil.logUserAction(logger, "saving user: {}", userDataBean.userId);
                    userService.saveNewUser(new UserCreationRequest(userDataBean.toUserData()));
                    KITSNotification.showInfo("Felhasználó létrehozva.");
                }
                hide();
                usersView.refresh();
            } catch(KITSException ex) {
                KITSNotification.showError(ex.getMessage());
            }
        } else {
            KITSNotification.showError("Hibás adatok");
        }
    }
    
    private void delete() {
        new ConfirmationDialog("Biztosan törölni akarod a felhasználót?", () -> {
            userService.deleteUser(idField.getValue());
            KITSNotification.showInfo("Felhasználó törölve");
            hide();
            usersView.refresh();
        }).open();
    }
    
    private void refresh() {
        usersView.refresh();
        setUser(userService.findUser(idField.getValue()));
    }

    private Component createHeader() {
        DetailsDrawerHeader detailsDrawerHeader = new DetailsDrawerHeader("Felhasználó");
        detailsDrawerHeader.addCloseListener(buttonClickEvent -> hide());
        return detailsDrawerHeader;
    }
    
    private Component createContent() {
        VerticalLayout fieldsLayout = new VerticalLayout(idField, 
                nameField, 
                emailField,
                phoneField,
                roleCombo, 
                statusField,
                new Hr(),
                generatePasswordButton, 
                deleteButton);
        fieldsLayout.setSpacing(false);
        fieldsLayout.setAlignSelf(Alignment.END, deleteButton);
        
        idField.setWidth("300px");
        nameField.setWidth("300px");
        emailField.setWidth("300px");
        phoneField.setWidth("200px");
        roleCombo.setWidth("120px");
        
        roleCombo.setItemLabelGenerator(Role::label);
        
        return fieldsLayout;
    }
    
    private Component createFooter() {
        saveButton.setWidthFull();
        saveButton.setHeight("50px");
        return saveButton;
    }
    
    void setUser(UserData userData) {
        binder.readBean(new UserDataBean(userData));
        idField.setReadOnly(!userData.isNew());
        deleteButton.setVisible(!userData.isNew());
        generatePasswordButton.setVisible(!userData.isNew());
        saveButton.setVisible(false);
        statusField.setUser(userData);
        VaadinUtil.logUserAction(logger, "viewing user: {}", userData);
    }
    
    public static class UserDataBean {
        
        private String userId;
        private String name;
        private Role role;
        private String phone; 
        private String email; 
        private Status status;
        
        UserDataBean(UserData userData) {
            userId = userData.userId();
            name = userData.name();
            role = userData.role();
            phone = userData.phone();
            email = userData.email();
            status = userData.status();
        }
        
        UserDataBean() {}
        
        public String getUserId() {
            return userId;
        }
        public void setUserId(String userId) {
            this.userId = userId;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public Role getRole() {
            return role;
        }
        public void setRole(Role role) {
            this.role = role;
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
        public Status getStatus() {
            return status;
        }
        public void setStatus(Status status) {
            this.status = status;
        }
        
        UserData toUserData() {
            return new UserData(userId, name, role, phone, email, status);
        }
        
    }
    
}
