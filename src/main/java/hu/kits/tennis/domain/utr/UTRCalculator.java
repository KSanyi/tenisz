package hu.kits.tennis.domain.utr;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.common.Clock;
import hu.kits.tennis.common.Pair;
import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.domain.match.MatchResult.SetResult;
import hu.kits.tennis.domain.player.Player;

public class UTRCalculator {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private static final int RELEVANT_MATCH_COUNT = 14;
    
    public static UTRDetails calculatePlayersUTRDetails(Player player, List<BookedMatch> allBookedMatches, LocalDate referenceDate, int numberOfTrophies) {
        
        List<BookedMatch> allPlayedMatchesForPlayer = allBookedMatches.stream()
                .filter(match -> match.playedMatch().isPlayed())
                .filter(match -> match.playedMatch().date().isBefore(referenceDate))
                .filter(match -> match.hasPlayed(player))
                .collect(toList());
        
        List<BookedMatch> allRelevantMatchesForPlayer = allPlayedMatchesForPlayer.stream()
                .filter(match -> match.utrOfMatchFor(player).isDefinded())
                .sorted(comparing((BookedMatch m) -> m.playedMatch().date()).reversed())
                .collect(toList());
        
        if(allRelevantMatchesForPlayer.isEmpty()) {
            return new UTRDetails(player.startingUTR(), Set.of(), 0, 0, 0);
        }
        
        List<BookedMatch> lastRelevantMatches = findLastRelevantMatches(allRelevantMatchesForPlayer);
        
        List<BookedMatch> effectiveMatches = lastRelevantMatches.size() < RELEVANT_MATCH_COUNT ?
            addDummyMatches(player, lastRelevantMatches) : lastRelevantMatches;
        
        List<Pair<Double, Integer>> utrWithWeights = effectiveMatches.stream()
                .map(match -> Pair.of(match.utrOfMatchFor(player).value(),
                                      calculateMatchWeight(match, referenceDate)))
                .collect(toList());
        
        double weightedAverage = calculatWeightedAverage(utrWithWeights);
        
        int numberOfWins = (int)allPlayedMatchesForPlayer.stream().filter(b -> b.playedMatch().winner().equals(player)).count();
        
        return new UTRDetails(new UTR(weightedAverage), effectiveMatches, allPlayedMatchesForPlayer.size(), numberOfWins, numberOfTrophies);
    }
    
    private static List<BookedMatch> findLastRelevantMatches(List<BookedMatch> allRelevantMatchesForPlayer) {
        
        LocalDate cutoffDate = allRelevantMatchesForPlayer.size() >= RELEVANT_MATCH_COUNT ? 
                allRelevantMatchesForPlayer.get(RELEVANT_MATCH_COUNT-1).playedMatch().date() : LocalDate.MIN;
        
        return allRelevantMatchesForPlayer.stream().filter(match -> !match.playedMatch().date().isBefore(cutoffDate)).toList();
    }

    private static double calculatWeightedAverage(List<Pair<Double, Integer>> valuesWithWeights) {
        int weightSum = valuesWithWeights.stream().mapToInt(e -> e.second()).sum();
        
        double sumProduct = valuesWithWeights.stream().mapToDouble(e -> e.first() * e.second()).sum();
        
        return sumProduct / weightSum;
    }

    private static List<BookedMatch> addDummyMatches(Player player, List<BookedMatch> matches) {
        
        if(player.startingUTR() == UTR.UNDEFINED) {
            return matches;
        }
        
        int dummyMatchCount = RELEVANT_MATCH_COUNT - matches.size() - 4;
        
        UTR utr = player.startingUTR();
        List<BookedMatch> extendedMatches = new ArrayList<>(matches);
        LocalDate firstMatchDate = matches.get(matches.size()-1).playedMatch().date();
        LocalDate dummyDate = firstMatchDate.minusMonths(1);
        for(int i=0;i<dummyMatchCount;i++) {
            extendedMatches.add(new BookedMatch(new Match(0, null, null, null, dummyDate, player, null, new MatchResult(List.of(new SetResult(6,0)))), 
                    UTR.UNDEFINED, UTR.UNDEFINED, utr, UTR.UNDEFINED));
        }
        return extendedMatches;
    }
    
    private static int calculateMatchWeight(BookedMatch match, LocalDate referenceDate) {
        int dateWeight = dateWeight(match.playedMatch().date(), referenceDate);
        
        return match.playedMatch().matchType().multiplier * dateWeight;
    }
    
    private static int dateWeight(LocalDate matchDate, LocalDate referenceDate) {
        int monthDiff = (int)ChronoUnit.MONTHS.between(matchDate, referenceDate);
        return switch (monthDiff) {
            case 0 -> 10;
            case 1 -> 9;
            case 2 -> 8;
            case 3 -> 7;
            case 4, 5, 6 -> 6;
            case 7, 8, 9 -> 5;
            case 10, 11, 12 -> 4;
            default -> 3;
        };
    }

    public static BookedMatch bookUTRForMatch(Match playedMatch, List<BookedMatch> allPlayedMatches) {
        
        UTR player1UTR = calculatePlayersUTRDetails(playedMatch.player1(), allPlayedMatches, playedMatch.date(), 0).utr();
        UTR player2UTR = calculatePlayersUTRDetails(playedMatch.player2(), allPlayedMatches, playedMatch.date(), 0).utr();
        
        boolean arePlayersComparable = player1UTR.comparable(player2UTR);
        
        UTR matchUTRForPlayer1 = arePlayersComparable ? playedMatch.result().calculateUTRForPlayer1(player2UTR) : UTR.UNDEFINED;
        UTR matchUTRForPlayer2 = arePlayersComparable ? playedMatch.result().calculateUTRForPlayer2(player1UTR) : UTR.UNDEFINED;    
        
        return new BookedMatch(playedMatch, player1UTR, player2UTR, matchUTRForPlayer1, matchUTRForPlayer2);
    }

    public static List<BookedMatch> recalculateAllUTRs(List<BookedMatch> bookedMatches) {
        
        logger.info("Recalculating all UTRs");
        
        List<BookedMatch> allPlayedMatches = bookedMatches.stream()
                .filter(m -> m.playedMatch().isPlayed())
                .sorted(comparing(m -> m.playedMatch().date()))
                .collect(toList());
        
        List<BookedMatch> recalculatedBookedMatches = new ArrayList<>();
        List<BookedMatch> changedBookedMatches = new ArrayList<>();
        for(BookedMatch match : allPlayedMatches) {
            BookedMatch recalculatedBookedMatch = bookUTRForMatch(match.playedMatch(), recalculatedBookedMatches);
            recalculatedBookedMatches.add(recalculatedBookedMatch);
            if(!recalculatedBookedMatch.equals(match)) {
                changedBookedMatches.add(recalculatedBookedMatch);
                logger.debug("Change: {} -> {}", match, recalculatedBookedMatch);
            }
        }
        
        logger.info("All UTRs recalculated successfully from {} matches: {} matches changed", allPlayedMatches.size(), changedBookedMatches.size());
        
        return changedBookedMatches;
    }
    
    public static UTRForecastResult forecast(PlayerWithUTR player1, PlayerWithUTR player2, List<BookedMatch> allMatches, MatchResult matchResult) {
        List<BookedMatch> updatedMatches = new ArrayList<>(allMatches);
        Match newMatch = new Match(0, "", 0, 0, Clock.today(), player1.player(), player2.player(), matchResult);
        BookedMatch newBookedMatch = bookUTRForMatch(newMatch, allMatches);
        updatedMatches.add(newBookedMatch);
        UTR player1NewUTR = calculatePlayersUTRDetails(player1.player(), updatedMatches, Clock.today().plusDays(1), 0).utr();
        UTR player2NewUTR = calculatePlayersUTRDetails(player2.player(), updatedMatches, Clock.today().plusDays(1), 0).utr();
        return new UTRForecastResult(newBookedMatch, player1NewUTR, player2NewUTR);
    }

}
