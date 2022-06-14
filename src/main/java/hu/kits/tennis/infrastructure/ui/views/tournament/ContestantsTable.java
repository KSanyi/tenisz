package hu.kits.tennis.infrastructure.ui.views.tournament;

import static java.util.stream.Collectors.toList;

import java.util.List;

import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;

import hu.kits.tennis.domain.utr.Player;

class ContestantsTable extends Grid<hu.kits.tennis.infrastructure.ui.views.tournament.ContestantsTable.GridItem> {

    private GridListDataView<GridItem> dataView;
    
    private GridItem draggedItem;
    
    private List<GridItem> items;
    
    private final TournamentView tournamentView;
    
    ContestantsTable(TournamentView tournamentView) {
        
        this.tournamentView = tournamentView;
        
        addColumn(item -> items.indexOf(item) + 1)
            .setHeader("Rank")
            .setAutoWidth(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(0);
        
        addColumn(item -> item.player.name())
            .setHeader("Név")
            .setAutoWidth(true)
            .setFlexGrow(1);
        
        setWidth("300px");
        
        this.setAllRowsVisible(true);
        
        configurDragAndDrop();
    }
    
    private void configurDragAndDrop() {
        setRowsDraggable(true);
        setDropMode(GridDropMode.BETWEEN);
        
        addDragStartListener(e -> draggedItem = e.getDraggedItems().get(0));
        addDragEndListener(e -> draggedItem = null);
        
        addDropListener(e -> {
            GridItem targetPlayer = e.getDropTargetItem().orElse(null);
            GridDropLocation dropLocation = e.getDropLocation();

            if (targetPlayer != null || !draggedItem.equals(targetPlayer)) {
                dataView.removeItem(draggedItem);
                if (dropLocation == GridDropLocation.BELOW) {
                    dataView.addItemAfter(draggedItem, targetPlayer);
                } else {
                    dataView.addItemBefore(draggedItem, targetPlayer);
                }
                items = dataView.getItems().collect(toList());
                List<Player> players = items.stream().map(gridItem -> gridItem.player).collect(toList());
                tournamentView.updateContestants(players);
            }
        });
    }
    
    void setPlayers(List<Player> players) {
        items = players.stream().map(GridItem::new).collect(toList());
        dataView = this.setItems(items);
    }
    
    static class GridItem {
        final Player player;

        public GridItem(Player player) {
            this.player = player;
        }
        
    }
    
}
