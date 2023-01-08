package hu.kits.tennis.domain.utr;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import hu.kits.tennis.common.Clock;

public record PlayerStats(Player player,
        List<MatchInfo> matches,
        UTRDetails utrDetails,
        int numberOfTournaments,
        int numberOfMatches,
        int numberOfWins,
        double winPercentage,
        int numberOfLosses,
        double lossPercentage,
        int numberOfGames,
        int numberOfGamesWon,
        double gamesWinPercentage,
        int numberOfGamesLost,
        double gamesLossPercentage,
        Optional<UTRWithDate> utrHigh,
        Optional<MatchInfo> bestUTRMatch,
        Optional<MatchInfo> worstUTRMatch
        ) {

    public static PlayerStats create(Player player, UTRDetails utrDetails, List<MatchInfo> matchInfos) {
        
        int numberOfTournaments = (int)matchInfos.stream().map(match -> match.tournamentInfo().id()).distinct().count();
        int numberOfMatches = matchInfos.size();
        int numberOfWins = (int)matchInfos.stream().filter(match -> match.result().isPlayer1Winner()).count();
        int numberOfLosses = (int)matchInfos.stream().filter(match -> match.result().isPlayer2Winner()).count();
        double winPercentage = numberOfMatches > 0 ? (double)numberOfWins / numberOfMatches : 0;
        double lossPercentage = numberOfMatches > 0 ? (double)numberOfLosses / numberOfMatches : 0;
        
        int numberOfGamesWon = matchInfos.stream().mapToInt(match -> match.result().sumPlayer1Games()).sum();
        int numberOfGamesLost = matchInfos.stream().mapToInt(match -> match.result().sumPlayer2Games()).sum();
        int numberOfGames = numberOfGamesWon + numberOfGamesLost;
        int sumGamesPlayed = numberOfGamesWon + numberOfGamesLost;
        double gamesWinPercentage = sumGamesPlayed > 0 ? (double)numberOfGamesWon / sumGamesPlayed : 0;
        double gamesLossPercentage = sumGamesPlayed > 0 ? (double)numberOfGamesLost / sumGamesPlayed : 0;
        
        Optional<MatchInfo> bestUTRMatch = matchInfos.stream()
                .filter(match -> match.matchUTRForPlayer1().isDefinded())
                .max(Comparator.comparing(MatchInfo::matchUTRForPlayer1));
        
        Optional<MatchInfo> worstUTRMatch = matchInfos.stream()
                .filter(match -> match.matchUTRForPlayer1().isDefinded())
                .min(Comparator.comparing(MatchInfo::matchUTRForPlayer1));
        
        Optional<UTRWithDate> utrHigh = findUTRHeight(utrDetails.utr(), matchInfos);
        
        return new PlayerStats(player,
                matchInfos,
                utrDetails,
                numberOfTournaments,
                numberOfMatches,
                numberOfWins,
                winPercentage,
                numberOfLosses,
                lossPercentage,
                numberOfGames,
                numberOfGamesWon,
                gamesWinPercentage, 
                numberOfGamesLost, 
                gamesLossPercentage,
                utrHigh,
                bestUTRMatch, worstUTRMatch);
    }
    
    private static Optional<UTRWithDate> findUTRHeight(UTR currentUTR, List<MatchInfo> matchInfos) {
        
        Optional<UTRWithDate> utrHeight = matchInfos.stream().max(Comparator.comparing(match -> match.player1UTR()))
                .map(m -> new UTRWithDate(m.player1UTR(), m.date()));
        
        if(utrHeight.isPresent() && currentUTR.compareTo(utrHeight.get().utr()) > 0) {
            return Optional.of(new UTRWithDate(currentUTR, Clock.today()));
        } else {
            return utrHeight;
        }
    }

}
