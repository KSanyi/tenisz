package hu.kits.tennis.domain.utr;

import java.util.Set;

public record UTRDetails(UTR utr,
        Set<Integer> relevantMatchIds,
        int numberOfMatches, 
        int numberOfWins, 
        int numberOfTrophies) {

}
