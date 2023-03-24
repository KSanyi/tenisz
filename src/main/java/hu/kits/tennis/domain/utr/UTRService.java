package hu.kits.tennis.domain.utr;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.common.Clock;
import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.domain.match.MatchRepository;
import hu.kits.tennis.domain.match.MatchService;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.PlayerRepository;
import hu.kits.tennis.domain.utr.UTRHistory.UTRHistoryEntry;

public class UTRService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    // TODO
    private final MatchService matchService;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    public UTRService(MatchService matchService, MatchRepository matchRepository, PlayerRepository playerRepository) {
        this.matchService = matchService;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
    }

    public UTRDetails calculatePlayersUTR(Player player) {
        List<BookedMatch> matches = matchRepository.loadAllPlayedMatches(player);
        return UTRCalculator.calculatePlayersUTRDetails(player, matches, Clock.today().plusDays(1));
    }
    
    public BookedMatch calculatUTRAndSaveMatch(Match playedMatch) {
        List<BookedMatch> allBookedMatches = matchRepository.loadAllBookedMatches();
        BookedMatch bookedMatch = UTRCalculator.bookUTRForMatch(playedMatch, allBookedMatches);
        return matchRepository.save(bookedMatch);
    }
    
    public List<PlayerWithUTR> calculateUTRRanking() {
        
        logger.info("Calculating UTR ranking");
        
        List<Player> players = playerRepository.loadAllPlayers().entries().stream()
                .toList();
        List<BookedMatch> allBookedMatches = getAllMatches();
        
        List<PlayerWithUTR> ranking = players.stream()
                .map(player -> createPlayerWithUTR(player, allBookedMatches))
                .sorted(comparing(PlayerWithUTR::utr).reversed())
                .collect(toList());
        
        List<PlayerWithUTR> result = ranking.stream()
                .map(playerWithUTR -> new PlayerWithUTR(playerWithUTR.player(), ranking.indexOf(playerWithUTR)+1, playerWithUTR.utr(), playerWithUTR.utrOneWeekAgo(), playerWithUTR.numberOfMatches(), playerWithUTR.numberOfWins()))
                .collect(toList());
        
        logger.info("UTR ranking calculated with {} entries", result.size());
        
        return result;
    }

    private List<BookedMatch> getAllMatches() {
        //Set<String> kvtkTournamentIds = tournamentRepository.loadAllTournaments().stream()
        //        .map(Tournament::id)
        //        .collect(toSet());
            
        return matchRepository.loadAllBookedMatches().stream()
            //.filter(b -> kvtkTournamentIds.contains(b.playedMatch().tournamentId()))
            .toList();
    }
    
    private static PlayerWithUTR createPlayerWithUTR(Player player, List<BookedMatch> allKVTKBookedMatches) {
        
        LocalDate tomorrow = Clock.today().plusDays(1); 
        LocalDate oneWeekAgo = Clock.today().minusWeeks(1).plusDays(1);
        
        UTRDetails utrDetails = UTRCalculator.calculatePlayersUTRDetails(player, allKVTKBookedMatches, tomorrow);
        UTRDetails utrDetailsOneWekAgo = UTRCalculator.calculatePlayersUTRDetails(player, allKVTKBookedMatches, oneWeekAgo);
        
        return new PlayerWithUTR(player, 0, utrDetails.utr(), utrDetailsOneWekAgo.utr(), utrDetails.numberOfMatches(),utrDetails.numberOfWins());
        
    }
    
    public void recalculateAllUTRs() {
        
        logger.info("Recalculating and saving all UTRs");
        
        List<BookedMatch> allBookedMatches = getAllMatches();
        
        List<BookedMatch> recalculatedBookedMatches = UTRCalculator.recalculateAllUTRs(allBookedMatches);
        
        logger.info("Saving recalculated UTRs");
        
        matchRepository.replaceAllBookedMatches(recalculatedBookedMatches);
        
        logger.info("Recalculated UTRs saved");
    }

    public List<BookedMatch> loadBookedMatches() {
        return matchRepository.loadAllBookedMatches().stream()
                .filter(match -> match.playedMatch().isPlayed())
                .sorted(Comparator.comparing((BookedMatch match) -> match.playedMatch().date()).reversed())
                .toList();
    }

    public void deleteMatch(int id) {
        matchRepository.deleteMatch(id);
    }
    
    public PlayerStats loadPlayerStats(Player player) {
        
        UTRDetails utrDetails = calculatePlayersUTR(player);
        
        List<MatchInfo> matchInfos = matchService.loadMatchesForPlayer(player).stream()
                .sorted(Comparator.comparing((MatchInfo matchInfo) -> matchInfo.date()).reversed())
                .collect(toList());
        
        if(matchInfos.isEmpty()) {
            return PlayerStats.create(player, utrDetails, matchInfos, UTRHistory.EMPTY);
        } else {
            LocalDate firstMatchDate = matchInfos.get(matchInfos.size()-1).date();
            UTRHistory utrHistory = calculateUTRHistory(player, firstMatchDate);
            return PlayerStats.create(player, utrDetails, matchInfos, utrHistory);
        }
    }

    private UTRHistory calculateUTRHistory(Player player, LocalDate startDate) {
        
        List<BookedMatch> matches = matchRepository.loadAllPlayedMatches(player);
        List<UTRHistoryEntry> historyEntries = new ArrayList<>();
        for(LocalDate date = startDate; !date.isAfter(Clock.today()); date = date.plusDays(1)) {
            UTR utr = UTRCalculator.calculatePlayersUTRDetails(player, matches, date).utr();
            historyEntries.add(new UTRHistoryEntry(date, utr));
        }
        
        return new UTRHistory(historyEntries);
    }
    
}
