package hu.kits.tennis.infrastructure.ui.views.tournament;

import static java.util.stream.Collectors.toList;

import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.component.PlayerSelectorDialog;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

class ContestantsTable extends VerticalLayout {
    
    private final ContestantsGrid grid;
    
    private final Button addButton = UIUtils.createPrimaryButton("Hozzáad", VaadinIcon.PLUS);

    public ContestantsTable(TournamentView tournamentView) {
        this.grid = new ContestantsGrid(tournamentView);
        
        setPadding(false);
        
        add(grid, addButton);
        setHorizontalComponentAlignment(Alignment.CENTER, addButton);
        
        addButton.addClickListener(click -> openPlayerSelector());
    }

    private void openPlayerSelector() {
        new PlayerSelectorDialog(grid::addPlayer).open();
    }

    void setPlayers(List<Player> playersLineup) {
        grid.setPlayers(playersLineup);
    }
    
}

class ContestantsGrid extends Grid<hu.kits.tennis.infrastructure.ui.views.tournament.ContestantsGrid.GridItem> {

    private GridListDataView<GridItem> dataView;
    
    private GridItem draggedItem;
    
    private List<GridItem> items;
    
    private final TournamentView tournamentView;
    
    ContestantsGrid(TournamentView tournamentView) {
        
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
        
        addItemClickListener(e -> handleClick(e));
    }
    
    private void handleClick(ItemClickEvent<GridItem> e) {
        if(e.getClickCount() > 1) {
            new PlayerSelectorDialog(player -> changePlayer(e.getItem().player, player)).open(); 
        }
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
                update();
            }
        });
    }
    
    // TODO refactor these methods
    
    private void changePlayer(Player playerToRemove, Player playerToAdd) {
        List<Player> players = items.stream().map(gridItem -> gridItem.player).collect(toList());
        if(!players.contains(playerToAdd)) {
            int index = players.indexOf(playerToRemove);
            players.remove(index);
            players.add(index, playerToAdd);
            setPlayers(players);
            update();
            KITSNotification.showInfo(playerToAdd.name() + " hozzáadva a versenyhez " + playerToAdd.name() + " helyére");
        } else {
            KITSNotification.showError(playerToAdd.name() + " már hozzá van adva a versenyhez");
        }
    }
    
    void setPlayers(List<Player> players) {
        items = players.stream().map(GridItem::new).collect(toList());
        dataView = this.setItems(items);
    }
    
    void addPlayer(Player player) {
        List<Player> players = items.stream().map(gridItem -> gridItem.player).collect(toList());
        if(!players.contains(player)) {
            players.add(player);
            setPlayers(players);
            update();
            KITSNotification.showInfo(player.name() + " hozzáadva a versenyhez");
        } else {
            KITSNotification.showError(player.name() + " már hozzá van adva a versenyhez");
        }
        
    }
    
    private void update() {
        List<Player> players = items.stream().map(gridItem -> gridItem.player).collect(toList());
        tournamentView.updateContestants(players);
    }
    
    static class GridItem {
        final Player player;

        public GridItem(Player player) {
            this.player = player;
        }
    }
    
}
