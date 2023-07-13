package hu.kits.tennis.application;

import java.util.List;

import hu.kits.tennis.domain.invoice.InvoiceService;
import hu.kits.tennis.domain.player.Player;

public class SpyInvoiceService implements InvoiceService {

    public Player lastSavedPlayer;
    public int numberOfInvoicesCreated;
    
    @Override
    public void createPartnerForPlayer(Player player) {
        lastSavedPlayer = player;
    }

    @Override
    public List<Partner> getPartners() {
        return List.of();
    }

    @Override
    public List<String> createAndSendInvoices(List<String> emailAddresses, int amount) {
        numberOfInvoicesCreated += emailAddresses.size();
        return emailAddresses;
    }

}
