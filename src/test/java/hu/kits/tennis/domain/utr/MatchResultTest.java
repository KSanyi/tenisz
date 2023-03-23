package hu.kits.tennis.domain.utr;

import org.junit.jupiter.api.Test;

import hu.kits.tennis.domain.match.MatchResult;

public class MatchResultTest {
    
    @Test
    void test() {
        
        UTR player1UTR = new UTR(7.0);
        UTR player2UTR = new UTR(8.0);
        
        MatchResult result;
        UTR matchUTRForPlayer1;
        UTR matchUTRForPlayer2;
        
        result = MatchResult.of(6,4, 7,5);
        matchUTRForPlayer1 = result.calculateUTRForPlayer1(player2UTR);
        matchUTRForPlayer2 = result.calculateUTRForPlayer2(player1UTR);
        System.out.println(result + " -> " + matchUTRForPlayer1);
        System.out.println(result + " -> " + matchUTRForPlayer2);
        
        result = MatchResult.of(6,1, 6,2);
        matchUTRForPlayer1 = result.calculateUTRForPlayer1(player2UTR);
        matchUTRForPlayer2 = result.calculateUTRForPlayer2(player1UTR);
        System.out.println(result + " -> " + matchUTRForPlayer1);
        System.out.println(result + " -> " + matchUTRForPlayer2);
        
        result = MatchResult.of(6,3, 6,3);
        matchUTRForPlayer1 = result.calculateUTRForPlayer1(player2UTR);
        matchUTRForPlayer2 = result.calculateUTRForPlayer2(player1UTR);
        System.out.println(result + " -> " + matchUTRForPlayer1);
        System.out.println(result + " -> " + matchUTRForPlayer2);
        
        result = MatchResult.of(6,3, 2,6, 6,4);
        matchUTRForPlayer1 = result.calculateUTRForPlayer1(player2UTR);
        matchUTRForPlayer2 = result.calculateUTRForPlayer2(player1UTR);
        System.out.println(result + " -> " + matchUTRForPlayer1);
        System.out.println(result + " -> " + matchUTRForPlayer2);
        
    }
    
}
