package hu.kits.tennis.domain.ktr;

import java.time.LocalDate;
import java.util.List;

public record KTRHistory(List<KTRHistoryEntry> entries) {

    public static record KTRHistoryEntry(LocalDate date, KTR ktr) {}
    
    public static KTRHistory EMPTY = new KTRHistory(List.of());
    
}
