package hu.kits.tennis.infrastructure.ui.views.tournament;

import java.time.LocalDate;
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
import com.vaadin.flow.data.binder.Binder;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.Clock;
import hu.kits.tennis.domain.tournament.Organization;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.Tournament.Type;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.infrastructure.ui.component.ComponentFactory;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class NewTournamentDialog extends Dialog {

    private final TournamentService tournamentService = Main.resourceFactory.getTournamentService();
    
    private final Consumer<Tournament> callback;
    
    private final ComboBox<Organization> organizationCombo = ComponentFactory.createComboBox("Szervezet", o -> o.name, Organization.values());
    private final TextField nameField = new TextField("Név");
    private final DatePicker dateField = ComponentFactory.createHungarianDatePicker("Dátum");
    private final ComboBox<Integer> setsCombo = ComponentFactory.createComboBox("Szettek", i -> i.toString(), List.of(1, 3, 5));
    private final ComboBox<Tournament.Type> typeCombo = ComponentFactory.createComboBox("Típus", t -> t.label, Tournament.Type.values());
    
    private final Button saveButton = UIUtils.createPrimaryButton("Mentés");
    
    private final Binder<TournamentInfoBean> binder = new Binder<>(TournamentInfoBean.class);

    public NewTournamentDialog(Consumer<Tournament> callback) {
        this.callback = callback;
        
        bind();
        
        add(createContent());
        nameField.focus();
        saveButton.addClickListener(click -> save());
    }

    private void bind() {
        
        binder.bind(organizationCombo, "organization");
        binder.forField(nameField)
            .asRequired("Kötelező mező")
            .bind("name");
        binder.bind(dateField, "date");
        binder.bind(setsCombo, "numberOfSets");
        binder.bind(typeCombo, "type");
        
        organizationCombo.setValue(Organization.KVTK);
        dateField.setValue(Clock.today());
        setsCombo.setValue(3);
        typeCombo.setValue(Type.SIMPLE_BOARD);
    }

    private void save() {
        
        TournamentInfoBean tournamentInfoBean = new TournamentInfoBean();
        boolean valid = binder.writeBeanIfValid(tournamentInfoBean);
        if(valid) {
            Tournament tournament = tournamentService.createTournament(
                    tournamentInfoBean.getOrganization(),
                    tournamentInfoBean.getName(),
                    "",
                    tournamentInfoBean.getDate(),
                    tournamentInfoBean.getType(),
                    tournamentInfoBean.getNumberOfSets());
            
            close();
            callback.accept(tournament);
        } else {
            KITSNotification.showError("Hibás adatok");
        }
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
    
    public static class TournamentInfoBean {
        
        private Organization organization;
        private String name;
        private LocalDate date;
        private int numberOfSets;
        private Tournament.Type type;
        public Organization getOrganization() {
            return organization;
        }
        public void setOrganization(Organization organization) {
            this.organization = organization;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public LocalDate getDate() {
            return date;
        }
        public void setDate(LocalDate date) {
            this.date = date;
        }
        public int getNumberOfSets() {
            return numberOfSets;
        }
        public void setNumberOfSets(int numberOfSets) {
            this.numberOfSets = numberOfSets;
        }
        public Tournament.Type getType() {
            return type;
        }
        public void setType(Tournament.Type type) {
            this.type = type;
        }
    }
    
}
