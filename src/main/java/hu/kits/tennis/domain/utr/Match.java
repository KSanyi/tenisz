package hu.kits.tennis.domain.utr;

import java.time.LocalDate;

public record Match(int id, String tournamentId, Integer torunamentMatchNumber, LocalDate date, Player player1, Player player2, MatchResult result) {

    public static Match createNew(String tournamentId, Integer torunamentMatchNumber, LocalDate date, Player player1, Player player2) {
        return new Match(0, tournamentId, torunamentMatchNumber, date, player1, player2, null);
    }
    
    public boolean hasPlayed(Player player) {
        return player1.equals(player) || player2.equals(player);
    }

    public Match swap() {
        return new Match(id, tournamentId, torunamentMatchNumber, date, player2, player1, result.swap());
    }

}
