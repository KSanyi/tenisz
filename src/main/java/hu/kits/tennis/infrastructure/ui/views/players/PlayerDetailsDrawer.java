package hu.kits.tennis.infrastructure.ui.views.players;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import hu.kits.tennis.common.KITSException;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.PlayersService;
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
    private final ComboBox<Integer> utrGroupCombo = new ComboBox<>("UTR csoport", List.of(5,6,7,8,9,10));
    private final Binder<PlayerDataBean> binder = new Binder<>(PlayerDataBean.class);

    private final Button saveButton = UIUtils.createPrimaryButton("Mentés");
    private final Button deleteButton = UIUtils.createErrorButton(VaadinIcon.TRASH);
    
    private final PlayersView playersView;
    
    private Player player;
    
    PlayerDetailsDrawer(PlayersService playersService, DetailsDrawer.Position position, PlayersView usersView) {
        super(position);
        
        this.playersService = playersService;
        this.playersView = usersView;
        
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
        binder.bind(utrGroupCombo, "utrGroup");
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
                    KITSNotification.showInfo("Játékos létrehozva.");
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
                utrGroupCombo,
                new Hr(),
                deleteButton);
        fieldsLayout.setSpacing(false);
        fieldsLayout.setAlignSelf(Alignment.END, deleteButton);
        
        idField.setWidth("300px");
        nameField.setWidth("300px");
        utrGroupCombo.setWidth("100px");
        
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
        private Integer utrGroup;
        
        public PlayerDataBean(Player player) {
            this.playerId = player.id().toString();
            this.name = player.name();
            this.utrGroup = player.utrGroup();
        }
        
        public Player toPlayer() {
            return new Player(!playerId.isEmpty() ? Integer.parseInt(playerId) : null, name, utrGroup);
        }

        public PlayerDataBean() {
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
        
        public Integer getUtrGroup() {
            return utrGroup;
        }
        
        public void setUtrGroup(Integer utrGroup) {
            this.utrGroup = utrGroup;
        }
        
    }
    
}
