package hu.kits.tennis.domain.tournament;

import static java.util.stream.Collectors.toList;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.domain.tournament.Tournament.Status;
import hu.kits.tennis.domain.utr.BookedMatch;
import hu.kits.tennis.domain.utr.Match;
import hu.kits.tennis.domain.utr.MatchRepository;
import hu.kits.tennis.domain.utr.Player;

public class TournamentService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;

    public TournamentService(TournamentRepository tournamentRepository, MatchRepository matchRepository) {
        this.tournamentRepository = tournamentRepository;
        this.matchRepository = matchRepository;
    }

    public List<Tournament> loadAllTournaments() {
        return tournamentRepository.loadAllTournaments();
    }
    
    public Tournament createTournament(String name, String venue, LocalDate date, Tournament.Type type, int bestOfNSets) {
        Tournament tournament = Tournament.createNew(name, venue, date, type, bestOfNSets);
        tournamentRepository.createTournament(tournament);
        return tournament;
    }
    
    public void updateTournamentName(Tournament tournament, String updatedName) {
        tournamentRepository.updateTournamentName(tournament.id(), updatedName);
        logger.info("Tournament name is updated: {} -> {}", tournament.name(), updatedName);
    }
    
    public void updateTournamentDate(Tournament tournament, LocalDate updatedDate) {
        tournamentRepository.updateTournamentDate(tournament.id(), updatedDate);
        logger.info("Tournament date is updated: {} -> {}", tournament.date(), updatedDate);
    }
    
    public void updateTournamentVenue(Tournament tournament, String venue) {
        tournamentRepository.updateTournamentVenue(tournament.id(), venue);
        logger.info("Tournament venue is updated: {} -> {}", tournament.venue(), venue);
    }
    
    public void updateTournamentType(Tournament tournament, Tournament.Type type) {
        tournamentRepository.updateTournamentType(tournament.id(), type);
        logger.info("Tournament type is updated: {} -> {}", tournament.type(), type);
    }
    
    public void updateContestants(Tournament tournament, List<Player> players) {
        
        List<Contestant> contestants = IntStream.range(0, players.size())
                .mapToObj(rank -> new Contestant(players.get(rank), rank))
                .collect(toList());
        
        tournamentRepository.updateContestants(tournament.id(), contestants);
        logger.info("Contestants updated: {} -> {}", tournament.contestants(), contestants);
    }
    
    public void deleteTournament(String tournamentId) {
        tournamentRepository.deleteTournament(tournamentId);
    }
    
    public Optional<Tournament> findTournament(String tournamentId) {
        return tournamentRepository.findTournament(tournamentId);
    }
    
    public void createMatches(String tournamentId, DrawMode drawMode) {
        
        Tournament tournament = tournamentRepository.findTournament(tournamentId).get();
        
        if(tournament.status() == Status.DRAFT) {
            
            matchRepository.deleteMatchesForTournament(tournament.id());
            
            List<Player> players = tournament.players();
            int matchNumber = 1;
            List<Match> matches = new ArrayList<>();
            for(int i=0;i<players.size();i+=2) {
                Match match = Match.createNew(tournament.id(), matchNumber, tournament.date(), players.get(i), players.get(i+1));
                matchRepository.save(new BookedMatch(match, null, null, null, null));
                matches.add(match);
                matchNumber++;
            }
            
            logger.info("{} matches created for {}", matches.size(), tournament);
        }
        
    }

}
