package hu.kits.tennis.infrastructure.ui.views.players.registration;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.ktr.KTR;
import hu.kits.tennis.domain.player.registration.Registration;
import hu.kits.tennis.domain.player.registration.RegistrationService;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class RegistrationApprovalWindow extends Dialog {

    private final RegistrationService registrationService = Main.applicationContext.getRegistrationService();
    
    private final Registration registration;
    private final Runnable callback;
    
    private final NumberField startingKTRField = new NumberField("Kezdő KTR");
    private final TextArea commentField = new TextArea("Megjegyzés");
    
    private final Button cancelButton = UIUtils.createContrastButton("Mégsem");
    private final Button approveButton = UIUtils.createPrimaryButton("Jóváhagy");
    
    public RegistrationApprovalWindow(Registration registration, Runnable callback) {
        this.registration = registration;
        this.callback = callback;
        
        cancelButton.addClickListener(click -> close());
        approveButton.addClickListener(click -> approve());
        
        add(createContent());
    }
    
    private Component createContent() {
        RegistrationForm registrationForm = new RegistrationForm();
        registrationForm.setRegistrationData(registration.data());
        registrationForm.setReadOnly();
        VerticalLayout layout = new VerticalLayout(
                registrationForm, 
                startingKTRField, 
                commentField);
        
        setHeaderTitle("Új regisztráció");
        
        startingKTRField.setWidth("120px");
        startingKTRField.setMin(1);
        startingKTRField.setMax(16);
        startingKTRField.setStep(0.01);
        startingKTRField.setStepButtonsVisible(true);
        
        commentField.setWidthFull();
        commentField.setHeight("100px");
        
        getFooter().add(cancelButton, approveButton);
        
        return layout;
    }

    private void approve() {

        if(startingKTRField.isEmpty()) {
            KITSNotification.showError("Add meg a kezdő KTR-t!");
            return;
        }
        
        KTR startingKTR = KTR.of(startingKTRField.getValue());
        
        String comment = commentField.getValue();
        
        registrationService.approveRegistration(registration, startingKTR, comment);
        callback.run();
        close();
        KITSNotification.showInfo(registration.data().name() + " felvéve a játékos adatbázisba!");
    }

}
