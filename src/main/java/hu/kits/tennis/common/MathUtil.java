package hu.kits.tennis.common;

public class MathUtil {

    private static final int[] twoPows = new int[] {1, 2, 4, 8, 16, 32, 64, 128, 256};

    public static int pow2(int x) {
        return twoPows[x];
    }
    
    public static int log2(int n) {
        for(int i=1;i<twoPows.length;i++) {
            if(n <= twoPows[i]) {
                return i;
            }
        }
        throw new IllegalArgumentException();
    }
    
    public static Pair<Integer, Integer> roundAndMatchNumberInRound(int matchNumber, int numberOfRounds) {
        int counter = 0;
        int round = 1;
        for(round=1;round<twoPows.length;round++) {
            if(matchNumber > counter) {
                counter += twoPows[numberOfRounds - round];
            } else {
                break;
            }
        }
        round--;
        int matchNumberInRound = matchNumber - (pow2(numberOfRounds) - pow2(numberOfRounds - round + 1));
        
        return Pair.of(round, matchNumberInRound);
    }
    
    public static double roundToTwoDigits(double value) {
        return Math.round(value * 100) / 100.;
    }
}
