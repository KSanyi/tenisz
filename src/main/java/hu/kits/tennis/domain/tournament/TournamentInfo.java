package hu.kits.tennis.domain.tournament;

import java.time.LocalDate;

public record TournamentInfo(
        String id,
        Organization organiser,
        LocalDate date, 
        String name,
        String venue,
        int bestOfNSets,
        int numberOfPlayers) {

    public static final TournamentInfo UNKNOWN = new TournamentInfo("NA", Organization.KVTK, LocalDate.of(1900,1,1), "NA", "NA", 0, 0);

}
