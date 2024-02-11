package hu.kits.tennis.domain.ktr;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.common.Clock;
import hu.kits.tennis.common.Pair;
import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchResult;
import hu.kits.tennis.domain.match.MatchResult.SetResult;
import hu.kits.tennis.domain.player.Player;

public class KTRCalculator {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private static final int RELEVANT_MATCH_COUNT = 25;
    
    public static KTRDetails calculatePlayersKTRDetails(Player player, List<BookedMatch> allBookedMatches, LocalDate referenceDate, int numberOfTrophies, List<KTRUpdate> ktrUpdates) {
        
        KTR ktr = calculateKTR(player, allBookedMatches, referenceDate, ktrUpdates);
        
        List<BookedMatch> allPlayedMatchesForPlayer = allBookedMatches.stream()
                .filter(match -> match.playedMatch().isPlayed())
                .filter(match -> match.playedMatch().date().isBefore(referenceDate))
                .filter(match -> match.hasPlayed(player))
                .collect(toList());
        
        List<BookedMatch> allRelevantMatchesForPlayer = allPlayedMatchesForPlayer.stream()
                .filter(match -> match.ktrOfMatchFor(player).isDefinded())
                .sorted(comparing((BookedMatch m) -> m.playedMatch().date()).reversed())
                .collect(toList());
        
        if(allRelevantMatchesForPlayer.isEmpty()) {
            return new KTRDetails(ktr, Set.of(), 0, 0, 0);
        }
        
        List<BookedMatch> lastRelevantMatches = findLastRelevantMatches(allRelevantMatchesForPlayer);
        
        Set<Integer> relevantMatchIds = lastRelevantMatches.stream()
                .map(b -> b.playedMatch().id())
                .collect(toSet());
        
        int numberOfWins = (int)allPlayedMatchesForPlayer.stream().filter(b -> b.playedMatch().winner().equals(player)).count();
        
        return new KTRDetails(ktr, relevantMatchIds, allPlayedMatchesForPlayer.size(), numberOfWins, numberOfTrophies);
    }
    
    public static KTR calculateKTR(Player player, List<BookedMatch> allBookedMatches, LocalDate referenceDate, List<KTRUpdate> ktrUpdates) {
        
        Optional<KTRUpdate> relevantKTRUpdate = calculateRelevantKTRUpdate(player, referenceDate, ktrUpdates);
        
        List<BookedMatch> allPlayedMatchesForPlayer = allBookedMatches.stream()
                .filter(match -> match.playedMatch().isPlayed())
                .filter(match -> match.playedMatch().date().isBefore(referenceDate))
                .filter(match -> match.playedMatch().date().isAfter(relevantKTRUpdate.map(KTRUpdate::date).orElse(LocalDate.MIN)))
                .filter(match -> match.hasPlayed(player))
                .collect(toList());
        
        List<BookedMatch> allRelevantMatchesForPlayer = allPlayedMatchesForPlayer.stream()
                .filter(match -> match.ktrOfMatchFor(player).isDefinded())
                .sorted(comparing((BookedMatch m) -> m.playedMatch().date()).reversed())
                .collect(toList());
        
        KTR baseKTR = relevantKTRUpdate.map(KTRUpdate::updatedKTR).orElse(player.startingKTR());
        
        if(allRelevantMatchesForPlayer.isEmpty()) {
            return baseKTR;
        }
        
        List<BookedMatch> lastRelevantMatches = findLastRelevantMatches(allRelevantMatchesForPlayer);
        
        List<BookedMatch> effectiveMatches = lastRelevantMatches.size() < RELEVANT_MATCH_COUNT ?
            addDummyMatches(player, baseKTR, lastRelevantMatches) : lastRelevantMatches;
        
        List<Pair<Double, Integer>> ktrWithWeights = effectiveMatches.stream()
                .map(match -> Pair.of(match.ktrOfMatchFor(player).value(),
                                      calculateMatchWeight(match, referenceDate)))
                .collect(toList());
        
        double weightedAverage = calculatWeightedAverage(ktrWithWeights);
        
        return new KTR(weightedAverage);
    }
    
    private static Optional<KTRUpdate> calculateRelevantKTRUpdate(Player player, LocalDate referenceDate, List<KTRUpdate> ktrUpdates) {
        return ktrUpdates.stream()
            .filter(ktrUpdate -> ktrUpdate.player().equals(player))
            .filter(ktrUpdate -> !ktrUpdate.date().isAfter(referenceDate))
            .max(Comparator.comparing(KTRUpdate::date));
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

    private static List<BookedMatch> addDummyMatches(Player player, KTR baseKTR, List<BookedMatch> matches) {
        
        if(baseKTR == KTR.UNDEFINED) {
            return matches;
        }
        
        int dummyMatchCount = (int)Math.max(Math.round(RELEVANT_MATCH_COUNT * 0.5) - matches.size(), 0);
        
        List<BookedMatch> extendedMatches = new ArrayList<>(matches);
        LocalDate firstMatchDate = matches.get(matches.size()-1).playedMatch().date();
        LocalDate dummyDate = firstMatchDate.minusMonths(1);
        for(int i=0;i<dummyMatchCount;i++) {
            extendedMatches.add(new BookedMatch(new Match(0, null, null, null, dummyDate, player, null, new MatchResult(List.of(new SetResult(6,0)))), 
                    KTR.UNDEFINED, KTR.UNDEFINED, baseKTR, KTR.UNDEFINED));
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

    public static BookedMatch bookKTRForMatch(Match playedMatch, List<BookedMatch> allPlayedMatches, List<KTRUpdate> ktrUpdates) {
        
        KTR player1KTR = calculateKTR(playedMatch.player1(), allPlayedMatches, playedMatch.date(), ktrUpdates);
        KTR player2KTR = calculateKTR(playedMatch.player2(), allPlayedMatches, playedMatch.date(), ktrUpdates);
        
        boolean arePlayersComparable = player1KTR.comparable(player2KTR);
        
        KTR matchKTRForPlayer1 = arePlayersComparable ? playedMatch.result().calculateKTRForPlayer1(player2KTR) : KTR.UNDEFINED;
        KTR matchKTRForPlayer2 = arePlayersComparable ? playedMatch.result().calculateKTRForPlayer2(player1KTR) : KTR.UNDEFINED;    
        
        return new BookedMatch(playedMatch, player1KTR, player2KTR, matchKTRForPlayer1, matchKTRForPlayer2);
    }

    public static List<BookedMatch> recalculateAllKTRs(List<BookedMatch> bookedMatches, List<KTRUpdate> ktrUpdates) {
        
        logger.info("Recalculating all KTRs");
        
        List<BookedMatch> allPlayedMatches = bookedMatches.stream()
                .filter(m -> m.playedMatch().isPlayed())
                .sorted(comparing(m -> m.playedMatch().date()))
                .collect(toList());
        
        List<BookedMatch> recalculatedBookedMatches = new ArrayList<>();
        List<BookedMatch> changedBookedMatches = new ArrayList<>();
        for(BookedMatch match : allPlayedMatches) {
            BookedMatch recalculatedBookedMatch = bookKTRForMatch(match.playedMatch(), recalculatedBookedMatches, ktrUpdates);
            recalculatedBookedMatches.add(recalculatedBookedMatch);
            if(!recalculatedBookedMatch.equals(match)) {
                changedBookedMatches.add(recalculatedBookedMatch);
                logger.debug("Change: {} -> {}", match, recalculatedBookedMatch);
            }
        }
        
        logger.info("All KTRs recalculated successfully from {} matches: {} matches changed", allPlayedMatches.size(), changedBookedMatches.size());
        
        return changedBookedMatches;
    }
    
    public static KTRForecastResult forecast(PlayerWithKTR player1, PlayerWithKTR player2, List<BookedMatch> allMatches, List<KTRUpdate> ktrUpdates, MatchResult matchResult) {
        List<BookedMatch> updatedMatches = new ArrayList<>(allMatches);
        Match newMatch = new Match(0, "", 0, 0, Clock.today(), player1.player(), player2.player(), matchResult);
        BookedMatch newBookedMatch = bookKTRForMatch(newMatch, allMatches, ktrUpdates);
        updatedMatches.add(newBookedMatch);
        KTR player1NewKTR = calculateKTR(player1.player(), updatedMatches, Clock.today().plusDays(1), ktrUpdates);
        KTR player2NewKTR = calculateKTR(player2.player(), updatedMatches, Clock.today().plusDays(1), ktrUpdates);
        return new KTRForecastResult(newBookedMatch, player1NewKTR, player2NewKTR);
    }

}
