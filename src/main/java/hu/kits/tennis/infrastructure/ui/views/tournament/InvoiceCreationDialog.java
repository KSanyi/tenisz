package hu.kits.tennis.infrastructure.ui.views.tournament;

import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.NumberField;

import hu.kits.tennis.Main;
import hu.kits.tennis.domain.invoice.InvoiceService;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.tournament.PaymentStatus;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentService;
import hu.kits.tennis.infrastructure.ui.vaadin.util.UIUtils;

public class InvoiceCreationDialog extends Dialog {

    private final TournamentService tournamentService = Main.applicationContext.getTournamentService();
    private final InvoiceService invoiceService = Main.applicationContext.getInvoiceService();
    
    private final Tournament tournament;
    
    private final NumberField amountField = new NumberField("Számla összege");
    private final InvoicingGrid invoicingGrid = new InvoicingGrid();
    
    private final Button createInvoicesButton = UIUtils.createPrimaryButton("Számla készítése");
    
    public InvoiceCreationDialog(String tournamentId) {
        add(amountField, invoicingGrid);
        
        setHeaderTitle("Számla készítés");
        getFooter().add(createInvoicesButton);
        
        tournament = tournamentService.findTournament(tournamentId).get();
        
        List<Player> playersToSendInvoice = tournament.contestants().stream()
            .filter(c -> c.paymentStatus() == PaymentStatus.PAID)
            .map(c -> c.player())
            .toList();
        
        invoicingGrid.setItems(playersToSendInvoice);
        
        createInvoicesButton.addClickListener(click -> createInvoices(playersToSendInvoice));
    }

    private void createInvoices(List<Player> playersToSendInvoice) {
        
        List<String> emails = playersToSendInvoice.stream().map(p -> p.contact().email()).toList();
        
        List<String> emailsWithSucess = invoiceService.createAndSendInvoices(emails, amountField.getValue().intValue());
        
        for(String email : emailsWithSucess) {
            Player player = playersToSendInvoice.stream().filter(p -> p.contact().email().equals(email)).findAny().get();
            tournamentService.setPaymentStatus(tournament, player, PaymentStatus.INVOICE_SENT);    
        }
        
        close();
        UI.getCurrent().getPage().reload();
    }

}

class InvoicingGrid extends Grid<Player> {
    
    InvoicingGrid() {
        
        addColumn(player -> player.name())
            .setHeader("Név")
            .setAutoWidth(true)
            .setFlexGrow(1);
        
        setAllRowsVisible(true);
    }
    
}
