package hu.kits.tennis.domain.utr;

import java.time.LocalDate;

public record Match(int id, String tournamentId, Integer tournamentBoardNumber, Integer tournamentMatchNumber, LocalDate date, Player player1, Player player2, MatchResult result) {

    public static Match createNew(String tournamentId, Integer tournamentBoardNumber, Integer tournamentMatchNumber, LocalDate date, Player player1, Player player2) {
        return new Match(0, tournamentId, tournamentBoardNumber, tournamentMatchNumber, date, player1, player2, null);
    }
    
    public boolean hasPlayed(Player player) {
        return player1.equals(player) || player2.equals(player);
    }

    public Match swap() {
        return new Match(id, tournamentId, tournamentBoardNumber, tournamentMatchNumber, date, player2, player1, result != null ? result.swap() : null);
    }

    public boolean arePlayersSet() {
        return player1 != null && player2 != null;
    }

    public Player winner() {
        return result.isPlayer1Winner() ? player1 : player2;
    }

}
