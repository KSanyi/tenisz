package hu.kits.tennis.domain.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import hu.kits.tennis.domain.tournament.Contestant;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentRepository;
import hu.kits.tennis.domain.utr.BookedMatch;
import hu.kits.tennis.domain.utr.MatchRepository;
import hu.kits.tennis.domain.utr.Player;
import hu.kits.tennis.domain.utr.PlayerRepository;

public class ReconciliationService {

    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final TournamentRepository tournamentRepository;
    
    public ReconciliationService(PlayerRepository playerRepository, MatchRepository matchRepository, TournamentRepository tournamentRepository) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.tournamentRepository = tournamentRepository;
    }

    public void reconcilePlayers(Player player, Player duplicate) {

        List<BookedMatch> updatableMatches = matchRepository.loadAllPlayedMatches(duplicate);
        
        for(BookedMatch match : updatableMatches) {
            if(match.playedMatch().player1().id().equals(duplicate.id())) {
                matchRepository.setPlayer1(match.playedMatch().id(), player);
            } else {
                matchRepository.setPlayer2(match.playedMatch().id(), player);
            }
        }
        
        List<Tournament> tournaments = tournamentRepository.loadAllTournaments(duplicate);
        for(Tournament tournament : tournaments) {
            List<Contestant> contestants = tournament.contestants();
            Optional<Contestant> contestant = contestants.stream().filter(c -> c.player().id().equals(duplicate.id())).findAny();
            if(contestant.isPresent()) {
                List<Contestant> updatedContestants = updateContestants(contestants, player, contestant.get());
                tournamentRepository.updateContestants(tournament.id(), updatedContestants);
            }
        }
        
        playerRepository.deletePlayer(duplicate);
    }

    private static List<Contestant> updateContestants(List<Contestant> contestants, Player player, Contestant contestant) {
        List<Contestant> updatedContestant = new ArrayList<>(contestants);
        updatedContestant.remove(contestant);
        updatedContestant.add(new Contestant(player, contestant.rank()));
        return updatedContestant;
    }
    
}
