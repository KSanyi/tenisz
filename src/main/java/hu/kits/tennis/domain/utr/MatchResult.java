package hu.kits.tennis.domain.utr;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public record MatchResult(List<SetResult> setResults) {

    public MatchResult(int player1Games, int player2Games) {
        this(List.of(new SetResult(player1Games, player2Games)));
    }
    
    private double scoreOfPlayer1() {
        int sumPlayer1Games = sumPlayer1Games();
        int sumPlayer2Games = sumPlayer2Games();
        double closeness = (double)Math.min(sumPlayer1Games, sumPlayer2Games) / Math.max(sumPlayer1Games, sumPlayer2Games);
        double absoluteScore = 1 - closeness;
        return sumPlayer1Games > sumPlayer2Games ? absoluteScore : -absoluteScore;
    }
    
    private double scoreOfPlayer2() {
        return - scoreOfPlayer1();
    }
    
    private int sumPlayer1Games() {
        return setResults.stream().mapToInt(setResult -> setResult.player1Games).sum();
    }
    
    private int sumPlayer2Games() {
        return setResults.stream().mapToInt(setResult -> setResult.player2Games).sum();
    }
    
    public UTR calculateUTRForPlayer1(UTR player2UTR) {
        double scoreOfPlayer1 = scoreOfPlayer1();
        return player2UTR.calculateMatchUTR(scoreOfPlayer1);
    }
    
    public UTR calculateUTRForPlayer2(UTR player1UTR) {
        double scoreOfPlayer2 = scoreOfPlayer2();
        return player1UTR.calculateMatchUTR(scoreOfPlayer2);
    }
    
    public MatchResult swap() {
        List<SetResult> reversedSetResults = setResults.stream().map(setResult -> new SetResult(setResult.player2Games, setResult.player1Games)).collect(toList());
        return new MatchResult(reversedSetResults);
    }
    
    public boolean isPlayer1Winner() {
        int numberOfSets = setResults.size();
        int setsWon = (int)setResults.stream().filter(SetResult::isPlayer1Winner).count();
        return setsWon > numberOfSets / 2;
    }
    
    public boolean isPlayer2Winner() {
        return !isPlayer1Winner();
    }
    
    @Override
    public String toString() {
        return setResults.stream().map(SetResult::toString).collect(joining(" "));
    }
    
    public static record SetResult(int player1Games, int player2Games) {
        
        public int sumGames() {
            return player1Games + player2Games;
        }
        
        @Override
        public String toString() {
            return player1Games + ":" + player2Games;
        }
        
        public static SetResult parse(String stringValue) {
            String[] parts = stringValue.split(":");
            return new SetResult(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }
        
        public boolean isPlayer1Winner() {
            return player1Games > player2Games;
        }
        
    }

    public static MatchResult of(int ... gamesInSets) {
        if(gamesInSets.length == 0 || gamesInSets.length % 2 == 1) throw new IllegalArgumentException("Illegal number of sets");
        List<SetResult> setResults = new ArrayList<>();
        for(int i=0;i<gamesInSets.length;i+=2) {
            SetResult setResult = new SetResult(gamesInSets[i], gamesInSets[i+1]);
            setResults.add(setResult);
        }
        
        return new MatchResult(setResults);
    }

    public String serialize() {
        return setResults.stream().map(SetResult::toString).collect(joining(" "));
    }

    public static MatchResult parse(String stringValue) {
        if(stringValue == null || stringValue.isBlank()) return null;
        try {
            return new MatchResult(Stream.of(stringValue.split(" "))
                    .map(String::trim)
                    .map(SetResult::parse)
                    .collect(toList()));    
        } catch(Exception ex) {
            throw new IllegalArgumentException("Can not parse match result: '" + stringValue + "'", ex);
        }
    }
    
}
