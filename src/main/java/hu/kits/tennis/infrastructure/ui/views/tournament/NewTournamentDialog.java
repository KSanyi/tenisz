package hu.kits.tennis.infrastructure.ui.views.tournament;

import java.util.List;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.Clock;
import hu.kits.tennis.domain.tournament.Organization;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.Tournament.Type;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.infrastructure.ui.component.ComponentFactory;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class NewTournamentDialog extends Dialog {

    private final TournamentService tournamentService = Main.resourceFactory.getTournamentService();
    
    private final Consumer<Tournament> callback;
    
    private final ComboBox<Organization> organizationCombo = ComponentFactory.createComboBox("Szervezet", o -> o.name, Organization.values());
    private final TextField nameField = new TextField("Név");
    private final DatePicker dateField = ComponentFactory.createHungarianDatePicker("Dátum");
    private final TextField venueField = new TextField("Helyszín");
    private final ComboBox<Integer> setsCombo = ComponentFactory.createComboBox("Szettek", i -> i.toString(), List.of(1, 3, 5));
    private final ComboBox<Tournament.Type> typeCombo = ComponentFactory.createComboBox("Típus", o -> o.label, Tournament.Type.values());
    
    private final Button saveButton = UIUtils.createPrimaryButton("Mentés");

    public NewTournamentDialog(Consumer<Tournament> callback) {
        this.callback = callback;
        
        setDefaults();
        
        add(createContent());
        
        saveButton.addClickListener(click -> save());
    }

    private void setDefaults() {
        organizationCombo.setValue(Organization.KVTK);
        nameField.setValue("TOUR");
        dateField.setValue(Clock.today());
        setsCombo.setValue(3);
        typeCombo.setValue(Type.SIMPLE_BOARD);
    }

    private void save() {
        Tournament tournament = tournamentService.createTournament(
                organizationCombo.getValue(),
                nameField.getValue(),
                venueField.getValue(),
                dateField.getValue(),
                typeCombo.getValue(),
                setsCombo.getValue());
        
        close();
        callback.accept(tournament);
    }

    private Component createContent() {
        
        organizationCombo.setWidth("200px");
        nameField.setWidth("200px");
        dateField.setWidth("150px");
        setsCombo.setWidth("80px");
        typeCombo.setWidth("200px");
        
        VerticalLayout layout = new VerticalLayout(organizationCombo, nameField, dateField, setsCombo, typeCombo);
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setAlignSelf(Alignment.CENTER, saveButton);
        getFooter().add(saveButton);
        return layout;
    }
    
}
