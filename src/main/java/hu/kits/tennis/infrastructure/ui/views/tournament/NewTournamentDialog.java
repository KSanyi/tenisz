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
import hu.kits.tennis.domain.tournament.Organizer;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.infrastructure.ui.component.ComponentFactory;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class NewTournamentDialog extends Dialog {

    private final TournamentService tournamentService = Main.resourceFactory.getTournamentService();
    
    private final Consumer<Tournament> callback;
    
    private final ComboBox<Organizer> organizerCombo = ComponentFactory.createComboBox("Szervező", o -> o.name, Organizer.values());
    private final TextField nameField = new TextField("Név");
    private final DatePicker dateField = ComponentFactory.createHungarianDatePicker("Dátum");
    private final TextField venueField = new TextField("Helyszín");
    private final ComboBox<Integer> setsCombo = ComponentFactory.createComboBox("Szettek", i -> i.toString(), List.of(1, 3, 5));
    private final ComboBox<Tournament.Type> typeCombo = ComponentFactory.createComboBox("Típus", o -> o.name(), Tournament.Type.values());
    
    private final Button saveButon = UIUtils.createPrimaryButton("Mentés");

    public NewTournamentDialog(Consumer<Tournament> callback) {
        this.callback = callback;
        
        add(createContent());
        
        saveButon.addClickListener(click -> save());
    }

    private void save() {
        Tournament tournament = tournamentService.createTournament(organizerCombo.getValue(), nameField.getValue(), venueField.getValue(), dateField.getValue(), typeCombo.getValue(), setsCombo.getValue());
        
        close();
        callback.accept(tournament);
    }

    private Component createContent() {
        
        organizerCombo.setWidth("300px");
        nameField.setWidth("300px");
        typeCombo.setWidth("300px");
        
        VerticalLayout layout = new VerticalLayout(organizerCombo, nameField, dateField, setsCombo, typeCombo, saveButon);
        layout.setAlignSelf(Alignment.CENTER, saveButon);
        return layout;
    }
    
}
