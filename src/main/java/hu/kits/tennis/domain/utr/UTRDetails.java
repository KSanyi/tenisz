package hu.kits.tennis.domain.utr;

import java.util.Collection;

public record UTRDetails(UTR utr, Collection<BookedMatch> relevantMatches) {

}
