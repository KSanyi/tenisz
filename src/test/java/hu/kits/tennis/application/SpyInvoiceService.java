package hu.kits.tennis.application;

import java.util.List;

import hu.kits.tennis.domain.invoice.InvoiceService;
import hu.kits.tennis.domain.player.Player;

public class SpyInvoiceService implements InvoiceService {

    public Player lastSavedPlayer;
    
    @Override
    public void createPartnerForPlayer(Player player) {
        lastSavedPlayer = player;
    }

    @Override
    public List<String> getPartnerEmails() {
        return List.of();
    }

}
