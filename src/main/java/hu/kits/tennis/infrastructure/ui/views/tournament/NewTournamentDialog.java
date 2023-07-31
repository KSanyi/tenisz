package hu.kits.tennis.infrastructure.ui.views.tournament;

import java.time.LocalDate;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import hu.kits.tennis.Main;
import hu.kits.tennis.common.Clock;
import hu.kits.tennis.domain.tournament.Organization;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentParams;
import hu.kits.tennis.domain.tournament.TournamentParams.Level;
import hu.kits.tennis.domain.tournament.TournamentParams.Structure;
import hu.kits.tennis.domain.tournament.TournamentParams.Surface;
import hu.kits.tennis.domain.tournament.TournamentParams.Type;
import hu.kits.tennis.domain.tournament.TournamentParams.VenueType;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.tournament.TournamentSummary.CourtInfo;
import hu.kits.tennis.infrastructure.ui.component.ComponentFactory;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class NewTournamentDialog extends Dialog {

    private final TournamentService tournamentService = Main.applicationContext.getTournamentService();
    
    private final Consumer<Tournament> callback;
    
    private final Select<Organization> organizationSelect = ComponentFactory.createSelect("Szervező", o -> o.name, Organization.values());
    private final Select<String> venueSelect = new Select<>();
    private final Select<Integer> numberOfCourtsSelect = ComponentFactory.createSelect("Pályák száma", String::valueOf, 1, 2, 3, 4, 5, 6, 7, 8);
    private final Select<TournamentParams.Surface> surfaceSelect = ComponentFactory.createSelect("Borítás", s -> s.label, TournamentParams.Surface.values());
    private final Select<TournamentParams.VenueType> venueTypeSelect = ComponentFactory.createSelect("Pálya", v -> v.label, TournamentParams.VenueType.values());
    private final Select<Type> typeSelect = ComponentFactory.createSelect("Típus", t -> t.label, Type.values());
    private final Select<Level> levelFromSelect = ComponentFactory.createSelect("Szint", l -> String.valueOf(l.value), Level.values());
    private final Select<Level> levelToSelect = ComponentFactory.createSelect("Szint", l -> String.valueOf(l.value), Level.values());
    private final TextField nameField = new TextField("Név");
    private final DatePicker dateField = ComponentFactory.createHungarianDatePicker("Dátum");
    private final Select<Integer> setsSelect = ComponentFactory.createSelect("Szettek", i -> i.toString(), 1, 3, 5);
    private final Select<Structure> structureSelect = ComponentFactory.createSelect("Lebonyolítás", t -> t.label, Structure.values());
    private final TextArea descriptionField = new TextArea("Leírás");
    
    private final Button saveButton = UIUtils.createPrimaryButton("Mentés");
    
    private final Binder<TournamentParamsBean> binder = new Binder<>(TournamentParamsBean.class);

    public NewTournamentDialog(Consumer<Tournament> callback) {
        this.callback = callback;
        
        venueSelect.setLabel("Helyszín");
        venueSelect.setItems(tournamentService.loadVenues());
        
        bind();
        
        add(createContent());
        nameField.focus();
        saveButton.addClickListener(click -> save());
    }

    private void bind() {
        
        binder.bind(organizationSelect, "organization");
        binder.bind(venueSelect, "venue");
        binder.bind(numberOfCourtsSelect, "numberOfCourts");
        binder.bind(surfaceSelect, "surface");
        binder.bind(venueTypeSelect, "venueType");
        binder.bind(typeSelect, "type");
        binder.bind(levelFromSelect, "levelFrom");
        binder.bind(levelToSelect, "levelTo");
        binder.forField(nameField)
            .asRequired("Kötelező mező")
            .bind("name");
        binder.bind(dateField, "date");
        binder.bind(setsSelect, "bestOfNSets");
        binder.bind(structureSelect, "structure");
        binder.bind(descriptionField, "description");
        
        organizationSelect.setValue(Organization.KVTK);
        venueSelect.setValue("Mini Garros");
        typeSelect.setValue(Type.DAILY);
        levelFromSelect.setValue(Level.L500);
        levelToSelect.setValue(Level.L500);
        dateField.setValue(Clock.today());
        setsSelect.setValue(3);
        structureSelect.setValue(Structure.SIMPLE_BOARD);
        numberOfCourtsSelect.setValue(4);
        surfaceSelect.setValue(Surface.CLAY);
        venueTypeSelect.setValue(VenueType.INDOOR);
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
        venueSelect.setWidth("200px");
        typeSelect.setWidth("150px");
        levelFromSelect.setWidth("90px");
        levelToSelect.setWidth("90px");
        nameField.setWidth("265px");
        dateField.setWidth("150px");
        setsSelect.setWidth("80px");
        structureSelect.setWidth("220px");
        numberOfCourtsSelect.setWidth("100px");
        surfaceSelect.setWidth("120px");
        venueTypeSelect.setWidth("120px");
        
        descriptionField.setWidth("350px");
        descriptionField.setHeight("100px");
        
        VerticalLayout layout = new VerticalLayout(organizationSelect,
                nameField,
                new HorizontalLayout(typeSelect, levelFromSelect, levelToSelect),
                new HorizontalLayout(dateField, venueSelect),
                new HorizontalLayout(numberOfCourtsSelect, surfaceSelect, venueTypeSelect),
                new HorizontalLayout(structureSelect, setsSelect),
                descriptionField);
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
        Surface surface;
        VenueType venueType;
        int numberOfCourts;
        Structure structure;
        int bestOfNSets;
        String description;
        
        public Surface getSurface() {
            return surface;
        }

        public void setSurface(Surface surface) {
            this.surface = surface;
        }

        public VenueType getVenueType() {
            return venueType;
        }

        public void setVenueType(VenueType venueType) {
            this.venueType = venueType;
        }

        public int getNumberOfCourts() {
            return numberOfCourts;
        }

        public void setNumberOfCourts(int numberOfCourts) {
            this.numberOfCourts = numberOfCourts;
        }

        public TournamentParams toTournamentParams() {
            return new TournamentParams(organization, type, levelFrom, levelTo, date, name, venue, new CourtInfo(numberOfCourts, surface, venueType), structure, bestOfNSets, description);
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
        
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
    
}
