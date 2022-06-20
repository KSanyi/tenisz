package hu.kits.tennis.domain.utr;

import java.time.LocalDate;

public record Match(int id, String tournamentId, Integer tournamentBoardNumber, Integer tournamentMatchNumber, LocalDate date, Player player1, Player player2, MatchResult result) {

    public static Match createNew(String tournamentId, Integer tournamentBoardNumber, Integer tournamentMatchNumber, LocalDate date, Player player1, Player player2) {
        return new Match(0, tournamentId, tournamentBoardNumber, tournamentMatchNumber, date, player1, player2, null);
    }
    
    public boolean hasPlayer(Player player) {
        return player.equals(player1) || player.equals(player2);
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
    
    @Override
    public String toString() {
        String player1Name = player1 != null ? player1.name() : "?";
        String player2Name = player2 != null ? player2.name() : "?";
        return (date != null ? (date.toString() + " ") : "") +  player1Name + " VS " + player2Name + " " + (result != null ? result.toString() : "");
    }

}
