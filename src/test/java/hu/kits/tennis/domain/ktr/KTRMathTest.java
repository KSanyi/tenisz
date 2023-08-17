package hu.kits.tennis.domain.ktr;

import hu.kits.tennis.domain.ktr.KTRMath;

public class KTRMathTest {

    public static void main(String[] args) {
        
        System.out.println(KTRMath.calculateScore(6, 0));
        System.out.println(KTRMath.calculateScore(6, 1));
        System.out.println(KTRMath.calculateScore(6, 2));
        System.out.println(KTRMath.calculateScore(6, 3));
        System.out.println(KTRMath.calculateScore(6, 4));
        System.out.println(KTRMath.calculateScore(6, 5));
        System.out.println(KTRMath.calculateScore(7, 5));
        System.out.println(KTRMath.calculateScore(7, 6));
    }
    
}
