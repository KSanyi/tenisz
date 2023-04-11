package hu.kits.tennis.infrastructure.ui.views.players.registration;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.player.registration.Registration;
import hu.kits.tennis.domain.player.registration.RegistrationService;
import hu.kits.tennis.domain.utr.UTR;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class RegistrationApprovalWindow extends Dialog {

    private final RegistrationService registrationService = Main.applicationContext.getRegistrationService();
    
    private final Registration registration;
    private final Runnable callback;
    
    private final NumberField startingUTRField = new NumberField("Kezdő UTR");
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
                startingUTRField, 
                commentField);
        
        setHeaderTitle("Új regisztráció");
        
        startingUTRField.setWidth("120px");
        startingUTRField.setMin(1);
        startingUTRField.setMax(16);
        startingUTRField.setStep(0.01);
        startingUTRField.setStepButtonsVisible(true);
        
        commentField.setWidthFull();
        commentField.setHeight("100px");
        
        getFooter().add(cancelButton, approveButton);
        
        return layout;
    }

    private void approve() {

        if(startingUTRField.isEmpty()) {
            KITSNotification.showError("Add meg a kezdő UTR-t!");
            return;
        }
        
        UTR startingUTR = UTR.of(startingUTRField.getValue());
        
        String comment = commentField.getValue();
        
        registrationService.approveRegistration(registration, startingUTR, comment);
        callback.run();
        close();
        KITSNotification.showInfo(registration.data().name() + " felvéve a játékos adatbázisba!");
    }

}
