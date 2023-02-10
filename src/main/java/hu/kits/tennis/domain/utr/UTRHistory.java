package hu.kits.tennis.domain.utr;

import java.time.LocalDate;
import java.util.List;

public record UTRHistory(List<UTRHistoryEntry> entries) {

    public static record UTRHistoryEntry(LocalDate date, UTR utr) {}
    
    public static UTRHistory EMPTY = new UTRHistory(List.of());
    
}
