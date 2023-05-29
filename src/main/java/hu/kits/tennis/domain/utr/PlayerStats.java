package hu.kits.tennis.domain.utr;

import java.util.Comparator;
import java.util.List;

import hu.kits.tennis.common.Clock;
import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.utr.UTRHistory.UTRHistoryEntry;

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
        UTRHistoryEntry utrHigh,
        MatchInfo bestUTRMatch,
        MatchInfo worstUTRMatch,
        UTRHistory utrHistory
        ) {

    public static PlayerStats create(Player player, UTRDetails utrDetails, List<MatchInfo> matchInfos, UTRHistory utrHistory) {
        
        int numberOfTournaments = (int)matchInfos.stream().map(match -> match.tournamentInfo().id()).distinct().count();
        int numberOfMatches = matchInfos.size();
        int numberOfWins = (int)matchInfos.stream().filter(match -> match.result().isPlayer1Winner()).count();
        int numberOfLosses = (int)matchInfos.stream().filter(match -> match.result().isPlayer2Winner()).count();
        double winPercentage = calculatePercentage(numberOfWins, numberOfMatches);
        double lossPercentage = calculatePercentage(numberOfLosses, numberOfMatches);
        
        int numberOfGamesWon = matchInfos.stream().mapToInt(match -> match.result().sumPlayer1Games()).sum();
        int numberOfGamesLost = matchInfos.stream().mapToInt(match -> match.result().sumPlayer2Games()).sum();
        int numberOfGames = numberOfGamesWon + numberOfGamesLost;
        int sumGamesPlayed = numberOfGamesWon + numberOfGamesLost;
        double gamesWinPercentage = calculatePercentage(numberOfGamesWon, sumGamesPlayed);
        double gamesLossPercentage = calculatePercentage(numberOfGamesLost, sumGamesPlayed);
        
        MatchInfo bestUTRMatch = matchInfos.stream()
                .filter(match -> match.matchUTRForPlayer1().isDefinded())
                .max(Comparator.comparing(MatchInfo::matchUTRForPlayer1))
                .get();
        
        MatchInfo worstUTRMatch = matchInfos.stream()
                .filter(match -> match.matchUTRForPlayer1().isDefinded())
                .min(Comparator.comparing(MatchInfo::matchUTRForPlayer1))
                .get();
        
        UTRHistoryEntry utrHigh = findUTRHeight(utrDetails.utr(), matchInfos);
        
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
                bestUTRMatch, worstUTRMatch,
                utrHistory);
    }
    
    private static double calculatePercentage(double x, double n) {
        return n > 0 ? 100 * x / n : 0;
    }
    
    
    private static UTRHistoryEntry findUTRHeight(UTR currentUTR, List<MatchInfo> matchInfos) {
        
        UTRHistoryEntry utrHeight = matchInfos.stream()
                .max(Comparator.comparing(match -> match.player1UTR()))
                .map(m -> new UTRHistoryEntry(m.date(), m.player1UTR()))
                .get();
        
        if(currentUTR.compareTo(utrHeight.utr()) > 0) {
            return new UTRHistoryEntry(Clock.today(), currentUTR);
        } else {
            return utrHeight;
        }
    }

}
