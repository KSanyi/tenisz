package hu.kits.tennis.domain.ktr;

import hu.kits.tennis.domain.player.Player;

public record PlayerWithKTR(Player player, int rank, KTR ktr, KTR ktrOneWeekAgo, int numberOfMatches, int numberOfWins, int numberOfTrophies) {
    
    public KTR ktrChange() {
        if(ktr.isDefinded() && ktrOneWeekAgo.isDefinded()) {
            return KTR.of(ktr.value() - ktrOneWeekAgo.value());    
        } else {
            return KTR.of(0.);
        }
    }

}
