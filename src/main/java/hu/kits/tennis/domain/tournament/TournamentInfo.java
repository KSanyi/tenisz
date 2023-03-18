package hu.kits.tennis.domain.tournament;

import java.time.LocalDate;

public record TournamentInfo(
        String id,
        Organizer organiser,
        LocalDate date, 
        String name,
        String venue,
        int bestOfNSets,
        int numberOfPlayers) {

    public static final TournamentInfo UNKNOWN = new TournamentInfo("NA", Organizer.KVTK, LocalDate.of(1900,1,1), "NA", "NA", 0, 0);

}
