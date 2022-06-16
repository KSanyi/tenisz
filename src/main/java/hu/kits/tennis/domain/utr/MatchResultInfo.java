package hu.kits.tennis.domain.utr;

import java.time.LocalDate;

public record MatchResultInfo(Match match, LocalDate date, MatchResult matchResult) {

}
