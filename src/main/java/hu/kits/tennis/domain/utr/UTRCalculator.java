package hu.kits.tennis.domain.utr;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.common.Clock;
import hu.kits.tennis.common.Pair;
import hu.kits.tennis.domain.utr.MatchResult.SetResult;

public class UTRCalculator {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private static final int RELEVANT_MATCH_COUNT = 14;
    
    public static UTR calculatePlayersUTR(Player player, List<BookedMatch> allBookedMatches, LocalDate date) {
        
        List<BookedMatch> allRelevantMatchesForPlayer = allBookedMatches.stream()
                .filter(match -> match.playedMatch().date().isBefore(date))
                .filter(match -> match.hasPlayed(player))
                .filter(match -> match.utrOfMatchFor(player).isDefinded())
                .sorted(comparing((BookedMatch m) -> m.playedMatch().date()).reversed())
                .collect(toList());
        
        if(allRelevantMatchesForPlayer.isEmpty()) {
            return player.startingUTR();
        }
        
        List<BookedMatch> lastRelevantMatches = findLastRelevantMatches(allRelevantMatchesForPlayer);
        
        List<BookedMatch> effectiveMatches = lastRelevantMatches.size() < RELEVANT_MATCH_COUNT ?
            addDummyMatches(player, lastRelevantMatches) : lastRelevantMatches;
        
        List<Pair<Double, Integer>> utrWithWeights = effectiveMatches.stream()
                .map(match -> Pair.of(match.utrOfMatchFor(player).value(),
                                        calculateMatchWeight(match)))
                .collect(toList());
        
        double weightedAverage = calculatWeightedAverage(utrWithWeights);
        
        return new UTR(weightedAverage);
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
    
    private static int calculateMatchWeight(BookedMatch match) {
        int dateWeight = dateWeight(match.playedMatch().date());
        
        return match.playedMatch().matchType().multiplier * dateWeight;
    }
    
    private static int dateWeight(LocalDate matchDate) {
        int monthDiff = (int)ChronoUnit.MONTHS.between(matchDate, Clock.today());
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

    public static BookedMatch createBookedMatch(Match playedMatch, List<BookedMatch> allPlayedMatches) {
        
        UTR player1UTR = calculatePlayersUTR(playedMatch.player1(), allPlayedMatches, playedMatch.date());
        UTR player2UTR = calculatePlayersUTR(playedMatch.player2(), allPlayedMatches, playedMatch.date());
        
        boolean arePlayersComparable = player1UTR.comparable(player2UTR);
        
        UTR matchUTRForPlayer1 = arePlayersComparable ? playedMatch.result().calculateUTRForPlayer1(player2UTR) : UTR.UNDEFINED;
        UTR matchUTRForPlayer2 = arePlayersComparable ? playedMatch.result().calculateUTRForPlayer2(player1UTR) : UTR.UNDEFINED;    
        
        return new BookedMatch(playedMatch, player1UTR, player2UTR, matchUTRForPlayer1, matchUTRForPlayer2);
    }

    public static List<BookedMatch> recalculateAllUTRs(List<BookedMatch> bookedMatches) {
        
        logger.info("Recalculating all UTRs");
        
        List<BookedMatch> recalculatedBookedMatches = new ArrayList<>();
        
        List<Match> allPlayedMatches = bookedMatches.stream()
                .map(BookedMatch::playedMatch)
                .sorted(comparing(Match::date))
                .collect(toList());
        
        for(Match playedMatch : allPlayedMatches) {
            BookedMatch recalculatedBookedMatch = createBookedMatch(playedMatch, recalculatedBookedMatches);
            recalculatedBookedMatches.add(recalculatedBookedMatch);
        }
        
        logger.info("All UTRs recalculated successfully from {} matches", allPlayedMatches.size());
        
        return recalculatedBookedMatches;
    }

}
