package hu.kits.tennis.infrastructure.ui.views.tournament;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.tournament.Contestant;
import hu.kits.tennis.domain.tournament.PaymentStatus;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.component.PlayerSelectorDialog;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;
import hu.kits.tennis.infrastructure.ui.views.tournament.ContestantsGrid.ContestantBean;

class ContestantsTable extends VerticalLayout {
    
    private final ContestantsGrid grid;
    
    private final Button addButton = UIUtils.createPrimaryButton("Versenyző", VaadinIcon.PLUS);
    private final Button createInvoiceButton = UIUtils.createPrimaryButton("Számla készítés", VaadinIcon.INVOICE);

    public ContestantsTable(String tournamentId, TournamentView tournamentView) {
        this.grid = new ContestantsGrid(tournamentView);
        
        setPadding(false);
        
        add(grid, new HorizontalLayout(addButton, createInvoiceButton));
        
        addButton.addClickListener(click -> openPlayerSelector());
        createInvoiceButton.addClickListener(click -> openInvoiceCreationDialog(tournamentId));
    }

    private static void openInvoiceCreationDialog(String tournamentId) {
        new InvoiceCreationDialog(tournamentId).open();
    }

    private void openPlayerSelector() {
        new PlayerSelectorDialog(grid::addPlayer).open();
    }

    void setPlayers(List<Contestant> contestants) {
        grid.setContestants(contestants.stream().map(ContestantBean::new).toList());
    }
    
    void setAddButtonVisible(boolean visible) {
        addButton.setVisible(visible);
    }
    
}

class ContestantsGrid extends Grid<hu.kits.tennis.infrastructure.ui.views.tournament.ContestantsGrid.ContestantBean> {

    private GridListDataView<ContestantBean> dataView;
    
    private ContestantBean draggedItem;
    
    private List<ContestantBean> items;
    
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
        
        addComponentColumn(item -> new PaymentStatusButton(tournamentView, item))
            .setHeader("Fizetés státusz")
            .setAutoWidth(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(1);
        
        setMaxWidth("500px");
        
        setAllRowsVisible(true);
        
        configurDragAndDrop();
        
        addItemClickListener(e -> handleClick(e));
    }
    
    private void handleClick(ItemClickEvent<ContestantBean> e) {
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
            ContestantBean targetPlayer = e.getDropTargetItem().orElse(null);
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
        List<ContestantBean> contestants = items.stream().map(gridItem -> gridItem).collect(toList());
        List<Player> players = contestants.stream().map(c -> c.player).collect(toList());
        if(!players.contains(playerToAdd)) {
            int index = players.indexOf(playerToRemove);
            contestants.remove(index);
            contestants.add(index, new ContestantBean(playerToAdd));
            setContestants(contestants);
            update();
            KITSNotification.showInfo(playerToAdd.name() + " hozzáadva a versenyhez " + playerToRemove.name() + " helyére");
        } else {
            KITSNotification.showError(playerToAdd.name() + " már hozzá van adva a versenyhez");
        }
    }
    
    void setContestants(List<ContestantBean> contestants) {
        items = new ArrayList<>(contestants);
        dataView = this.setItems(items);
    }
    
    void addPlayer(Player player) {
        List<ContestantBean> contestants = items.stream().map(gridItem -> gridItem).collect(toList());
        List<Player> players = contestants.stream().map(c -> c.player).collect(toList());
        if(!players.contains(player)) {
            contestants.add(new ContestantBean(player));
            setContestants(contestants);
            update();
            KITSNotification.showInfo(player.name() + " hozzáadva a versenyhez");
        } else {
            KITSNotification.showError(player.name() + " már hozzá van adva a versenyhez");
        }
    }
    
    private void update() {
        List<Contestant> contestants = items.stream().map(gridItem -> gridItem.toContestant()).collect(toList());
        tournamentView.updateContestants(contestants);
    }
    
    static class ContestantBean {
        
        private final Player player;
        private PaymentStatus paymentStatus;
        
        ContestantBean(Contestant contestant) {
            this.player = contestant.player();
            this.paymentStatus = contestant.paymentStatus();
        }
        
        ContestantBean(Player player) {
            this.player = player;
            this.paymentStatus = PaymentStatus.NOT_PAID;
        }
        
        Contestant toContestant() {
            return new Contestant(player, 0, paymentStatus);
        }
    }
    
    static class PaymentStatusButton extends Button {
        
        private final TournamentView tournamentView;
        private final ContestantBean contestantBean;
        
        PaymentStatusButton(TournamentView tournamentView, ContestantBean contestantBean) {
            this.tournamentView = tournamentView;
            this.contestantBean = contestantBean;
            addClickListener(click -> clicked());
            updateText();
            setWidth("110px");
            addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        }

        private void clicked() {
            PaymentStatus[] values = PaymentStatus.values();
            contestantBean.paymentStatus = values[(contestantBean.paymentStatus.ordinal()+1) % values.length];
            tournamentView.setPaymentStatus(contestantBean.player, contestantBean.paymentStatus);
            updateText();
        }
        
        private void updateText() {
            setText(contestantBean.paymentStatus.label);
            this.removeThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_ERROR);
            switch(contestantBean.paymentStatus) {
            case INVOICE_SENT: addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                break;
            case PAID: //addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                break;
            default: addThemeVariants(ButtonVariant.LUMO_ERROR);
                break;
            }
        }
    }
    
}
