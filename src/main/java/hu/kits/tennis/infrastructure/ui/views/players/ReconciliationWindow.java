package hu.kits.tennis.infrastructure.ui.views.players;

import static java.util.stream.Collectors.toList;

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
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
    
    private final OtherPlayersGrid playersGrid;
    private final Player player;
    private final Runnable callback;
    private final Button reconcileButton = UIUtils.createPrimaryButton("Összevon");
    
    public ReconciliationWindow(Player player, Runnable callback) {
        this.player = player;
        this.callback = callback;
        this.playersGrid = new OtherPlayersGrid(loadOtherPlayers(player));
        
        setDraggable(true);
        setResizable(true);
        
        add(createContent());
        
        reconcileButton.setEnabled(false);
        playersGrid.addSelectionListener(e -> reconcileButton.setEnabled(e.getFirstSelectedItem().isPresent()));
        reconcileButton.addClickListener(click -> reconcile());
        
        setWidth("600px");
    }
    
    private List<Player> loadOtherPlayers(Player player) {
        return playersService.loadAllPlayers().entries().stream()
                .filter(p -> !player.id().equals(p.id()))
                .collect(toList());
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
        layout.setHorizontalComponentAlignment(Alignment.CENTER, reconcileButton);
        return layout;
    }
    
    private static class OtherPlayersGrid extends Grid<Player> {

        public OtherPlayersGrid(List<Player> players) {
            addColumn(Player::id)
                .setHeader("Id")
                .setSortable(true)
                .setFlexGrow(1);
            
            addColumn(Player::name)
                .setHeader("Név")
                .setSortable(true)
                .setFlexGrow(3);
            
            addColumn(Player::startingUTR)
                .setHeader("Induló UTR")
                .setSortable(true)
                .setFlexGrow(1);
            
            setItems(players);
        }
        
    }

}
