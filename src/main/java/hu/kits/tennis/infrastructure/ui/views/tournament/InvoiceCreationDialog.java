package hu.kits.tennis.infrastructure.ui.views.tournament;

import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.NumberField;

import hu.kits.tennis.Main;
import hu.kits.tennis.application.usecase.InvoicingUseCase;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.tournament.PaymentStatus;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.infrastructure.ui.component.KITSNotification;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class InvoiceCreationDialog extends Dialog {

    private final InvoicingUseCase invoicingUseCase = Main.applicationContext.getInvoicingUseCase();
    private final TournamentService tournamentService = Main.applicationContext.getTournamentService();
    
    private final Tournament tournament;
    
    private final NumberField amountField = new NumberField("Számla összege");
    private final InvoicingGrid invoicingGrid = new InvoicingGrid();
    
    private final Button createInvoicesButton = UIUtils.createPrimaryButton("Számla készítése");
    private final Button cancelButton = UIUtils.createContrastButton("Mégsem");
    
    public InvoiceCreationDialog(String tournamentId) {
        add(amountField, invoicingGrid);
        
        setHeaderTitle("Számla készítés");
        getFooter().add(cancelButton, createInvoicesButton);
        
        tournament = tournamentService.findTournament(tournamentId).get();
        
        List<Player> playersToSendInvoice = tournament.contestants().stream()
            .filter(c -> c.paymentStatus() == PaymentStatus.PAID)
            .map(c -> c.player())
            .toList();
        
        invoicingGrid.setItems(playersToSendInvoice);
        
        createInvoicesButton.addClickListener(click -> createInvoices(playersToSendInvoice));
        cancelButton.addClickListener(click -> close());
    }

    private void createInvoices(List<Player> playersToSendInvoice) {
        
        if(amountField.getValue() != null && amountField.getValue() > 0) {
            invoicingUseCase.createInvoices(playersToSendInvoice, amountField.getValue().intValue(), tournament);
            close();
            UI.getCurrent().getPage().reload();            
        } else {
            KITSNotification.showError("A számla összegét meg kell adni");
        }
    }

}

class InvoicingGrid extends Grid<Player> {
    
    InvoicingGrid() {
        
        addColumn(player -> player.name())
            .setAutoWidth(true)
            .setFlexGrow(1);
        
        setAllRowsVisible(true);
    }
    
}
