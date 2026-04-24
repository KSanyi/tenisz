package hu.kits.tennis.domain.match;

import java.util.List;

import hu.kits.tennis.domain.player.Player;

public record Head2HeadData(Player player1, Player player2, List<MatchInfo> matches) {

    public int numberOfMatches() {
        return matches.size();
    }
    
    public int player1Wins() {
        return (int)matches.stream().filter(match -> match.isWinner(player1)).count();
    }
    
    public int player2Wins() {
        return (int)matches.stream().filter(match -> match.isWinner(player2)).count();
    }
    
    @Override
    public String toString() {
        return player1.name() + " " + player2.name() + ": " + player1Wins() + " - " + player2Wins();
    }
    
}
