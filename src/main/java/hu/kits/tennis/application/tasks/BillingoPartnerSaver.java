package hu.kits.tennis.application.tasks;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.domain.invoice.InvoiceService;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.PlayerRepository;
import hu.kits.tennis.infrastructure.ApplicationContext;

public class BillingoPartnerSaver {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final PlayerRepository playerRepository;
    private final InvoiceService invoiceService;

    public BillingoPartnerSaver(ApplicationContext applicationContext) {
        this.playerRepository = applicationContext.getPlayerRepository();
        this.invoiceService = applicationContext.getInvoiceService();
    }
    
    public void saveNewPartners() {
        
        List<Player> playersWithAddress = playerRepository.loadAllPlayers().entries().stream()
            .filter(p -> !p.contact().address().isEmpty())
            .toList();
        
        List<String> partnerEmailsInBillingo = invoiceService.getPartnerEmails();
        
        List<Player> playersWithAddressButNotInBillingo = playersWithAddress.stream()
                .filter(p -> !partnerEmailsInBillingo.contains(p.contact().email()))
                .toList();
        
        for(Player player : playersWithAddressButNotInBillingo) {
            logger.info("Creating player in billingo: {}", player);
            invoiceService.createPartnerForPlayer(player);
        }
    }
    
    
}