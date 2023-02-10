package hu.kits.tennis.domain.utr;

public class UTRMathTest {

    public static void main(String[] args) {
        
        System.out.println(UTRMath.calculateScore(6, 0));
        System.out.println(UTRMath.calculateScore(6, 1));
        System.out.println(UTRMath.calculateScore(6, 2));
        System.out.println(UTRMath.calculateScore(6, 3));
        System.out.println(UTRMath.calculateScore(6, 4));
        System.out.println(UTRMath.calculateScore(6, 5));
        System.out.println(UTRMath.calculateScore(7, 5));
        System.out.println(UTRMath.calculateScore(7, 6));
    }
    
}
