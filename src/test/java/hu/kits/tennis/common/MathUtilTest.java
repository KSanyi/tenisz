package hu.kits.tennis.common;

import static hu.kits.tennis.common.MathUtil.log2;
import static hu.kits.tennis.common.MathUtil.pow2;
import static hu.kits.tennis.common.MathUtil.roundAndMatchNumberInRound;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MathUtilTest {

    @Test
    void pow2Test() {
        assertEquals(  2, pow2(1));
        assertEquals(  4, pow2(2));
        assertEquals(  8, pow2(3));
        assertEquals( 16, pow2(4));
        assertEquals( 32, pow2(5));
        assertEquals( 64, pow2(6));
        assertEquals(128, pow2(7));
    }
    
    @Test
    void log2Test() {
        assertEquals(1, log2(2));
        assertEquals(2, log2(3));
        assertEquals(2, log2(4));
        assertEquals(3, log2(5));
        assertEquals(3, log2(6));
        assertEquals(3, log2(7));
        assertEquals(3, log2(8));
        assertEquals(4, log2(9));
        assertEquals(4, log2(16));
        assertEquals(5, log2(17));
        assertEquals(5, log2(32));
    }
    
    @Test
    void roundAndMatchNumberInRoundTestIn8PlayerTournament() {
        
        int NR_ROUNDS = 3;
        
        // quarterfinals
        assertEquals(Pair.of(1, 1), roundAndMatchNumberInRound(1, NR_ROUNDS));
        assertEquals(Pair.of(1, 2), roundAndMatchNumberInRound(2, NR_ROUNDS));
        assertEquals(Pair.of(1, 3), roundAndMatchNumberInRound(3, NR_ROUNDS));
        assertEquals(Pair.of(1, 4), roundAndMatchNumberInRound(4, NR_ROUNDS));
        // semifinals
        assertEquals(Pair.of(2, 1), roundAndMatchNumberInRound(5, NR_ROUNDS));
        assertEquals(Pair.of(2, 2), roundAndMatchNumberInRound(6, NR_ROUNDS));
        // final
        assertEquals(Pair.of(3, 1), roundAndMatchNumberInRound(7, NR_ROUNDS));
    }
    
}
