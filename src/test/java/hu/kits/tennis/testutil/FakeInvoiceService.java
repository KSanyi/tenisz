package hu.kits.tennis.testutil;

import java.util.List;

import hu.kits.tennis.domain.invoice.InvoiceService;
import hu.kits.tennis.domain.player.Player;

public class FakeInvoiceService implements InvoiceService {

    @Override
    public void createPartnerForPlayer(Player player) {
    }

    @Override
    public List<String> getPartnerEmails() {
        return List.of();
    }

}
