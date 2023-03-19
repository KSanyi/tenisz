package hu.kits.tennis.domain.utr;

import static hu.kits.tennis.testutil.TestUtil.date;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import hu.kits.tennis.testutil.TestUtil;

public class UTRCalculatorTest2 {

    @Test
    void recalculationIsIdempotent() {
    
        List<Match> matches = new ArrayList<>();
        
        matches.addAll(TestUtil.generateRandomTournament(date("2022-01-01"), 10));
        matches.addAll(TestUtil.generateRandomTournament(date("2022-01-15"), 10));
        
        List<BookedMatch> unbookedMatches = matches.stream().map(m -> new BookedMatch(m, UTR.UNDEFINED, UTR.UNDEFINED, UTR.UNDEFINED, UTR.UNDEFINED)).toList();
        
        List<BookedMatch> bookedMatches = UTRCalculator.recalculateAllUTRs(unbookedMatches);
        
        List<BookedMatch> rebookedMatches = UTRCalculator.recalculateAllUTRs(bookedMatches);
        
        Assertions.assertTrue(rebookedMatches.isEmpty());
    }
    
    @Test
    void recalculationIsMatchSortOrderIdempotent() {
    
        List<Match> matches = new ArrayList<>();
        
        matches.addAll(TestUtil.generateRandomTournament(date("2022-01-01"), 10));
        matches.addAll(TestUtil.generateRandomTournament(date("2022-01-15"), 10));
        
        List<BookedMatch> unbookedMatches = matches.stream().map(m -> new BookedMatch(m, UTR.UNDEFINED, UTR.UNDEFINED, UTR.UNDEFINED, UTR.UNDEFINED)).collect(Collectors.toList());
        List<BookedMatch> bookedMatches1 = UTRCalculator.recalculateAllUTRs(unbookedMatches);
        Collections.shuffle(unbookedMatches);        
        List<BookedMatch> bookedMatches2 = UTRCalculator.recalculateAllUTRs(unbookedMatches);
        
        Assertions.assertTrue(bookedMatches1.containsAll(bookedMatches2));
        Assertions.assertTrue(bookedMatches2.containsAll(bookedMatches1));
    }
}
