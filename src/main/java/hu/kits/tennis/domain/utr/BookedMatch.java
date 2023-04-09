package hu.kits.tennis.domain.utr;

import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.player.Player;

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

    public BookedMatch swapIfPlayer2Won() {
        if(playedMatch.player2().equals(playedMatch.winner())) {
            return swap();
        } else {
            return this;
        }
    }
    
    public BookedMatch swap() {
        return new BookedMatch(playedMatch.swap(), player2UTR, player1UTR, matchUTRForPlayer2, matchUTRForPlayer1);
    }
    
    public BookedMatch clearUTRs() {
        return new BookedMatch(playedMatch, UTR.UNDEFINED, UTR.UNDEFINED, UTR.UNDEFINED, UTR.UNDEFINED);
    }

    public boolean isUpset() {
        if(playedMatch.result() != null && player1UTR != null && player2UTR != null && player1UTR.isDefinded() && player2UTR.isDefinded()) {
            return player2UTR.value() - player1UTR.value() > 1 && playedMatch.winner().equals(playedMatch.player1()) ||
                   player1UTR.value() - player2UTR.value() > 1 && playedMatch.winner().equals(playedMatch.player2());
        } else {
            return false;
        }
    }
    
}
