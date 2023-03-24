package hu.kits.tennis.domain.match;

import java.time.LocalDate;
import java.util.stream.Stream;

import hu.kits.tennis.domain.player.Player;

public record Match(Integer id, String tournamentId, Integer tournamentBoardNumber, Integer tournamentMatchNumber, LocalDate date, Player player1, Player player2, MatchResult result) {

    public static Match createNew(String tournamentId, Integer tournamentBoardNumber, Integer tournamentMatchNumber, LocalDate date, Player player1, Player player2) {
        return new Match(null, tournamentId, tournamentBoardNumber, tournamentMatchNumber, date, player1, player2, null);
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
        return result != null && result.isPlayer1Winner() ? player1 : player2;
    }
    
    public Player loser() {
        return result != null && result.isPlayer1Winner() ? player2 : player1;
    }
    
    public Stream<Player> players(){
        return Stream.of(player1, player2);
    }
    
    public MatchType matchType() {
        return result.matchType();
    }
    
    @Override
    public String toString() {
        String player1Name = player1 != null ? player1.name() : "?";
        String player2Name = player2 != null ? player2.name() : "?";
        return (date != null ? (date.toString() + " ") : "") +  player1Name + " VS " + player2Name + " " + (result != null ? result.toString() : "");
    }

    public boolean isPlayed() {
        return result != null;
    }
    
    public static enum MatchType {
        
        ONE_SET(3),
        ONE_FOUR_GAMES_SET(2),
        BEST_OF_THREE(6),
        SUPER_TIE_BREAK(0),
        OTHER(6);
        
        public final int multiplier;

        private MatchType(int multiplier) {
            this.multiplier = multiplier;
        }
    }

}
