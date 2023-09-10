package hu.kits.tennis.domain.ktr;

import java.util.Comparator;
import java.util.List;

import hu.kits.tennis.common.Clock;
import hu.kits.tennis.domain.ktr.KTRHistory.KTRHistoryEntry;
import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.domain.player.Player;

public record PlayerStats(Player player,
        List<MatchInfo> matches,
        KTRDetails ktrDetails,
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
        KTRHistoryEntry ktrHigh,
        MatchInfo bestKTRMatch,
        MatchInfo worstKTRMatch,
        MatchInfo winAgainstStrongest,
        KTRHistory ktrHistory,
        int rank
        ) {

    public static PlayerStats create(Player player, KTRDetails ktrDetails, List<MatchInfo> matchInfos, KTRHistory ktrHistory, int rank) {
        
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
        
        MatchInfo bestKTRMatch = matchInfos.stream()
                .filter(match -> match.matchKTRForPlayer1().isDefinded())
                .max(Comparator.comparing(MatchInfo::matchKTRForPlayer1))
                .get();
        
        MatchInfo worstKTRMatch = matchInfos.stream()
                .filter(match -> match.matchKTRForPlayer1().isDefinded())
                .min(Comparator.comparing(MatchInfo::matchKTRForPlayer1))
                .get();
        
        MatchInfo winAgainstStrongest = matchInfos.stream()
                .filter(match -> match.player2KTR().isDefinded())
                .filter(match -> match.result().isPlayer1Winner())
                .max(Comparator.comparing(MatchInfo::player2KTR))
                .orElse(null);
        
        KTRHistoryEntry ktrHigh = findKTRHeight(ktrDetails.ktr(), matchInfos);
        
        return new PlayerStats(player,
                matchInfos,
                ktrDetails,
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
                ktrHigh,
                bestKTRMatch, worstKTRMatch,
                winAgainstStrongest,
                ktrHistory,
                rank);
    }
    
    private static double calculatePercentage(double x, double n) {
        return n > 0 ? 100 * x / n : 0;
    }
    
    
    private static KTRHistoryEntry findKTRHeight(KTR currentKTR, List<MatchInfo> matchInfos) {
        
        KTRHistoryEntry ktrHeight = matchInfos.stream()
                .max(Comparator.comparing(match -> match.player1KTR()))
                .map(m -> new KTRHistoryEntry(m.date(), m.player1KTR()))
                .get();
        
        if(currentKTR.compareTo(ktrHeight.ktr()) > 0) {
            return new KTRHistoryEntry(Clock.today(), currentKTR);
        } else {
            return ktrHeight;
        }
    }

}
