package hu.kits.tennis.domain.utr;

public class UTRMath {

    public static double calculateScore(int player1Games, int plyer2Games) {
        
        return 1 - (double)Math.min(player1Games, plyer2Games) / Math.max(player1Games, plyer2Games);
    }
    
}
