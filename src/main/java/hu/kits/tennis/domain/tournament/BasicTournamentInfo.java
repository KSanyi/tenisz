package hu.kits.tennis.domain.tournament;

public record BasicTournamentInfo(
        String id,
        Organization organiser,
        String name) {

    public static final BasicTournamentInfo UNKNOWN = new BasicTournamentInfo("NA", Organization.KVTK, "NA");

}
