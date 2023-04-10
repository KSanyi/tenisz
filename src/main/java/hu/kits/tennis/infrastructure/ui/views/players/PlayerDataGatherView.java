package hu.kits.tennis.infrastructure.ui.views.players;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;

import hu.kits.tennis.Main;
import hu.kits.tennis.application.AddPlayerAddressWorkflow;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.Player.Address;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.views.utr.ranking.PlayerStatsView;
import hu.kits.tennis.infrastructure.ui.views.utr.ranking.UTRRankingView;

@Route(value = "data-request")
@PageTitle("Adatbekérő")
public class PlayerDataGatherView extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final AddPlayerAddressWorkflow addPlayerAddressWorkflow = Main.applicationContext.getAddPlayerAddressWorkflow();
    
    private final TextField nameField = new TextField("Név");
    private final EmailField emailField = new EmailField("Email cím");
    private final IntegerField zipField = new IntegerField("Irányítószám");
    private final TextField townField = new TextField("Város");
    private final TextArea streetAddressField = new TextArea("Utca, házszám");
    
    private final Binder<AddressDataGatherBean> binder = new Binder<>(AddressDataGatherBean.class);
    
    private final Button saveButton = UIUtils.createPrimaryButton("Mentés");
    
    public PlayerDataGatherView() {

        logger.info("Init");
        
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setSpacing(false);
        
        Label info = new Label("""
                Kérlek töltsd ki a az alábbi űrlapot a későbbi hatékonyabb számlakészítés érdekében. Adataidat bizalmasan kezeljük. 
                """);
        
        nameField.setWidth("365px");
        emailField.setWidth("365px");
        zipField.setWidth("100px");
        townField.setWidth("250px");
        streetAddressField.setWidth("365px");
        streetAddressField.setHeight("90px");
        
        bind();
        
        zipField.addValueChangeListener(e -> zipChanged(e.getValue()));
        
        add(new H3("Adatbekérő"),
                info,
                nameField, 
                emailField, 
                new HorizontalLayout(zipField, townField), 
                streetAddressField, 
                saveButton);
        
        saveButton.addClickListener(click -> save());
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
        
        binder.forField(emailField)
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
    }

    private void save() {
        logger.info("Save button clicked");
        AddressDataGatherBean bean = new AddressDataGatherBean();
        boolean valid = binder.writeBeanIfValid(bean);
        if(valid) {
            Optional<Player> player = addPlayerAddressWorkflow.addPlayerAddress(bean.getName(), bean.getEmail(), bean.getAddress());
            KITSNotification.showInfo("Sikeres adatmentés!");
            if(player.isPresent()) {
                UI.getCurrent().navigate(PlayerStatsView.class, new RouteParameters("playerId" , String.valueOf(player.get().id())));
            } else {
                UI.getCurrent().navigate(UTRRankingView.class);                
            }
        } else {
            KITSNotification.showError("Hibás adatok");
        }
    }
    
    public static class AddressDataGatherBean {
        
        private String name;
        private String email;
        private Integer zip;
        private String town;
        private String streetAddress;
        
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }
        
        public Address getAddress() {
            return new Address(zip, town, streetAddress);
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
    }
    
}
