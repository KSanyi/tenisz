package hu.kits.tennis.infrastructure.ui.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ConfirmationDialog extends Dialog {

    public ConfirmationDialog(String message, Runnable action) {
        this(message, action, ()->{});
    }
    
    public ConfirmationDialog(String message, Runnable yesAction, Runnable noAction) {

        setCloseOnOutsideClick(false);
        
        Button confirmButton = new Button("Igen", click -> { 
            yesAction.run();
            close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmButton.setWidth("100px");
        
        Button cancelButton = new Button("Nem", click -> {
            noAction.run();
            close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelButton.setWidth("100px");
        
        VerticalLayout layout = new VerticalLayout(new H5(message), new HorizontalLayout(cancelButton, confirmButton));
        layout.setAlignItems(Alignment.CENTER);
        layout.setPadding(false);
        
        add(layout);     
    }
}
