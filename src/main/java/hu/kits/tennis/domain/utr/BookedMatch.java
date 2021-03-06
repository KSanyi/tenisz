package hu.kits.tennis.domain.utr;

public record BookedMatch(Match playedMatch, UTR player1UTR, UTR player2UTR, UTR matchUTRForPlayer1, UTR matchUTRForPlayer2) {

    public UTR utrOfMatchFor(Player player) {
        if(player.equals(playedMatch.player1())) {
            return matchUTRForPlayer1;
        } else {
            return matchUTRForPlayer2;
        }
    }

    public boolean hasPlayed(Player player) {
        return playedMatch.hasPlayer(player);
    }

    public BookedMatch swap() {
        return new BookedMatch(playedMatch.swap(), player2UTR, player1UTR, matchUTRForPlayer2, matchUTRForPlayer1);
    }
    
    public BookedMatch clearUTRs() {
        return new BookedMatch(playedMatch, UTR.UNDEFINED, UTR.UNDEFINED, UTR.UNDEFINED, UTR.UNDEFINED);
    }
    
}
