package hu.kits.tennis.domain.utr;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.common.Clock;

public class UTRService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    public UTRService(MatchRepository matchRepository, PlayerRepository playerRepository) {
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
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
        
        List<Player> players = playerRepository.loadAllPlayers().entries();
        List<BookedMatch> allBookedMatches = matchRepository.loadAllBookedMatches();
        
        LocalDate tomorrow = Clock.today().plusDays(1); 
        
        List<PlayerWithUTR> ranking = players.stream()
                .map(player -> new PlayerWithUTR(player, 0, UTRCalculator.calculatePlayersUTR(player, allBookedMatches, tomorrow)))
                .sorted(comparing(PlayerWithUTR::utr).reversed())
                .collect(toList());
        
        /*
        String rankingString = ranking.stream().map(playerWithRanking -> playerWithRanking.player().id() + "\t" + playerWithRanking.player().name() + "\t" + playerWithRanking.utr().value()).collect(Collectors.joining("\n"));
        
        try {
            Files.write(Paths.get("c:\\Users\\Sanyi\\Desktop\\partner-utr-groups.txt"), rankingString.getBytes());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */
        
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
    
    public void recalculateAllUTRs() {
        
        logger.info("Recalculating and saving all UTRs");
        
        List<BookedMatch> bookedMatches = matchRepository.loadAllBookedMatches();
        
        List<BookedMatch> recalculatedBookedMatches = UTRCalculator.recalculateAllUTRs(bookedMatches);
        
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
