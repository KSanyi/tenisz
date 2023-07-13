package hu.kits.tennis.domain.invoice;

import java.util.List;

import hu.kits.tennis.domain.player.Player;

public interface InvoiceService {

    void createPartnerForPlayer(Player player);
    
    List<Partner> getPartners();
    
    List<String> createAndSendInvoices(List<String> emailAddresses, int amount);
    
    static record Partner(long partnerId, String email, String name) {}
}
