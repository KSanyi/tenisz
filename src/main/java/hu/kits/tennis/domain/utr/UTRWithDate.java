package hu.kits.tennis.domain.utr;

import java.time.LocalDate;

public record UTRWithDate(UTR utr, LocalDate date) {

}
