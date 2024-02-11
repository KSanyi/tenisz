package hu.kits.tennis.domain.ktr;

import static hu.kits.tennis.testutil.TestUtil.date;
import static hu.kits.tennis.testutil.TestUtil.player1;
import static hu.kits.tennis.testutil.TestUtil.player2;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.domain.player.Player;

public class KTRCalculatorTest {

    private static final double EPSILON = 0.006;
    
    private final LocalDate date = date("2022-01-01");
    
    @Test
    void calculatePlayersKTRNoMatches() {
        test(List.of(), 9);
    }
    
    @Test
    void calculatePlayersKTR1Match() {
        
        List<BookedMatch> matches = List.of(
                bookedMatch(date("2021-07-01"), player1, player2, KTR.of(8.4)));
        
        test(matches, 8.95);
    }
    
    @Test
    void calculatePlayersKTR7Match() {
        
        List<BookedMatch> matches = List.of(
                bookedMatch(date("2021-07-01"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-02"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-03"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-04"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-05"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-06"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-07"), player1, player2, KTR.of(8.4)));
        
        test(matches, 8.65);
    }
    
    @Test
    void calculatePlayersKTR14Match() {
        
        List<BookedMatch> matches = List.of(
                bookedMatch(date("2021-07-01"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-02"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-03"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-04"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-05"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-06"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-07"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-08"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-09"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-10"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-11"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-12"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-13"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-14"), player1, player2, KTR.of(8.4)));
        
        test(matches, 8.4);
    }
    
    @Test
    void calculatePlayersKTR15Match() {
        
        List<BookedMatch> matches = List.of(
                bookedMatch(date("2021-07-01"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-02"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-03"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-04"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-05"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-06"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-06-01"), player1, player2, KTR.of(5.)),   // should have no effect
                bookedMatch(date("2021-07-07"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-08"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-09"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-10"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-11"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-12"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-13"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2021-07-14"), player1, player2, KTR.of(8.4)));
        
        test(matches, 8.21);
    }
    
    @Test
    void calculatePlayersKTR1MatchAndOneFutureMatch() {
        
        List<BookedMatch> matches = List.of(
                bookedMatch(date("2021-07-01"), player1, player2, KTR.of(8.4)),
                bookedMatch(date("2022-01-02"), player1, player2, KTR.of(10.))); // should have no effect
        
        test(matches, 8.95);
    }
    
    private void test(List<BookedMatch> matches, double expectedKTR) {
        
        KTR ktr = KTRCalculator.calculatePlayersKTRDetails(player1, matches, date, 0, List.of()).ktr();
        
        assertEquals(expectedKTR, ktr.value(), EPSILON);
    }
    
    private static BookedMatch bookedMatch(LocalDate date, Player player1, Player player2, KTR matchKTRForPlayer1) {
        
        return new BookedMatch(
                new Match(null, null, null, null, date, player1, player2, MatchResult.of(0, 0)), 
                KTR.of(8.), KTR.of(8.), matchKTRForPlayer1, KTR.of(7.5));
    }
    
}
