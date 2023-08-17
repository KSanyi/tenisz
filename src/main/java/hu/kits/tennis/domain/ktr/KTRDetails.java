package hu.kits.tennis.domain.ktr;

import java.util.Set;

public record KTRDetails(KTR ktr,
        Set<Integer> relevantMatchIds,
        int numberOfMatches, 
        int numberOfWins, 
        int numberOfTrophies) {

}
