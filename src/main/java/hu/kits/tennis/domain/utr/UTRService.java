package hu.kits.tennis.domain.utr;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.List;

import hu.kits.tennis.common.Clock;

public class UTRService {

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
    
    public List<BookedMatch> loadMathesForPlayer(Player player) {
        List<BookedMatch> matches = matchRepository.loadAllPlayedMatches(player);
        
        return matches.stream().map(match -> swapIfNeeed(match, player)).collect(toList());
    }
    
    public List<PlayerWithUTR> calculateUTRRanking() {
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
        
        return ranking.stream()
                .map(playerWithUTR -> new PlayerWithUTR(playerWithUTR.player(), ranking.indexOf(playerWithUTR)+1, playerWithUTR.utr()))
                .collect(toList());
    }

    private static BookedMatch swapIfNeeed(BookedMatch match, Player player) {
        if(match.playedMatch().player1().equals(player)) {
            return match;
        } else {
            return match.swap();            
        }
    }
    
    public void recalculateAllUTRs() {
        
        List<BookedMatch> bookedMatches = matchRepository.loadAllBookedMatches();
        
        List<BookedMatch> recalculatedBookedMatches = UTRCalculator.recalculateAllUTRs(bookedMatches);
        
        matchRepository.replaceAllBookedMatches(recalculatedBookedMatches);
    }

    public List<BookedMatch> loadBookedMatches() {
        return matchRepository.loadAllBookedMatches();
    }

    public void deleteMatch(int id) {
        matchRepository.deleteMatch(id);
    }
    
}
