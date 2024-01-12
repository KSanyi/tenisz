package hu.kits.tennis.infrastructure.ui.views.players;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.DoubleRangeValidator;
import com.vaadin.flow.data.validator.EmailValidator;

import hu.kits.tennis.common.KITSException;
import hu.kits.tennis.domain.ktr.KTR;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.Player.Address;
import hu.kits.tennis.domain.player.Player.Contact;
import hu.kits.tennis.domain.player.PlayersService;
import hu.kits.tennis.domain.tournament.Organization;
import hu.kits.tennis.infrastructure.ui.component.ConfirmationDialog;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.util.DataValidator.PhoneValidator;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
import hu.kits.tennis.infrastructure.ui.vaadin.components.detailsdrawer.DetailsDrawer;
import hu.kits.tennis.infrastructure.ui.vaadin.components.detailsdrawer.DetailsDrawerHeader;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

class PlayerDetailsDrawer extends DetailsDrawer {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final PlayersService playersService;
    
    private final TextField idField = new TextField("Azonosító");
    private final TextField nameField = new TextField("Név");
    private final EmailField emailField = new EmailField ("Email");
    private final TextField phoneField = new TextField("Telefonszám");
    private final IntegerField zipField = new IntegerField("Irányítószám");
    private final TextField townField = new TextField("Város");
    private final TextArea streetAddressField = new TextArea("Utca, házszám");
    private final TextArea commentField = new TextArea("Megjegyzés");
    private final NumberField startingKTRField = new NumberField("Induló KTR");
    private final Checkbox kvtkCheckBox = new Checkbox("KVTK tag");
    private final Binder<PlayerDataBean> binder = new Binder<>(PlayerDataBean.class);

    private final Button saveButton = UIUtils.createPrimaryButton("Mentés");
    private final Button deleteButton = UIUtils.createErrorButton(VaadinIcon.TRASH);
    
    private final PlayersView playersView;
    
    private Player player;
    
    PlayerDetailsDrawer(PlayersService playersService, DetailsDrawer.Position position, PlayersView playersView) {
        super(position);
        
        this.playersService = playersService;
        this.playersView = playersView;
        
        setHeader(createHeader());
        setContent(createContent());
        setFooter(createFooter());
        
        bind();
        
        binder.addValueChangeListener(e -> saveButton.setVisible(binder.hasChanges()));
        saveButton.addClickListener(click -> save());
        deleteButton.addClickListener(click -> delete());
        
        idField.setReadOnly(true);
    }
    
    private void bind() {
        binder.bind(idField, "playerId");
        binder.forField(nameField)
            .asRequired("Kötelező mező")
            .bind("name");
        
        binder.forField(emailField)
            .withValidator(new EmailValidator("Hibás email cím", true))
            .bind("email");
        
        binder.forField(phoneField)
            .withValidator(new PhoneValidator())
            .bind("phone");
        
        binder.forField(emailField)
            .withValidator(new EmailValidator("Helytelen email cím"))
            .bind("email");
        
        binder.forField(zipField)
            .bind("zip");
        
        binder.forField(townField)
            .bind("town");
        
        binder.forField(streetAddressField)
            .bind("streetAddress");
        
        binder.forField(commentField)
            .bind("comment");
        
        binder.forField(startingKTRField)
            .withValidator(new DoubleRangeValidator("1 es 16 között", 1., 16.))
            .bind("startingKTR");
        
        binder.forField(kvtkCheckBox).bind(p -> p.getOrganisations().contains(Organization.KVTK), (p, b) -> p.addKVTK(b));
    }

    private void save() {
        String id = idField.getValue();
        PlayerDataBean playerDataBean = new PlayerDataBean();
        boolean valid = binder.writeBeanIfValid(playerDataBean);
        if(valid) {
            try {
                if(!id.isEmpty()) {
                    Player updatedPlayer = playerDataBean.toPlayer();
                    VaadinUtil.logUserAction(logger, "updating player: {}", updatedPlayer);
                    playersService.updatePlayer(updatedPlayer);
                    KITSNotification.showInfo("Játékos adatok frissítve");    
                } else {
                    Player newPlayer = playerDataBean.toPlayer();
                    VaadinUtil.logUserAction(logger, "saving new player: {}", newPlayer);
                    playersService.saveNewPlayer(newPlayer);
                    KITSNotification.showInfo("Játékos létrehozva");
                }
                hide();
                playersView.refresh();
            } catch(KITSException ex) {
                KITSNotification.showError(ex.getMessage());
            }
        } else {
            KITSNotification.showError("Hibás adatok");
        }
    }
    
    private void delete() {
        new ConfirmationDialog("Biztosan törölni akarod a " + player.name() + " nevű játékost?", () -> {
            Optional<String> errorMessage = playersService.deletePlayer(player);
            if(errorMessage.isEmpty()) {
                KITSNotification.showInfo("Játékos törölve");
                hide();
                playersView.refresh();    
            } else {
                KITSNotification.showError("Nem lehet törölni a játékost: " + errorMessage.get());
            }
        }).open();
    }
    
    private Component createHeader() {
        DetailsDrawerHeader detailsDrawerHeader = new DetailsDrawerHeader("Felhasználó");
        detailsDrawerHeader.addCloseListener(buttonClickEvent -> hide());
        return detailsDrawerHeader;
    }
    
    private Component createContent() {
        VerticalLayout fieldsLayout = new VerticalLayout(idField, 
                nameField,
                emailField,
                phoneField,
                zipField,
                townField, 
                streetAddressField, 
                commentField,
                startingKTRField,
                kvtkCheckBox,
                new Hr(),
                deleteButton);
        fieldsLayout.setSpacing(false);
        fieldsLayout.setAlignSelf(Alignment.END, deleteButton);
        
        idField.setWidth("300px");
        nameField.setWidth("300px");
        emailField.setWidth("300px");
        phoneField.setWidth("300px");
        zipField.setWidth("100px");
        townField.setWidth("250px");
        streetAddressField.setWidth("300px");
        streetAddressField.setHeight("90px");
        startingKTRField.setWidth("120px");
        
        zipField.addValueChangeListener(e -> zipChanged(e.getValue()));
        
        startingKTRField.setMin(1);
        startingKTRField.setMax(16);
        startingKTRField.setStep(0.01);
        startingKTRField.setStepButtonsVisible(true);
        
        return fieldsLayout;
    }
    
    private void zipChanged(Integer value) {
        if(value != null && 1000 <= value && value <=1999) {
            townField.setValue("Budapest");
        }
    }
    
    private Component createFooter() {
        saveButton.setWidthFull();
        saveButton.setHeight("50px");
        return saveButton;
    }
    
    private void initPlayer(PlayerDataBean playerDataBean) {
        binder.readBean(playerDataBean);
        deleteButton.setVisible(!playerDataBean.isNew());
        saveButton.setVisible(false);
    }
    
    void setPlayer(Player player) {
        this.player = player;
        initPlayer(new PlayerDataBean(player));
        VaadinUtil.logUserAction(logger, "viewing player: {}", player);
    }
    
    void setNewPlayer() {
        initPlayer(new PlayerDataBean());
        VaadinUtil.logUserAction(logger, "creating player");
    }
    
    public static class PlayerDataBean {
        
        private String playerId;
        private String name;
        private String email;
        private String phone;
        private Integer zip;
        private String town;
        private String streetAddress;
        private String comment;
        private Double startingKTR;
        private Set<Organization> organisations;
        
        public PlayerDataBean(Player player) {
            this.playerId = player.id().toString();
            this.name = player.name();
            this.email = player.contact().email();
            this.phone = player.contact().phone();
            this.comment = player.contact().comment();
            this.startingKTR = player.startingKTR().value();
            this.organisations = new HashSet<>(player.organisations());
            if(!player.contact().address().isEmpty()) {
                this.zip = player.contact().address().zip();
                this.town = player.contact().address().town();
                this.streetAddress = player.contact().address().streetAddress();
            }
        }
        
        public Player toPlayer() {
            
            Address address = zip != null ? new Address(zip, town, streetAddress) : Address.EMPTY;
            
            return new Player(!playerId.isEmpty() ? Integer.parseInt(playerId) : null,
                    name, 
                    new Contact(email, phone, address, comment), 
                    KTR.of(startingKTR), 
                    organisations);
        }

        public PlayerDataBean() {
            organisations = new HashSet<>();
        }

        public boolean isNew() {
            return playerId == null;
        }

        public String getPlayerId() {
            return playerId;
        }
        
        public void setPlayerId(String playerId) {
            this.playerId = playerId;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
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

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public Double getStartingKTR() {
            return startingKTR;
        }
        
        public void setStartingKTR(Double startingKTR) {
            this.startingKTR = startingKTR;
        }

        public Set<Organization> getOrganisations() {
            return organisations;
        }
        
        public void addKVTK(boolean containsKVTK) {
            if(containsKVTK) {
                organisations.add(Organization.KVTK);    
            } else {
                organisations.remove(Organization.KVTK);
            }
        }

    }
    
}
