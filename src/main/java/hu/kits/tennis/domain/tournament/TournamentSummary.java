package hu.kits.tennis.domain.tournament;

import java.time.LocalDate;

import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.tournament.TournamentParams.Level;
import hu.kits.tennis.domain.tournament.TournamentParams.Status;
import hu.kits.tennis.domain.tournament.TournamentParams.Type;

public record TournamentSummary(String id,
        Organization organiser,
        Type type,
        Level categoryFrom,
        Level categoryTo,
        String name,
        LocalDate date,
        Status status,
        int numberOfMatchesPlayed,
        int numberOfPlayers,
        Player winner) {

}
