package hu.kits.tennis.domain.utr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

public class UTRCalculatorTest {

    private static final double EPSILON = 0.006;
    
    private final Player player1 = new Player(1, "Player1", 8);
    private final Player player2 = new Player(2, "Player2", 8);
    private final LocalDate date = date("2022-01-01");
    
    
    @Test
    void calculatePlayersUTRNoMatches() {
        test(List.of(), 8);
    }
    
    @Test
    void calculatePlayersUTR1Match() {
        
        List<BookedMatch> matches = List.of(
                bookedMatch(date("2021-07-01"), player1, player2, UTR.of(8.4)));
        
        test(matches, 8.2);
    }
    
    @Test
    void calculatePlayersUTR7Match() {
        
        List<BookedMatch> matches = List.of(
                bookedMatch(date("2021-07-01"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-02"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-03"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-04"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-05"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-06"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-07"), player1, player2, UTR.of(8.4)));
        
        test(matches, 8.36);
    }
    
    @Test
    void calculatePlayersUTR14Match() {
        
        List<BookedMatch> matches = List.of(
                bookedMatch(date("2021-07-01"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-02"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-03"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-04"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-05"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-06"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-07"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-08"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-09"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-10"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-11"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-12"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-13"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-14"), player1, player2, UTR.of(8.4)));
        
        test(matches, 8.4);
    }
    
    @Test
    void calculatePlayersUTR15Match() {
        
        List<BookedMatch> matches = List.of(
                bookedMatch(date("2021-07-01"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-02"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-03"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-04"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-05"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-06"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-06-01"), player1, player2, UTR.of(5)),   // should have no effect
                bookedMatch(date("2021-07-07"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-08"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-09"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-10"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-11"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-12"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-13"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2021-07-14"), player1, player2, UTR.of(8.4)));
        
        test(matches, 8.4);
    }
    
    @Test
    void calculatePlayersUTR1MatchAndOneFutureMatch() {
        
        List<BookedMatch> matches = List.of(
                bookedMatch(date("2021-07-01"), player1, player2, UTR.of(8.4)),
                bookedMatch(date("2022-01-02"), player1, player2, UTR.of(10))); // should have no effect
        
        test(matches, 8.2);
    }
    
    private void test(List<BookedMatch> matches, double expectedUTR) {
        
        UTR utr = UTRCalculator.calculatePlayersUTR(player1, matches, date);
        
        assertEquals(expectedUTR, utr.value(), EPSILON);
    }
    
    private static BookedMatch bookedMatch(LocalDate date, Player player1, Player player2, UTR matchUTRForPlayer1) {
        
        return new BookedMatch(
                new Match(null, null, null, null, date, player1, player2, MatchResult.of(0, 0)), 
                UTR.of(8), UTR.of(8), matchUTRForPlayer1, UTR.of(7.5));
    }
    
    private static LocalDate date(String dateString) {
        return LocalDate.parse(dateString);
    }
    
}
