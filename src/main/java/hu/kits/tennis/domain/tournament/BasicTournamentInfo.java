package hu.kits.tennis.domain.tournament;

import java.time.LocalDate;

public record BasicTournamentInfo(
        String id,
        Organization organiser,
        String name,
        LocalDate date) {

    public static final BasicTournamentInfo UNKNOWN = new BasicTournamentInfo("NA", Organization.KVTK, "NA", LocalDate.of(2000,1,1));

}
