package hu.kits.tennis.application.usecase;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.domain.invoice.InvoiceService;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.tournament.PaymentStatus;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentService;

public class InvoicingUseCase {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final TournamentService tournamentService;
    private final InvoiceService invoiceService;
    
    public InvoicingUseCase(TournamentService tournamentService, InvoiceService invoiceService) {
        this.tournamentService = tournamentService;
        this.invoiceService = invoiceService;
    }
    
    public void createInvoices(List<Player> players, int amount, Tournament tournament) {
        
        logger.info("Creating {} Ft invoice for {} for {}", amount, tournament.params().name(), players);
        
        List<String> emails = players.stream().map(p -> p.contact().email()).toList();
        
        List<String> emailsWithSuccess = invoiceService.createAndSendInvoices(emails, amount);
        logger.info("Success for {} from {}", emailsWithSuccess.size(), players.size());
        
        for(String email : emailsWithSuccess) {
            Player player = players.stream().filter(p -> p.contact().email().equals(email)).findAny().get();
            tournamentService.setPaymentStatus(tournament, player, PaymentStatus.INVOICE_SENT);    
        }
        
    }
    
}
