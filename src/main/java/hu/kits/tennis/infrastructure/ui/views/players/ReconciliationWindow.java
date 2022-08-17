package hu.kits.tennis.infrastructure.ui.views.players;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.user.ReconciliationService;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.PlayersService;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class ReconciliationWindow extends Dialog {

    private final PlayersService playersService = Main.resourceFactory.getPlayersService();
    private final ReconciliationService reconciliationService = Main.resourceFactory.getReconciliationService();
    
    private final PlayersGrid playersGrid;
    private final Player player;
    private final Runnable callback;
    private final Button reconcileButton = UIUtils.createPrimaryButton("Összevon");
    
    public ReconciliationWindow(Player player, Runnable callback) {
        this.player = player;
        this.callback = callback;
        this.playersGrid = new PlayersGrid(playersService);
        
        setDraggable(true);
        setResizable(true);
        
        add(createContent());
        
        reconcileButton.addClickListener(click -> reconcile());
        
        setWidth("600px");
    }

    private void reconcile() {
        
        Player duplicate = playersGrid.asSingleSelect().getValue();
        if(duplicate != null) {
            reconciliationService.reconcilePlayers(player, duplicate);
            callback.run();
            close();
            KITSNotification.showInfo(player.name() + " összevonva " + duplicate.name() + "-val.");
        }
    }

    private Component createContent() {
        VerticalLayout layout = new VerticalLayout(playersGrid, reconcileButton);
        return layout;
    }

}
