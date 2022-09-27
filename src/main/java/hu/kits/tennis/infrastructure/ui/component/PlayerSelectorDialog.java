package hu.kits.tennis.infrastructure.ui.component;

import java.util.function.Consumer;

import com.vaadin.flow.component.dialog.Dialog;

import hu.kits.tennis.domain.utr.Player;

public class PlayerSelectorDialog extends Dialog {

    public PlayerSelectorDialog(Consumer<Player> callback) {
        
        setDraggable(true);
        setResizable(true);
        
        Consumer<Player> extendedCallback = player -> {
            callback.accept(player);
            //close();
        };
        
        PlayerSelector playerSelector = new PlayerSelector(extendedCallback);
        
        add(playerSelector);
        
        setWidth("550px");
    }
    
}
