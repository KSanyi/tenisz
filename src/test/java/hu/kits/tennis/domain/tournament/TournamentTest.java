package hu.kits.tennis.domain.tournament;

import static hu.kits.tennis.TestUtil.*;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import hu.kits.tennis.domain.tournament.Tournament.Board;
import hu.kits.tennis.domain.tournament.Tournament.Status;
import hu.kits.tennis.domain.tournament.Tournament.Type;
import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.Player;

public class TournamentTest {

    private final String TOURNAMENT_ID = "58c4446c";
    
    private final LocalDate DATE = LocalDate.of(2022,1,11);
    
    @Test
    void lineupTestWith4Players() {
        
        List<Contestant> contestants = List.of(
                new Contestant(player1, 1),
                new Contestant(player2, 2),
                new Contestant(player3, 3),
                new Contestant(player4, 4));
        
        Tournament tournament = new Tournament(TOURNAMENT_ID, DATE, "BVSC Tour", "BVSC",  Type.SIMPLE_BOARD, 1, contestants, Status.DRAFT, List.of(new Tournament.Board(2, Map.of())));
        
        List<Player> lineup = tournament.playersLineup();
        
        assertEquals(List.of(player1, player2, player3, player4), lineup);
    }
    
    @Test
    void lineupTestWith6Players() {
        
        List<Contestant> contestants = List.of(
                new Contestant(player1, 1),
                new Contestant(player2, 3),
                new Contestant(player3, 4),
                new Contestant(player4, 5),
                new Contestant(player5, 6),
                new Contestant(player6, 7));
        
        Tournament tournament = new Tournament(TOURNAMENT_ID, DATE, "BVSC Tour", "BVSC",  Type.SIMPLE_BOARD, 1, contestants, Status.DRAFT, List.of(new Tournament.Board(3, Map.of())));
        
        List<Player> lineup = tournament.playersLineup();
        
        assertEquals(List.of(player1, Player.BYE, player2, player3, player4, player5, player6, Player.BYE), lineup);
    }
    
    @Test
    void boardTestWith6Players() {
        
        Match quarterFinal1 = Match.createNew(TOURNAMENT_ID, 1, 1, DATE, player1, Player.BYE);
        Match quarterFinal2 = Match.createNew(TOURNAMENT_ID, 1, 2, DATE, player2, player3);
        Match quarterFinal3 = Match.createNew(TOURNAMENT_ID, 1, 3, DATE, player4, player5);
        Match quarterFinal4 = Match.createNew(TOURNAMENT_ID, 1, 4, DATE, player6, Player.BYE);
        
        Match semiFinal1 = Match.createNew(TOURNAMENT_ID, 1, 5, DATE, player1, player2);
        Match semiFinal2 = Match.createNew(TOURNAMENT_ID, 1, 6, DATE, player5, player6);
        
        Match theFinal = Match.createNew(TOURNAMENT_ID, 1, 7, DATE, player2, player5);
        
        // TODO
        Map<Integer, Match> matches = Stream.of(quarterFinal1, quarterFinal2, quarterFinal3, quarterFinal4, semiFinal1, semiFinal2, theFinal)
                .collect(toMap(Match::tournamentMatchNumber, identity()));
        
        Board board = new Board(3, matches);
        
        assertEquals(quarterFinal1, board.getMatch(1, 1));
        assertEquals(quarterFinal2, board.getMatch(1, 2));
        assertEquals(quarterFinal3, board.getMatch(1, 3));
        assertEquals(quarterFinal4, board.getMatch(1, 4));
        
        assertEquals(semiFinal1, board.getMatch(2, 1));
        assertEquals(semiFinal2, board.getMatch(2, 2));
        
        assertEquals(theFinal, board.getMatch(3, 1));
        assertEquals(theFinal, board.finalMatch());
        assertEquals(true, board.isFinal(theFinal));
        assertEquals(false, board.isFinal(semiFinal1));
        
        assertEquals(1, board.roundNumber(quarterFinal3));
        assertEquals(2, board.roundNumber(semiFinal1));
        assertEquals(3, board.roundNumber(theFinal));
        
        assertEquals(Optional.empty(), board.findPrevMatch(quarterFinal1, player1));
        assertEquals(Optional.of(quarterFinal1), board.findPrevMatch(semiFinal1, player1));
        assertEquals(Optional.of(quarterFinal2), board.findPrevMatch(semiFinal1, player2));
        // TODO
        // assertEquals(Optional.of(semiFinal1), board.findPrevMatch(theFinal, player2));
        
        assertEquals(5, board.nextRoundMatchNumber(1));
        assertEquals(5, board.nextRoundMatchNumber(2));
        assertEquals(6, board.nextRoundMatchNumber(3));
        assertEquals(6, board.nextRoundMatchNumber(4));
        
        assertEquals(7, board.nextRoundMatchNumber(5));
        assertEquals(7, board.nextRoundMatchNumber(6));
    }
    
}
