package hu.kits.tennis.infrastructure.ui.views.tournament;

import static java.util.stream.Collectors.toList;

import java.util.List;

import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.domain.utr.Player;

class ContestantsTable extends Grid<Player> {

    private final TournamentService tournamentService = Main.resourceFactory.getTournamentService();
    
    private GridListDataView<Player> dataView;
    
    private Player draggedItem;
    
    private List<Player> items;
    
    private final TournamentView tournamentView;
    
    ContestantsTable(TournamentView tournamentView) {
        
        this.tournamentView = tournamentView;
        
        addColumn(player -> items.indexOf(player) + 1)
            .setHeader("Rank")
            .setAutoWidth(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(0);
        
        addColumn(Player::name)
            .setHeader("Név")
            .setAutoWidth(true)
            .setFlexGrow(1);
        
        setWidth("400px");
        
        this.setAllRowsVisible(true);
        
        configurDragAndDrop();
    }
    
    private void configurDragAndDrop() {
        setRowsDraggable(true);
        setDropMode(GridDropMode.BETWEEN);
        
        addDragStartListener(e -> draggedItem = e.getDraggedItems().get(0));
        addDragEndListener(e -> draggedItem = null);
        
        addDropListener(e -> {
            Player targetPlayer = e.getDropTargetItem().orElse(null);
            GridDropLocation dropLocation = e.getDropLocation();

            if (targetPlayer != null || !draggedItem.equals(targetPlayer)) {
                dataView.removeItem(draggedItem);
                if (dropLocation == GridDropLocation.BELOW) {
                    dataView.addItemAfter(draggedItem, targetPlayer);
                } else {
                    dataView.addItemBefore(draggedItem, targetPlayer);
                }
                items = dataView.getItems().collect(toList());
                tournamentView.updateContestants(items);
            }
        });
    }
    
    void setPlayers(List<Player> players) {
        dataView = this.setItems(players);
        items = players;
    }
    
}
