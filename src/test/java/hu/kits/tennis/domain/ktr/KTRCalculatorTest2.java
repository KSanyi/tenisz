package hu.kits.tennis.domain.ktr;

import static hu.kits.tennis.testutil.TestUtil.date;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.testutil.TestUtil;

public class KTRCalculatorTest2 {

    @Test
    void recalculationIsIdempotent() {
    
        List<Match> matches = new ArrayList<>();
        
        matches.addAll(TestUtil.generateRandomTournament(date("2022-01-01"), 10));
        matches.addAll(TestUtil.generateRandomTournament(date("2022-01-15"), 10));
        
        List<BookedMatch> unbookedMatches = matches.stream().map(m -> new BookedMatch(m, KTR.UNDEFINED, KTR.UNDEFINED, KTR.UNDEFINED, KTR.UNDEFINED)).toList();
        
        List<BookedMatch> bookedMatches = KTRCalculator.recalculateAllKTRs(unbookedMatches, List.of());
        
        List<BookedMatch> rebookedMatches = KTRCalculator.recalculateAllKTRs(bookedMatches, List.of());
        
        Assertions.assertTrue(rebookedMatches.isEmpty());
    }
    
    @Test
    void recalculationIsMatchSortOrderIdempotent() {
    
        List<Match> matches = new ArrayList<>();
        
        matches.addAll(TestUtil.generateRandomTournament(date("2022-01-01"), 10));
        matches.addAll(TestUtil.generateRandomTournament(date("2022-01-15"), 10));
        
        List<BookedMatch> unbookedMatches = matches.stream().map(m -> new BookedMatch(m, KTR.UNDEFINED, KTR.UNDEFINED, KTR.UNDEFINED, KTR.UNDEFINED)).collect(Collectors.toList());
        List<BookedMatch> bookedMatches1 = KTRCalculator.recalculateAllKTRs(unbookedMatches, List.of());
        Collections.shuffle(unbookedMatches);        
        List<BookedMatch> bookedMatches2 = KTRCalculator.recalculateAllKTRs(unbookedMatches, List.of());
        
        Assertions.assertTrue(bookedMatches1.containsAll(bookedMatches2));
        Assertions.assertTrue(bookedMatches2.containsAll(bookedMatches1));
    }
}
