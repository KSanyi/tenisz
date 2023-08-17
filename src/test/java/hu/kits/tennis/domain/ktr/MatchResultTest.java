package hu.kits.tennis.domain.ktr;

import org.junit.jupiter.api.Test;

import hu.kits.tennis.domain.ktr.KTR;
import hu.kits.tennis.domain.match.MatchResult;

public class MatchResultTest {
    
    @Test
    void test() {
        
        KTR player1KTR = new KTR(7.0);
        KTR player2KTR = new KTR(8.0);
        
        MatchResult result;
        KTR matchKTRForPlayer1;
        KTR matchKTRForPlayer2;
        
        result = MatchResult.of(6,4, 7,5);
        matchKTRForPlayer1 = result.calculateKTRForPlayer1(player2KTR);
        matchKTRForPlayer2 = result.calculateKTRForPlayer2(player1KTR);
        System.out.println(result + " -> " + matchKTRForPlayer1);
        System.out.println(result + " -> " + matchKTRForPlayer2);
        
        result = MatchResult.of(6,1, 6,2);
        matchKTRForPlayer1 = result.calculateKTRForPlayer1(player2KTR);
        matchKTRForPlayer2 = result.calculateKTRForPlayer2(player1KTR);
        System.out.println(result + " -> " + matchKTRForPlayer1);
        System.out.println(result + " -> " + matchKTRForPlayer2);
        
        result = MatchResult.of(6,3, 6,3);
        matchKTRForPlayer1 = result.calculateKTRForPlayer1(player2KTR);
        matchKTRForPlayer2 = result.calculateKTRForPlayer2(player1KTR);
        System.out.println(result + " -> " + matchKTRForPlayer1);
        System.out.println(result + " -> " + matchKTRForPlayer2);
        
        result = MatchResult.of(6,3, 2,6, 6,4);
        matchKTRForPlayer1 = result.calculateKTRForPlayer1(player2KTR);
        matchKTRForPlayer2 = result.calculateKTRForPlayer2(player1KTR);
        System.out.println(result + " -> " + matchKTRForPlayer1);
        System.out.println(result + " -> " + matchKTRForPlayer2);
        
    }
    
}
