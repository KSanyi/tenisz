package hu.kits.tennis.domain.invoice;

import java.util.List;

import hu.kits.tennis.domain.player.Player;

public interface InvoiceService {

    void createPartnerForPlayer(Player player);
    
    List<String> getPartnerEmails();
    
}
