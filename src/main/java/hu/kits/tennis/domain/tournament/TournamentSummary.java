package hu.kits.tennis.domain.tournament;

import java.time.LocalDate;

import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.tournament.TournamentParams.Level;
import hu.kits.tennis.domain.tournament.TournamentParams.Status;
import hu.kits.tennis.domain.tournament.TournamentParams.Surface;
import hu.kits.tennis.domain.tournament.TournamentParams.Type;
import hu.kits.tennis.domain.tournament.TournamentParams.VenueType;

public record TournamentSummary(String id,
        Organization organiser,
        Type type,
        Level levelFrom,
        Level levelTo,
        String venue,
        CourtInfo courtInfo,
        String name,
        LocalDate date,
        Status status,
        int numberOfMatchesPlayed,
        int numberOfPlayers,
        Player winner,
        String description) {
    
    public String levelDisplay() {
        return levelFrom == levelTo ? levelFrom.toString() : (levelFrom + "-" + levelTo);
    }
    
    public static record CourtInfo(int numberOfCourts, Surface surface, VenueType venueType) {}

}
