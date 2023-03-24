package hu.kits.tennis.infrastructure.ui.views.tournament;

import java.time.LocalDate;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.Clock;
import hu.kits.tennis.domain.tournament.Organization;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentParams;
import hu.kits.tennis.domain.tournament.TournamentParams.Level;
import hu.kits.tennis.domain.tournament.TournamentParams.Structure;
import hu.kits.tennis.domain.tournament.TournamentParams.Type;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.infrastructure.ui.component.ComponentFactory;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class NewTournamentDialog extends Dialog {

    private final TournamentService tournamentService = Main.resourceFactory.getTournamentService();
    
    private final Consumer<Tournament> callback;
    
    private final Select<Organization> organizationSelect = ComponentFactory.createSelect("Szervező", o -> o.name, Organization.values());
    private final Select<Type> typeSelect = ComponentFactory.createSelect("Típus", t -> t.label, Type.values());
    private final Select<Level> levelSelect = ComponentFactory.createSelect("Szint", l -> String.valueOf(l.value), Level.values());
    private final TextField nameField = new TextField("Név");
    private final DatePicker dateField = ComponentFactory.createHungarianDatePicker("Dátum");
    private final Select<Integer> setsSelect = ComponentFactory.createSelect("Szettek", i -> i.toString(), 1, 3, 5);
    private final Select<Structure> structureSelect = ComponentFactory.createSelect("Lebonyolítás", t -> t.label, Structure.values());
    
    private final Button saveButton = UIUtils.createPrimaryButton("Mentés");
    
    private final Binder<TournamentParamsBean> binder = new Binder<>(TournamentParamsBean.class);

    public NewTournamentDialog(Consumer<Tournament> callback) {
        this.callback = callback;
        
        bind();
        
        add(createContent());
        nameField.focus();
        saveButton.addClickListener(click -> save());
    }

    private void bind() {
        
        binder.bind(organizationSelect, "organization");
        binder.bind(typeSelect, "type");
        binder.bind(levelSelect, "levelFrom");
        binder.forField(nameField)
            .asRequired("Kötelező mező")
            .bind("name");
        binder.bind(dateField, "date");
        binder.bind(setsSelect, "bestOfNSets");
        binder.bind(structureSelect, "structure");
        
        organizationSelect.setValue(Organization.KVTK);
        typeSelect.setValue(Type.DAILY);
        levelSelect.setValue(Level.L500);
        dateField.setValue(Clock.today());
        setsSelect.setValue(3);
        structureSelect.setValue(Structure.SIMPLE_BOARD);
    }

    private void save() {
        
        TournamentParamsBean tournamentParamsBean = new TournamentParamsBean();
        boolean valid = binder.writeBeanIfValid(tournamentParamsBean);
        if(valid) {
            Tournament tournament = tournamentService.createTournament(tournamentParamsBean.toTournamentParams());
            close();
            callback.accept(tournament);
        } else {
            KITSNotification.showError("Hibás adatok");
        }
    }

    private Component createContent() {
        
        organizationSelect.setWidth("200px");
        typeSelect.setWidth("150px");
        levelSelect.setWidth("100px");
        nameField.setWidth("200px");
        dateField.setWidth("150px");
        setsSelect.setWidth("80px");
        structureSelect.setWidth("200px");
        
        VerticalLayout layout = new VerticalLayout(organizationSelect, typeSelect, levelSelect, nameField, dateField, setsSelect, structureSelect);
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setAlignSelf(Alignment.CENTER, saveButton);
        getFooter().add(saveButton);
        return layout;
    }
    
    public static class TournamentParamsBean {
        
        Organization organization;
        Type type;
        Level levelFrom;
        Level levelTo;
        LocalDate date; 
        String name;
        String venue = "";
        Structure structure;
        int bestOfNSets;
        
        public TournamentParams toTournamentParams() {
            return new TournamentParams(organization, type, levelFrom, levelFrom, date, name, venue, structure, bestOfNSets);
        }

        public Organization getOrganization() {
            return organization;
        }

        public void setOrganization(Organization organization) {
            this.organization = organization;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public Level getLevelFrom() {
            return levelFrom;
        }

        public void setLevelFrom(Level levelFrom) {
            this.levelFrom = levelFrom;
        }

        public Level getLevelTo() {
            return levelTo;
        }

        public void setLevelTo(Level levelTo) {
            this.levelTo = levelTo;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVenue() {
            return venue;
        }

        public void setVenue(String venue) {
            this.venue = venue;
        }

        public Structure getStructure() {
            return structure;
        }

        public void setStructure(Structure structure) {
            this.structure = structure;
        }

        public int getBestOfNSets() {
            return bestOfNSets;
        }

        public void setBestOfNSets(int bestOfNSets) {
            this.bestOfNSets = bestOfNSets;
        }
    }
    
}
