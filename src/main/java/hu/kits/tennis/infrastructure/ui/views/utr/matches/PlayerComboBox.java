package hu.kits.tennis.infrastructure.ui.views.utr.matches;

import com.vaadin.flow.component.combobox.ComboBox;

import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.PlayerRepository;
import hu.kits.tennis.domain.utr.Players;
import hu.kits.tennis.infrastructure.ui.component.ConfirmationDialog;

public class PlayerComboBox extends ComboBox<Player> {

    private final PlayerRepository playerRepository;
    
    private Players players;
    
    public PlayerComboBox(Players players) {
        this(players, null);
    }
    
    public PlayerComboBox(Players players, PlayerRepository playerRepository) {
        this.players = players;
        this.playerRepository = playerRepository;
        
        setItemLabelGenerator(Player::name);
        setWidthFull();
        setItems(players.entries());
        if(!players.isEmpty()) {
            setValue(players.getOne());
        }
        
        if(playerRepository != null) {
            setAllowCustomValue(true);
            addCustomValueSetListener(e -> offerSavingNewPlayer(e.getDetail()));            
        }
    }
    
    private void offerSavingNewPlayer(String newPlayerName) {
        if(!players.containsPlayerWithName(newPlayerName)) {
            
            Runnable yesAction = () -> {
                Player player = playerRepository.saveNewPlayer(new Player(0, newPlayerName, 0));
                players = players.add(player);
                setItems(players.entries());
                setValue(player);
            };
            
            Runnable noAction = () -> {
              this.clear();  
            };
            
            new ConfirmationDialog(newPlayerName + " nevű játékos még nem létezik, elmented?", yesAction, noAction).open();
        }
    }

}
