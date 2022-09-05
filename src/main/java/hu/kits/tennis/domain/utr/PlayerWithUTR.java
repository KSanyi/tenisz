package hu.kits.tennis.domain.utr;

public record PlayerWithUTR(Player player, int rank, UTR utr, UTR lastMondayUtr) {
    
    public UTR utrChange() {
        if(utr.isDefinded() && lastMondayUtr.isDefinded()) {
            return UTR.of(utr.value() - lastMondayUtr.value());    
        } else {
            return UTR.of(0.);
        }
    }

}
