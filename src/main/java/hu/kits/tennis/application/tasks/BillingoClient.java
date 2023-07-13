package hu.kits.tennis.application.tasks;

import static java.util.stream.Collectors.joining;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.domain.invoice.InvoiceService;
import hu.kits.tennis.domain.invoice.InvoiceService.Partner;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.PlayerRepository;
import hu.kits.tennis.infrastructure.ApplicationContext;

public class BillingoClient {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final PlayerRepository playerRepository;
    private final InvoiceService invoiceService;

    public BillingoClient(ApplicationContext applicationContext) {
        this.playerRepository = applicationContext.getPlayerRepository();
        this.invoiceService = applicationContext.getInvoiceService();
    }
    
    public void saveNewPartners() {
        
        List<Player> playersWithAddress = playerRepository.loadAllPlayers().entries().stream()
            .filter(p -> !p.contact().address().isEmpty())
            .toList();
        
        List<Partner> partnersInBillingo = invoiceService.getPartners();
        logger.info("Found {} partners in Billingo", partnersInBillingo.size());
        
        List<Player> playersWithAddressButNotInBillingo = playersWithAddress.stream()
                .filter(p -> !isPartnerWithEmailExists(partnersInBillingo, p.contact().email()))
                .toList();
        
        logger.info("Players to be created in Billingo: {}", playersWithAddressButNotInBillingo.stream().map(Player::toString).collect(joining("\n")));
        
        for(Player player : playersWithAddressButNotInBillingo) {
            logger.info("Creating player in billingo: {}", player);
            //invoiceService.createPartnerForPlayer(player);
        }
    }
    
    private static boolean isPartnerWithEmailExists(List<Partner> partners, String email) {
        return partners.stream().anyMatch(p -> p.email().equals(email));
    }
    
    public void createInvoice() {
        invoiceService.createAndSendInvoices(List.of(
                "kocso.sandor.gabor@gmail.com",
                "playboy8301@gmail.com"), 100);
    }
}
