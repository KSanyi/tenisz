package hu.kits.tennis.domain.match;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import hu.kits.tennis.domain.match.Match.MatchType;
import hu.kits.tennis.domain.utr.UTR;

public record MatchResult(List<SetResult> setResults) {

    public MatchResult(int player1Games, int player2Games) {
        this(List.of(new SetResult(player1Games, player2Games)));
    }
    
    private double scoreOfPlayer1() {
        int sumPlayer1Games = sumPlayer1Games();
        int sumPlayer2Games = sumPlayer2Games();
        double absoluteScore = 1 - (double)Math.min(sumPlayer1Games, sumPlayer2Games) / Math.max(sumPlayer1Games, sumPlayer2Games);
        return sumPlayer1Games > sumPlayer2Games ? absoluteScore : -absoluteScore;
    }
    
    private double scoreOfPlayer2() {
        return - scoreOfPlayer1();
    }
    
    public int sumPlayer1Games() {
        return setResults.stream().mapToInt(SetResult::player1Games).sum();
    }
    
    public int sumPlayer2Games() {
        return setResults.stream().mapToInt(SetResult::player2Games).sum();
    }
    
    public UTR calculateUTRForPlayer1(UTR player2UTR) {
        if(matchType() == Match.MatchType.SUPER_TIE_BREAK || !isMatchLongEnough()) {
            return UTR.UNDEFINED;
        }
        double scoreOfPlayer1 = scoreOfPlayer1();
        return player2UTR.calculateMatchUTR(scoreOfPlayer1);
    }
    
    public UTR calculateUTRForPlayer2(UTR player1UTR) {
        if(matchType() == Match.MatchType.SUPER_TIE_BREAK || !isMatchLongEnough()) {
            return UTR.UNDEFINED;
        }
        double scoreOfPlayer2 = scoreOfPlayer2();
        return player1UTR.calculateMatchUTR(scoreOfPlayer2);
    }
    
    private boolean isMatchLongEnough() {
        return !setResults.isEmpty() && Math.max(setResults.get(0).player1Games(), setResults.get(0).player2Games())  >= 4;
    }

    public MatchResult swap() {
        List<SetResult> reversedSetResults = setResults.stream().map(setResult -> new SetResult(setResult.player2Score, setResult.player1Score)).collect(toList());
        return new MatchResult(reversedSetResults);
    }
    
    public boolean isPlayer1Winner() {
        int setsWon = (int)setResults.stream().filter(SetResult::isPlayer1Winner).count();
        int setsLost = (int)setResults.stream().filter(SetResult::isPlayer2Winner).count();
        return setsWon > setsLost;
    }
    
    public boolean isPlayer2Winner() {
        int setsWon = (int)setResults.stream().filter(SetResult::isPlayer2Winner).count();
        int setsLost = (int)setResults.stream().filter(SetResult::isPlayer1Winner).count();
        return setsWon > setsLost;
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

    public MatchType matchType() {
        if(setResults.size() == 1) {
            SetResult setResult = setResults.get(0);
            if(setResult.isSuperTieBreak()) {
                return MatchType.SUPER_TIE_BREAK;
            } else if(setResult.isFourGamesSet()) {
                return MatchType.ONE_FOUR_GAMES_SET;
            } else {
                return MatchType.ONE_SET;
            }
        } else if(setResults.size() == 2 || setResults.size() == 3) {
            return MatchType.BEST_OF_THREE;
        } else {
            return MatchType.OTHER;
        }
    }

    @Override
    public String toString() {
        return setResults.stream().map(SetResult::toString).collect(joining(" "));
    }
    
    public static record SetResult(int player1Score, int player2Score) {
        
        public int sumGames() {
            return player1Games() + player2Games();
        }
        
        private int player1Games() {
            if(!isSuperTieBreak()) {
                return player1Score;
            } else {
                return isPlayer1Winner() ? 2 : 0;
            }
        }
        
        private int player2Games() {
            if(!isSuperTieBreak()) {
                return player2Score;
            } else {
                return isPlayer2Winner() ? 2 : 0;
            }
        }
        
        public boolean isSuperTieBreak() {
            return Math.max(player1Score, player2Score) >= 10;
        }
        
        public boolean isFourGamesSet() {
            return Math.max(player1Score, player2Score) == 4;
        }

        @Override
        public String toString() {
            return player1Score + ":" + player2Score;
        }
        
        public static SetResult parse(String stringValue) {
            String[] parts = stringValue.split(":");
            return new SetResult(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }
        
        public boolean isPlayer1Winner() {
            return player1Score > player2Score;
        }
        
        public boolean isPlayer2Winner() {
            return player1Score < player2Score;
        }
        
    }

}
