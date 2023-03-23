package hu.kits.tennis.domain.match;

import java.time.LocalDate;

public record MatchResultInfo(Match match, LocalDate date, MatchResult matchResult) {

}
