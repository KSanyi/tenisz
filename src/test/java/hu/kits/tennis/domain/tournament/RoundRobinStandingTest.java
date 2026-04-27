package hu.kits.tennis.domain.tournament;

import static hu.kits.tennis.testutil.TestUtil.player1;
import static hu.kits.tennis.testutil.TestUtil.player2;
import static hu.kits.tennis.testutil.TestUtil.player3;
import static hu.kits.tennis.testutil.TestUtil.player4;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.domain.player.Player;

class RoundRobinStandingTest {

    private static final LocalDate DATE = LocalDate.of(20269,9,26);
    
    @Test
    void test() {
        List<Player> players = List.of(player1, player2, player3, player4);
        
        List<Match> matches = List.of(
                createMatch(player1, player2, MatchResult.of(6, 4, 6, 2)),
                createMatch(player3, player4, MatchResult.of(6, 0, 6, 0)),
                createMatch(player1, player3, MatchResult.of(7, 5, 0, 6, 1, 6)),
                createMatch(player2, player4, MatchResult.of(6, 3, 6, 3)));
        
        List<RoundRobinStanding> standing = RoundRobinStanding.calculate(players, matches);
        String standingFormatted = standing.stream().map(RoundRobinStanding::toString).collect(Collectors.joining("\n"));
        
        Assertions.assertEquals("""
                RoundRobinStanding[rank=1, player=P3(3), matchesPlayed=2, wins=2, losses=0, setsWon=4, setsLost=1, gamesWon=29, gamesLost=8]
                RoundRobinStanding[rank=2, player=P1(1), matchesPlayed=2, wins=1, losses=1, setsWon=3, setsLost=2, gamesWon=20, gamesLost=23]
                RoundRobinStanding[rank=3, player=P2(2), matchesPlayed=2, wins=1, losses=1, setsWon=2, setsLost=2, gamesWon=18, gamesLost=18]
                RoundRobinStanding[rank=4, player=P4(4), matchesPlayed=2, wins=0, losses=2, setsWon=0, setsLost=4, gamesWon=6, gamesLost=24]""", standingFormatted);
    }
    
    private static Match createMatch(Player playerA, Player playerB, MatchResult result) {
        return new Match(null, null, null, null, DATE, playerA, playerB, result);
    }

}
