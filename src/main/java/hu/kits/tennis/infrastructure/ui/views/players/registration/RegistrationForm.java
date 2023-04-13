package hu.kits.tennis.infrastructure.ui.views.players.registration;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.RegexpValidator;

import hu.kits.tennis.domain.player.registration.Registration.RegistrationData;
import hu.kits.tennis.infrastructure.ui.component.ComponentFactory;

class RegistrationForm extends VerticalLayout {
    
    private final TextField nameField = new TextField("Név");
    private final TextField phoneField = new TextField("Telefonszám");
    private final EmailField emailField = new EmailField("Email cím");
    private final IntegerField zipField = new IntegerField("Irányítószám");
    private final TextField townField = new TextField("Város");
    private final TextArea streetAddressField = new TextArea("Utca, házszám");
    private final Select<String> experienceSelect = ComponentFactory.createSelect("Mióta teniszezel?", s->s, "Gyerekorom óta", "10 éve", "2-3 éve", "Most kezdtem");
    private final Select<String> frequencySelect = ComponentFactory.createSelect("Milyen gyakran teniszezel?", s->s, "Havonta 1x", "Hetente 1x", "Hetente többször");
    private final TextArea venueField = new TextArea("Hol játszol rendszeresen?");
    private final Select<String> hasPlayedInTournamentSelect = ComponentFactory.createSelect("Játszottál-e már versenyen?", s->s, "Igen", "Nem");
    
    private final Binder<RegistrationDataBean> binder = new Binder<>(RegistrationDataBean.class);
    
    RegistrationForm() {

        setWidthFull();
        
        nameField.setWidth("280px");
        phoneField.setWidth("280px");
        phoneField.setTooltipText("Formátum: +36/20-123-4567");
        //phoneField.setPattern("[0-9]*");
        emailField.setWidth("280px");
        zipField.setWidth("100px");
        townField.setWidth("165px");
        streetAddressField.setWidth("280px");
        streetAddressField.setHeight("90px");
        experienceSelect.setWidth("200px");
        frequencySelect.setWidth("200px");
        venueField.setWidth("280px");
        hasPlayedInTournamentSelect.setWidth("200px");
        
        bind();
        
        zipField.addValueChangeListener(e -> zipChanged(e.getValue()));
        
        add(nameField,
            phoneField,
            emailField,
            new HorizontalLayout(zipField, townField), 
            streetAddressField,
            experienceSelect,
            frequencySelect,
            venueField,
            hasPlayedInTournamentSelect);
        
        setSpacing(false);
        setPadding(false);
        setSizeUndefined();
    }

    private void zipChanged(Integer value) {
        if(value != null && 1000 <= value && value <=1999) {
            townField.setValue("Budapest");
        }
    }
    
    private void bind() {
        binder.forField(nameField)
            .asRequired("Kötelező mező")
            .bind("name");
        
        binder.forField(phoneField)
            .asRequired("Kötelező mező")
            .withValidator(new RegexpValidator("Helytelen telefonszám, a helyes formátum: +36/20-123-4567", "\\+\\d{1,2}/\\d{2}\\-\\d{3}-\\d{4}"))
            .bind("phone");
        
        binder.forField(emailField)
            .asRequired("Kötelező mező")
            .withValidator(new EmailValidator("Helytelen email cím"))
            .bind("email");
        
        binder.forField(zipField)
            .asRequired("Kötelező mező")
            .bind("zip");
        
        binder.forField(townField)
            .asRequired("Kötelező mező")
            .bind("town");
        
        binder.forField(streetAddressField)
            .asRequired("Kötelező mező")
            .bind("streetAddress");
        
        binder.forField(experienceSelect)
            .asRequired("Kötelező mező")
            .bind("experience");
        
        binder.forField(frequencySelect)
            .asRequired("Kötelező mező")
            .bind("playFrequency");
        
        binder.forField(venueField)
            .asRequired("Kötelező mező")
            .bind("venue");
        
        binder.forField(hasPlayedInTournamentSelect)
            .asRequired("Kötelező mező")
            .bind("hasPlayedInTournament");
    }
    
    boolean writeBeanIfValid(RegistrationDataBean bean) {
        return binder.writeBeanIfValid(bean);
    }
    
    void setRegistrationData(RegistrationData data) {
        binder.readBean(new RegistrationDataBean(data));
    }
    
    void setReadOnly() {
        binder.setReadOnly(true);
    }
    
    public static class RegistrationDataBean {
        
        private String name;
        private String phone;
        private String email;
        private Integer zip;
        private String town;
        private String streetAddress;
        private String experience;
        private String playFrequency;
        private String venue;
        private String hasPlayedInTournament;
        
        public RegistrationDataBean(RegistrationData data) {
            name = data.name();
            phone = data.phone();
            email = data.email();
            zip = data.zip();
            town = data.town();
            streetAddress = data.streetAddress();
            experience = data.experience();
            playFrequency = data.playFrequency();
            venue = data.venue();
            hasPlayedInTournament = data.hasPlayedInTournament();
        }
        
        RegistrationDataBean(){}

        public RegistrationData toRegistration() {
            return new RegistrationData(
                    name, 
                    phone, 
                    email, 
                    zip, 
                    town, 
                    streetAddress, 
                    experience, 
                    playFrequency, 
                    venue, 
                    hasPlayedInTournament);
        }
        
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
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
        public Integer getZip() {
            return zip;
        }
        public void setZip(Integer zip) {
            this.zip = zip;
        }
        public String getTown() {
            return town;
        }
        public void setTown(String town) {
            this.town = town;
        }
        public String getStreetAddress() {
            return streetAddress;
        }
        public void setStreetAddress(String streetAddress) {
            this.streetAddress = streetAddress;
        }
        public String getExperience() {
            return experience;
        }
        public void setExperience(String experience) {
            this.experience = experience;
        }
        public String getPlayFrequency() {
            return playFrequency;
        }
        public void setPlayFrequency(String playFrequency) {
            this.playFrequency = playFrequency;
        }
        public String getVenue() {
            return venue;
        }
        public void setVenue(String venue) {
            this.venue = venue;
        }
        public String getHasPlayedInTournament() {
            return hasPlayedInTournament;
        }
        public void setHasPlayedInTournament(String hasPlayedInTournament) {
            this.hasPlayedInTournament = hasPlayedInTournament;
        }
    }
    
}
