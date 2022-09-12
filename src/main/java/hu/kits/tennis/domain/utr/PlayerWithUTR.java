package hu.kits.tennis.domain.utr;

public record PlayerWithUTR(Player player, int rank, UTR utr, UTR utrOneWeekAgo) {
    
    public UTR utrChange() {
        if(utr.isDefinded() && utrOneWeekAgo.isDefinded()) {
            return UTR.of(utr.value() - utrOneWeekAgo.value());    
        } else {
            return UTR.of(0.);
        }
    }

}
