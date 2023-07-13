package hu.kits.tennis.testutil;

import java.util.List;

import hu.kits.tennis.domain.invoice.InvoiceService;
import hu.kits.tennis.domain.player.Player;

public class FakeInvoiceService implements InvoiceService {

    @Override
    public void createPartnerForPlayer(Player player) {
    }

    @Override
    public List<Partner> getPartners() {
        return List.of();
    }

    @Override
    public List<String> createAndSendInvoices(List<String> emailAddresses, int amount) {
        return emailAddresses;
    }

}
