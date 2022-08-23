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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.DoubleRangeValidator;

import hu.kits.tennis.common.KITSException;
import hu.kits.tennis.domain.tournament.Organizer;
import hu.kits.tennis.domain.utr.Player;
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
                startingUTRField,
                kvtkCheckBox,
                new Hr(),
                deleteButton,
                reconcileButton);
        fieldsLayout.setSpacing(false);
        fieldsLayout.setAlignSelf(Alignment.END, deleteButton);
        
        idField.setWidth("300px");
        nameField.setWidth("300px");
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
        private Double startingUTR;
        private Set<Organizer> organisations;
        
        public PlayerDataBean(Player player) {
            this.playerId = player.id().toString();
            this.name = player.name();
            this.startingUTR = player.startingUTR().value();
            this.organisations = new HashSet<>(player.organisations());
        }
        
        public Player toPlayer() {
            return new Player(!playerId.isEmpty() ? Integer.parseInt(playerId) : null, name, UTR.of(startingUTR), organisations);
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
    
}
