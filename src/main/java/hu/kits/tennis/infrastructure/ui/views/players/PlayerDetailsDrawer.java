package hu.kits.tennis.infrastructure.ui.views.players;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.DoubleRangeValidator;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.RegexpValidator;

import hu.kits.tennis.common.KITSException;
import hu.kits.tennis.domain.tournament.Organizer;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.Player.Contact;
import hu.kits.tennis.domain.utr.PlayersService;
import hu.kits.tennis.domain.utr.UTR;
import hu.kits.tennis.infrastructure.ui.component.ConfirmationDialog;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
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
    private final TextArea commentField = new TextArea("Megjegyzés");
    private final NumberField startingUTRField = new NumberField("Induló UTR");
    private final Checkbox kvtkCheckBox = new Checkbox("KVTK tag");
    private final Binder<PlayerDataBean> binder = new Binder<>(PlayerDataBean.class);

    private final Button reconcileButton = UIUtils.createPrimaryButton("Összevon");
    
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
        reconcileButton.addClickListener(click -> openReconciliationWindow());
        
        idField.setReadOnly(true);
    }
    
    private void openReconciliationWindow() {
        new ReconciliationWindow(player, () -> playersView.refresh()).open();
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
        
        binder.forField(commentField)
            .bind("comment");
        
        binder.forField(startingUTRField)
            .withValidator(new DoubleRangeValidator("1 es 16 között", 1., 16.))
            .bind("startingUTR");
        
        binder.forField(kvtkCheckBox).bind(p -> p.getOrganisations().contains(Organizer.KVTK), (p, b) -> p.addKVTK(b));
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
            boolean isDeleted = playersService.deletePlayer(player);
            if(isDeleted) {
                KITSNotification.showInfo("Játékos törölve");
                hide();
                playersView.refresh();    
            } else {
                KITSNotification.showError("Nem lehet törölni a játékost, mert vannak már mérkőzései");
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
                commentField,
                startingUTRField,
                kvtkCheckBox,
                new Hr(),
                deleteButton,
                reconcileButton);
        fieldsLayout.setSpacing(false);
        fieldsLayout.setAlignSelf(Alignment.END, deleteButton);
        
        idField.setWidth("300px");
        nameField.setWidth("300px");
        emailField.setWidth("300px");
        phoneField.setWidth("300px");
        startingUTRField.setWidth("120px");
        
        startingUTRField.setMin(1);
        startingUTRField.setMax(16);
        startingUTRField.setStep(0.01);
        startingUTRField.setHasControls(true);
        
        return fieldsLayout;
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
        private String comment;
        private Double startingUTR;
        private Set<Organizer> organisations;
        
        public PlayerDataBean(Player player) {
            this.playerId = player.id().toString();
            this.name = player.name();
            this.email = player.contact().email();
            this.phone = player.contact().phone();
            this.comment = player.contact().comment();
            this.startingUTR = player.startingUTR().value();
            this.organisations = new HashSet<>(player.organisations());
        }
        
        public Player toPlayer() {
            return new Player(!playerId.isEmpty() ? Integer.parseInt(playerId) : null, name, new Contact(email, phone, comment), UTR.of(startingUTR), organisations);
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

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public Double getStartingUTR() {
            return startingUTR;
        }
        
        public void setStartingUTR(Double startingUTR) {
            this.startingUTR = startingUTR;
        }

        public Set<Organizer> getOrganisations() {
            return organisations;
        }
        
        public void addKVTK(boolean containsKVTK) {
            if(containsKVTK) {
                organisations.add(Organizer.KVTK);    
            } else {
                organisations.remove(Organizer.KVTK);
            }
        }

    }
    
    private static class PhoneValidator implements Validator<String> {

        private final RegexpValidator regexpValidator = new RegexpValidator("Hibás telefonszám: a helyes formátum: +36/70-123-1234, +39/12-1234-1234", 
                "\\+\\d{2}/\\d{2}\\-\\d{3,4}-\\d{4}");
        
        @Override
        public ValidationResult apply(String value, ValueContext context) {
            return value.isEmpty() ? ValidationResult.ok() : regexpValidator.apply(value, context);
        }
        
    }
    
}
