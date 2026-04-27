package hu.kits.tennis.domain.tournament;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchResult.SetResult;
import hu.kits.tennis.domain.player.Player;

public record RoundRobinStanding(
        int rank,
        Player player,
        int matchesPlayed,
        int wins,
        int losses,
        int setsWon,
        int setsLost,
        int gamesWon,
        int gamesLost) {

    public static List<RoundRobinStanding> calculate(List<Player> players, List<Match> matches) {
        List<RoundRobinStanding> standings = new ArrayList<>();
        
        for (Player player : players) {
            int played = 0, wins = 0, losses = 0, setsWon = 0, setsLost = 0, gamesWon = 0, gamesLost = 0;
            
            for (Match match : matches) {
                if (!match.hasPlayer(player) || !match.isPlayed()) continue;
                played++;
                boolean isPlayer1 = player.equals(match.player1());
                if (isPlayer1 ? match.result().isPlayer1Winner() : match.result().isPlayer2Winner()) wins++;
                else losses++;
                
                for (SetResult set : match.result().setResults()) {
                    if (isPlayer1 ? set.isPlayer1Winner() : set.isPlayer2Winner()) setsWon++;
                    else setsLost++;
                }
                gamesWon  += isPlayer1 ? match.result().sumPlayer1Games() : match.result().sumPlayer2Games();
                gamesLost += isPlayer1 ? match.result().sumPlayer2Games() : match.result().sumPlayer1Games();
            }
            standings.add(new RoundRobinStanding(0, player, played, wins, losses, setsWon, setsLost, gamesWon, gamesLost));
        }
        // Sort: wins desc, then set difference, then game difference
        standings.sort(Comparator.comparingInt(RoundRobinStanding::wins).reversed()
                .thenComparingInt(s -> s.setsLost() - s.setsWon())
                .thenComparingInt(s -> s.gamesLost() - s.gamesWon()));
        
        List<RoundRobinStanding> ranked = new ArrayList<>();
        for (int i = 0; i < standings.size(); i++) {
            RoundRobinStanding s = standings.get(i);
            ranked.add(new RoundRobinStanding(i + 1, s.player(), s.matchesPlayed(), s.wins(), s.losses(), s.setsWon(), s.setsLost(), s.gamesWon(), s.gamesLost()));
        }
        return ranked;
    }
}
