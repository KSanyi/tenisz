package hu.kits.tennis.domain.ktr;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.common.Clock;
import hu.kits.tennis.domain.ktr.KTRHistory.KTRHistoryEntry;
import hu.kits.tennis.domain.match.Match;
import hu.kits.tennis.domain.match.MatchInfo;
import hu.kits.tennis.domain.match.MatchRepository;
import hu.kits.tennis.domain.match.MatchService;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.player.PlayerRepository;
import hu.kits.tennis.domain.tournament.TournamentRepository;
import hu.kits.tennis.domain.tournament.TournamentSummary;

public class KTRService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    // TODO
    private final MatchService matchService;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final TournamentRepository tournamentRepository;

    public KTRService(MatchService matchService, MatchRepository matchRepository, PlayerRepository playerRepository, TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
        this.matchService = matchService;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
    }

    public KTRDetails calculatePlayersKTR(Player player) {
        List<BookedMatch> matches = matchRepository.loadAllPlayedMatches(player);
        return KTRCalculator.calculatePlayersKTRDetails(player, matches, Clock.today().plusDays(1), 0);
    }
    
    public BookedMatch calculatKTRAndSaveMatch(Match playedMatch) {
        List<BookedMatch> allBookedMatches = matchRepository.loadAllBookedMatches();
        BookedMatch bookedMatch = KTRCalculator.bookKTRForMatch(playedMatch, allBookedMatches);
        return matchRepository.save(bookedMatch);
    }
    
    public List<PlayerWithKTR> calculateKTRRanking() {
        
        logger.info("Calculating KTR ranking");
        
        List<Player> players = playerRepository.loadAllPlayers().entries();
        List<BookedMatch> allBookedMatches = getAllMatches();
        
        Map<Player, Long> numberOfTrophiesByPlayer = loadNumberOfTrophiesByPlayer();
        
        List<PlayerWithKTR> ranking = players.stream()
                .map(player -> createPlayerWithKTR(player, allBookedMatches, numberOfTrophiesByPlayer.getOrDefault(player, 0L)))
                .sorted(comparing(PlayerWithKTR::ktr).reversed())
                .collect(toList());
        
        List<PlayerWithKTR> result = ranking.stream()
                .map(playerWithKTR -> new PlayerWithKTR(playerWithKTR.player(), ranking.indexOf(playerWithKTR)+1, playerWithKTR.ktr(), playerWithKTR.ktrOneWeekAgo(), playerWithKTR.numberOfMatches(), playerWithKTR.numberOfWins(), playerWithKTR.numberOfTrophies()))
                .collect(toList());
        
        logger.info("KTR ranking calculated with {} entries", result.size());
        
        return result;
    }

    private Map<Player, Long> loadNumberOfTrophiesByPlayer() {
        return tournamentRepository.loadTournamentSummariesList().stream()
                .filter(tournament -> tournament.winner() != null)
                .collect(groupingBy(TournamentSummary::winner, counting()));
    }

    private List<BookedMatch> getAllMatches() {
        //Set<String> kvtkTournamentIds = tournamentRepository.loadAllTournaments().stream()
        //        .map(Tournament::id)
        //        .collect(toSet());
            
        return matchRepository.loadAllBookedMatches().stream()
            //.filter(b -> kvtkTournamentIds.contains(b.playedMatch().tournamentId()))
            .toList();
    }
    
    private static PlayerWithKTR createPlayerWithKTR(Player player, List<BookedMatch> allKVTKBookedMatches, long numberOfTrophies) {
        
        LocalDate tomorrow = Clock.today().plusDays(1); 
        LocalDate oneWeekAgo = Clock.today().minusWeeks(1).plusDays(1);
        
        KTRDetails ktrDetails = KTRCalculator.calculatePlayersKTRDetails(player, allKVTKBookedMatches, tomorrow, (int)numberOfTrophies);
        KTRDetails ktrDetailsOneWekAgo = KTRCalculator.calculatePlayersKTRDetails(player, allKVTKBookedMatches, oneWeekAgo, (int)numberOfTrophies);
        
        return new PlayerWithKTR(player, 0, ktrDetails.ktr(), ktrDetailsOneWekAgo.ktr(), ktrDetails.numberOfMatches(), ktrDetails.numberOfWins(), ktrDetails.numberOfTrophies());
        
    }
    
    public void recalculateAllKTRs() {
        
        logger.info("Recalculating and saving all KTRs");
        
        List<BookedMatch> allBookedMatches = getAllMatches();
        
        List<BookedMatch> recalculatedBookedMatches = KTRCalculator.recalculateAllKTRs(allBookedMatches);
        
        logger.info("Saving recalculated KTRs");
        
        matchRepository.replaceAllBookedMatches(recalculatedBookedMatches);
        
        logger.info("Recalculated KTRs saved");
    }

    public List<BookedMatch> loadBookedMatches() {
        return matchRepository.loadAllBookedMatches().stream()
                .filter(match -> match.playedMatch().isPlayed())
                .sorted(Comparator.comparing((BookedMatch match) -> match.playedMatch().date()).reversed())
                .toList();
    }

    public PlayerStats loadPlayerStats(Player player) {
        
        KTRDetails ktrDetails = calculatePlayersKTR(player);
        
        List<MatchInfo> matchInfos = matchService.loadMatchesForPlayer(player).stream()
                .sorted(Comparator.comparing((MatchInfo matchInfo) -> matchInfo.date()).reversed())
                .collect(toList());
        
        if(matchInfos.isEmpty()) {
            return PlayerStats.create(player, ktrDetails, matchInfos, KTRHistory.EMPTY, 0);
        } else {
            List<PlayerWithKTR> ktrRanking = calculateKTRRanking();
            PlayerWithKTR playerWithKTR = ktrRanking.stream().filter(p -> p.player().equals(player)).findFirst().get();
            int rank = ktrRanking.indexOf(playerWithKTR) + 1;
            
            LocalDate firstMatchDate = matchInfos.get(matchInfos.size()-1).date();
            KTRHistory ktrHistory = calculateKTRHistory(player, firstMatchDate);
            return PlayerStats.create(player, ktrDetails, matchInfos, ktrHistory, rank);
        }
    }

    private KTRHistory calculateKTRHistory(Player player, LocalDate startDate) {
        
        List<BookedMatch> matches = matchRepository.loadAllPlayedMatches(player);
        List<KTRHistoryEntry> historyEntries = new ArrayList<>();
        for(LocalDate date = startDate; !date.isAfter(Clock.today()); date = date.plusDays(1)) {
            KTR ktr = KTRCalculator.calculatePlayersKTRDetails(player, matches, date, 0).ktr();
            historyEntries.add(new KTRHistoryEntry(date, ktr));
        }
        
        return new KTRHistory(historyEntries);
    }
    
}
