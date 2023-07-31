package hu.kits.tennis.application;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.domain.invoice.InvoiceService;
import hu.kits.tennis.domain.invoice.InvoiceService.Partner;
import hu.kits.tennis.domain.player.PlayersService;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.Player.Address;

public class AddPlayerAddressWorkflow {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final PlayersService playerService;
    private final InvoiceService invoiceService;
    
    public AddPlayerAddressWorkflow(PlayersService playerService, InvoiceService invoiceService) {
        this.playerService = playerService;
        this.invoiceService = invoiceService;
    }
    
    public Optional<Player> addPlayerAddress(String name, String email, Address address) {
        
        logger.info("Adding address to player {} ({}): {}", name, email, address);
        
        Optional<Player> player = playerService.saveAddress(name, email, address);
        logger.info("Player address saved");
        if(player.isPresent()) {
            List<Partner> partners = invoiceService.getPartners();
            if(partners.stream().noneMatch(partner -> partner.email().equals(email))) {
                logger.info("Creating player in invoice system");
                Player playerWithAddress = playerService.findPlayer(player.get().id());
                invoiceService.createPartnerForPlayer(playerWithAddress);
                logger.info("Player created in invoice system");
            } else {
                logger.info("Player is already in the invoice system");
            }
        }
        return player;
    }
    
}
