package hu.kits.tennis.domain.utr;

import hu.kits.tennis.domain.player.Player;

public record PlayerWithUTR(Player player, int rank, UTR utr, UTR utrOneWeekAgo, int numberOfMatches) {
    
    public UTR utrChange() {
        if(utr.isDefinded() && utrOneWeekAgo.isDefinded()) {
            return UTR.of(utr.value() - utrOneWeekAgo.value());    
        } else {
            return UTR.of(0.);
        }
    }

}
