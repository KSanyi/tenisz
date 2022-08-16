package hu.kits.tennis.domain.utr;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.common.Clock;
import hu.kits.tennis.domain.tournament.Organizer;
import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentRepository;

public class UTRService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final TournamentRepository tournamentRepository;

    public UTRService(MatchRepository matchRepository, PlayerRepository playerRepository,
            TournamentRepository tournamentRepository) {
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.tournamentRepository = tournamentRepository;
    }

    public UTR calculatePlayersUTR(Player player) {
        List<BookedMatch> matches = matchRepository.loadAllPlayedMatches(player);
        return UTRCalculator.calculatePlayersUTR(player, matches, Clock.today().plusDays(1));
    }
    
    public BookedMatch calculatUTRAndSaveMatch(Match playedMatch) {
        List<BookedMatch> allBookedMatches = matchRepository.loadAllBookedMatches();
        BookedMatch bookedMatch = UTRCalculator.createBookedMatch(playedMatch, allBookedMatches);
        return matchRepository.save(bookedMatch);
    }
    
    public List<BookedMatch> loadMatchesForPlayer(Player player) {
        List<BookedMatch> matches = matchRepository.loadAllPlayedMatches(player);
        
        return matches.stream().map(match -> swapIfNeeed(match, player)).collect(toList());
    }
    
    public List<PlayerWithUTR> calculateUTRRanking() {
        
        logger.info("Calculating UTR ranking");
        
        //List<Player> players = playerRepository.loadAllPlayers().entries();
        List<BookedMatch> allKVTKBookedMatches = getAllKVTKMatches();
        
        List<Player> players = allKVTKBookedMatches.stream()
                .flatMap(b -> Stream.of(b.playedMatch().player1(), b.playedMatch().player1()))
                .distinct()
                .toList();
        
        LocalDate tomorrow = Clock.today().plusDays(1); 
        
        List<PlayerWithUTR> ranking = players.stream()
                .map(player -> new PlayerWithUTR(player, 0, UTRCalculator.calculatePlayersUTR(player, allKVTKBookedMatches, tomorrow)))
                .sorted(comparing(PlayerWithUTR::utr).reversed())
                .collect(toList());
        
        List<PlayerWithUTR> result = ranking.stream()
                .map(playerWithUTR -> new PlayerWithUTR(playerWithUTR.player(), ranking.indexOf(playerWithUTR)+1, playerWithUTR.utr()))
                .collect(toList());
        
        logger.info("UTR ranking calculated with {} entries", result.size());
        
        return result;
    }

    private static BookedMatch swapIfNeeed(BookedMatch match, Player player) {
        if(match.playedMatch().player1().equals(player)) {
            return match;
        } else {
            return match.swap();            
        }
    }
    
    private List<BookedMatch> getAllKVTKMatches() {
        Set<String> kvtkTournamentIds = tournamentRepository.loadAllTournaments().stream()
                .filter(tournament -> tournament.organizer() == Organizer.KVTK)
                .map(Tournament::id)
                .collect(toSet());
            
        return matchRepository.loadAllBookedMatches().stream()
            .filter(b -> kvtkTournamentIds.contains(b.playedMatch().tournamentId()))
            .toList();
    }
    
    public void recalculateAllUTRs(boolean resetUTRGroupsBefore) {
        
        logger.info("Recalculating and saving all UTRs (now only for KVTK)");
        
        if(resetUTRGroupsBefore) {
            List<PlayerWithUTR> utrRanking = calculateUTRRanking();
            for(PlayerWithUTR playerWithUTR : utrRanking) {
                Player player = playerWithUTR.player();
                Player updatedPlayer = new Player(player.id(), player.name(), (int)Math.round(playerWithUTR.utr().value()));
                playerRepository.updatePlayer(updatedPlayer);
            }
        }
        
        List<BookedMatch> allKVTKBookedMatches = getAllKVTKMatches();
        
        List<BookedMatch> recalculatedBookedMatches = UTRCalculator.recalculateAllUTRs(allKVTKBookedMatches);
        
        logger.info("Saving recalculated UTRs");
        
        matchRepository.replaceAllBookedMatches(recalculatedBookedMatches);
        
        logger.info("Recalculated UTRs saved");
    }

    public List<BookedMatch> loadBookedMatches() {
        return matchRepository.loadAllBookedMatches();
    }

    public void deleteMatch(int id) {
        matchRepository.deleteMatch(id);
    }
    
}
