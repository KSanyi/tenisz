package hu.kits.tennis.infrastructure.ui.views.tournament;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoIcon;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.ktr.PlayersWithKTR;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.tournament.Contestant;
import hu.kits.tennis.domain.tournament.PaymentStatus;
import hu.kits.tennis.infrastructure.ui.component.ConfirmationDialog;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.component.PlayerSelectorDialog;
import hu.kits.tennis.infrastructure.ui.util.VaadinUtil;
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

    void setContestants(List<Contestant> contestants) {
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
    
    private final PlayersWithKTR playersWithKTR = Main.applicationContext.getPlayersService().loadAllPlayersWithKTR();
    
    ContestantsGrid(TournamentView tournamentView) {
        
        this.tournamentView = tournamentView;
        
        addColumn(item -> items.indexOf(item) + 1)
            .setHeader("Rank")
            .setAutoWidth(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(0);
        
        addColumn(item -> formatNameAndKTR(item.player, playersWithKTR))
            .setHeader("Név")
            .setAutoWidth(true)
            .setFlexGrow(1);
        
        addComponentColumn(item -> item.player.equals(Player.BYE) ? null : new PaymentStatusButton(tournamentView, item))
            .setHeader("Fizetés státusz")
            .setAutoWidth(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(1);
        
        addComponentColumn(item -> item.player.equals(Player.BYE) ? null : createDeleteButton(item))
            .setAutoWidth(true)
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(1);
        
        setMaxWidth("600px");
        
        setAllRowsVisible(true);
        
        configurDragAndDrop();
    }
    
    private static String formatNameAndKTR(Player player, PlayersWithKTR playersWithKTR) {
        if(player.equals(Player.BYE)) {
            return "Bye";
        } else {
            return player.name() + " (" + playersWithKTR.getKTR(player.id()) + ")";
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
    
    private Button createDeleteButton(ContestantBean contestant) {
        Button deleteButton = new Button(LumoIcon.CROSS.create(), click -> new ConfirmationDialog("Biztosan eltávolítod " + contestant.player.name() + "-t a versenyről?", () -> deleteContestant(contestant)).open());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        return deleteButton;
    }
    
    private void deleteContestant(ContestantBean contestantToDelete) {
        List<ContestantBean> contestants = items.stream().map(gridItem -> gridItem).collect(toList());
        contestants.remove(contestantToDelete);
        setContestants(contestants);
        update();
        KITSNotification.showInfo(contestantToDelete.player.name() + " eltávolítva");
    }
    
    void setContestants(List<ContestantBean> contestants) {
        items = contestants.stream()
                .sorted(comparing((ContestantBean c) -> playersWithKTR.getKTR(c.player.id())).reversed())
                .collect(Collectors.toList());
        dataView = this.setItems(items);
    }
    
    void addPlayer(Player player) {
        List<ContestantBean> contestants = items.stream().map(gridItem -> gridItem).collect(toList());
        List<Player> players = contestants.stream().map(c -> c.player).collect(toList());
        if(!players.contains(player)) {
            
            int indexOfBye = players.indexOf(Player.BYE);
            if(indexOfBye > -1) {
                contestants.remove(indexOfBye);
                contestants.add(indexOfBye, new ContestantBean(player));
            } else {
                contestants.add(new ContestantBean(player));
            }
            
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
        
        final Player player;
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
            return new Contestant(player, 0, paymentStatus, 0);
        }
    }
    
    static class PaymentStatusButton extends Button {
        
        private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
        
        private final TournamentView tournamentView;
        private final ContestantBean contestantBean;
        
        PaymentStatusButton(TournamentView tournamentView, ContestantBean contestantBean) {
            
            this.tournamentView = tournamentView;
            this.contestantBean = contestantBean;
            addClickListener(click -> clicked());
            updateText();
            setWidth("120px");
            addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        }

        private void clicked() {
            PaymentStatus currentStatus = contestantBean.paymentStatus;
            PaymentStatus[] values = PaymentStatus.values();
            PaymentStatus nextStatus = values[(contestantBean.paymentStatus.ordinal()+1) % values.length];
            
            VaadinUtil.logUserAction(logger, "Clicked: {} -> {}", currentStatus, nextStatus);
            
            contestantBean.paymentStatus = nextStatus;
            tournamentView.setPaymentStatus(contestantBean.player, contestantBean.paymentStatus);
            updateText();
        }
        
        private void updateText() {
            setText(contestantBean.paymentStatus.label);
            this.removeThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_ERROR);
            switch(contestantBean.paymentStatus) {
            case INVOICE_SENT:
            case PAID_CASH: addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                break;
            case PAID: //addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                break;
            default: addThemeVariants(ButtonVariant.LUMO_ERROR);
                break;
            }
        }
    }
    
}
