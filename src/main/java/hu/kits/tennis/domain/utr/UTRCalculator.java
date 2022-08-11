package hu.kits.tennis.domain.utr;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.common.Pair;
import hu.kits.tennis.domain.utr.MatchResult.SetResult;

public class UTRCalculator {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private static final int RELEVANT_MATCH_COUNT = 14;
    private static final UTR DEFAULT_UTR = new UTR(7.0);
    
    public static UTR calculatePlayersUTR(Player player, List<BookedMatch> allBookedMatches, LocalDate date) {
        
        List<BookedMatch> lastRelevantMatchesFoPlayer = allBookedMatches.stream()
                .filter(match -> match.playedMatch().date().isBefore(date))
                .filter(match -> match.hasPlayed(player))
                .filter(match -> ! match.utrOfMatchFor(player).isUndefinded())
                .sorted(comparing((BookedMatch m) -> m.playedMatch().date()).reversed())
                .limit(RELEVANT_MATCH_COUNT)
                .collect(toList());
        
        List<BookedMatch> effectiveMatches = lastRelevantMatchesFoPlayer.size() < RELEVANT_MATCH_COUNT ?
            addDummyMatches(player, lastRelevantMatchesFoPlayer) : lastRelevantMatchesFoPlayer;
        
        List<Pair<Double, Integer>> utrWithWeights = effectiveMatches.stream()
                .map(match -> new Pair<>(match.utrOfMatchFor(player).value(),
                                        calculateMatchWeight(effectiveMatches.indexOf(match), match)))
                .collect(toList());
        
        double weightedAverage = calculatWeightedAverage(utrWithWeights);
        
        return new UTR(weightedAverage);
    }
    
    private static double calculatWeightedAverage(List<Pair<Double, Integer>> valuesWithWeights) {
        int weightSum = valuesWithWeights.stream().mapToInt(e -> e.second()).sum();
        
        double sumProduct = valuesWithWeights.stream().mapToDouble(e -> e.first() * e.second()).sum();
        
        return sumProduct / weightSum;
    }

    private static List<BookedMatch> addDummyMatches(Player player, List<BookedMatch> matches) {
        UTR utr = player.utrGroup() != null && player.utrGroup() > 0 ? new UTR((double)player.utrGroup()) : DEFAULT_UTR;
        int matchesToAdd = 1;
        List<BookedMatch> extendedMatches = new ArrayList<>(matches);
        for(int i=0;i<matchesToAdd;i++) {
            extendedMatches.add(new BookedMatch(new Match(0, null, null, null, LocalDate.MIN, player, null, new MatchResult(List.of(new SetResult(6,0)))), 
                    UTR.UNDEFINED, UTR.UNDEFINED, utr, UTR.UNDEFINED));
        }
        return extendedMatches;
    }
    
    private static int calculateMatchWeight(int matchIndex, BookedMatch match) {
        return (RELEVANT_MATCH_COUNT - matchIndex) + 5;
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
