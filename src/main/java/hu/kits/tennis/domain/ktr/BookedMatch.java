package hu.kits.tennis.domain.ktr;

import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.player.Player;

public record BookedMatch(Match playedMatch, KTR player1KTR, KTR player2KTR, KTR matchKTRForPlayer1, KTR matchKTRForPlayer2) {

    public KTR ktrOfMatchFor(Player player) {
        if(player.equals(playedMatch.player1())) {
            return matchKTRForPlayer1;
        } else {
            return matchKTRForPlayer2;
        }
    }

    public boolean hasPlayed(Player player) {
        return playedMatch.hasPlayer(player);
    }

    public BookedMatch swapIfPlayer2Won() {
        if(playedMatch.player2().equals(playedMatch.winner())) {
            return swap();
        } else {
            return this;
        }
    }
    
    public BookedMatch swap() {
        return new BookedMatch(playedMatch.swap(), player2KTR, player1KTR, matchKTRForPlayer2, matchKTRForPlayer1);
    }
    
    public BookedMatch clearKTRs() {
        return new BookedMatch(playedMatch, KTR.UNDEFINED, KTR.UNDEFINED, KTR.UNDEFINED, KTR.UNDEFINED);
    }

    public boolean isUpset() {
        if(playedMatch.result() != null && playedMatch.result().isMatchLongEnough() && player1KTR != null && player2KTR != null && player1KTR.isDefinded() && player2KTR.isDefinded()) {
            return player2KTR.value() - player1KTR.value() > 1 && playedMatch.winner().equals(playedMatch.player1()) ||
                   player1KTR.value() - player2KTR.value() > 1 && playedMatch.winner().equals(playedMatch.player2());
        } else {
            return false;
        }
    }
    
}
