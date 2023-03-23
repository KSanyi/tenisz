package hu.kits.tennis.domain.tournament;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.tournament.TournamentParams.Status;
import hu.kits.tennis.domain.tournament.TournamentParams.Structure;

public interface TournamentRepository {

    List<Tournament> loadAllTournaments();
    
    List<Tournament> loadAllTournaments(Player player);
    
    Optional<Tournament> findTournament(String tournamentId);
    
    void createTournament(Tournament tournament);
    
    void deleteTournament(String tournamentId);

    void updateTournamentName(String id, String updatedName);

    void updateTournamentDate(String id, LocalDate updatedDate);

    void updateTournamentVenue(String id, String venue);

    void updateTournamentType(String id, Structure structure);
    
    void updateTournamentStatus(String tournamentId, Status status);

    void updateContestants(String id, List<Contestant> contestants);

}
