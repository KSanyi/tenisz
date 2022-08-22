package hu.kits.tennis.infrastructure.ui.views.players;

import static java.util.stream.Collectors.toList;

import java.util.List;

import com.vaadin.flow.component.dialog.Dialog;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.user.ReconciliationService;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.PlayersService;
import hu.kits.tennis.infrastructure.ui.component.ConfirmationDialog;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.component.PlayerSelector;

public class ReconciliationWindow extends Dialog {

    private final PlayersService playersService = Main.resourceFactory.getPlayersService();
    private final ReconciliationService reconciliationService = Main.resourceFactory.getReconciliationService();
    
    private final PlayerSelector playerSelector;
    private final Player player;
    private final Runnable callback;
    
    public ReconciliationWindow(Player player, Runnable callback) {
        this.player = player;
        this.callback = callback;
        this.playerSelector = new PlayerSelector(this::reconcile, loadOtherPlayers(player));
        
        setDraggable(true);
        setResizable(true);
        
        add(playerSelector);
        
        setWidth("600px");
    }
    
    private List<Player> loadOtherPlayers(Player player) {
        return playersService.loadAllPlayers().entries().stream()
                .filter(p -> !player.id().equals(p.id()))
                .collect(toList());
    }

    private void reconcile(Player duplicate) {
        
        String question = String.format("Összevonod %s-t %s-val?", player.name(), duplicate.name());
        
        new ConfirmationDialog(question, () -> {
            reconciliationService.reconcilePlayers(player, duplicate);
            callback.run();
            close();
            KITSNotification.showInfo(player.name() + " összevonva " + duplicate.name() + "-val.");
        }).open();
    }

}
