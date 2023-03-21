package hu.kits.tennis.domain.utr;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.lang.invoke.MethodHandles;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentInfo;
import hu.kits.tennis.domain.tournament.TournamentRepository;

public class MatchService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private final MatchRepository matchRepository;
    
    private final TournamentRepository tournamentRepository;

    public MatchService(MatchRepository matchRepository, TournamentRepository tournamentRepository) {
        this.matchRepository = matchRepository;
        this.tournamentRepository = tournamentRepository;
    }
    
    public List<MatchInfo> loadMatchesForPlayer(Player player) {
        List<BookedMatch> matches = matchRepository.loadAllPlayedMatches(player);
        
        Map<String, Tournament> tournamenMap = tournamentRepository.loadAllTournaments().stream()
                .collect(toMap(Tournament::id, Function.identity()));
        
        List<MatchInfo> matchInfos = matches.stream()
                .map(match -> player.equals(match.playedMatch().player1()) ? match : match.swap())
                .map(bookedMatch -> toMatchInfo(bookedMatch, tournamenMap))
                .collect(toList());
        
        return matchInfos;
    }

    private static MatchInfo toMatchInfo(BookedMatch bookedMatch, Map<String, Tournament> tournamenMap) {
        
        Tournament tournament = tournamenMap.get(bookedMatch.playedMatch().tournamentId());
        
        TournamentInfo tournamentInfo = tournament != null ? tournament.tournamentInfo() : TournamentInfo.UNKNOWN;
        
        return new MatchInfo(bookedMatch.playedMatch().id(),
                tournamentInfo, 
                bookedMatch.playedMatch().date(), 
                bookedMatch.playedMatch().player1(), 
                bookedMatch.player1UTR(), 
                bookedMatch.playedMatch().player2(), 
                bookedMatch.player2UTR(), 
                bookedMatch.playedMatch().result(), 
                bookedMatch.matchUTRForPlayer1(), 
                bookedMatch.matchUTRForPlayer2(),
                bookedMatch.isUpset());
    }

    public List<MatchInfo> loadAllMatches() {
        List<BookedMatch> matches = matchRepository.loadAllBookedMatches();
        
        Map<String, Tournament> tournamenMap = tournamentRepository.loadAllTournaments().stream()
                .collect(toMap(Tournament::id, Function.identity()));
        
        List<MatchInfo> matchInfos = matches.stream()
                .filter(bookedMatch -> ! bookedMatch.hasPlayed(Player.BYE))
                .map(bookedMatch -> toMatchInfo(bookedMatch, tournamenMap))
                .collect(toList());
        
        return matchInfos;
    }
    
    public void deleteMatch(Match match) {
        matchRepository.deleteMatch(match.id());
        logger.info("Match deleted: {}", match);
    }
    
    public void saveMatch(Match match) {
        BookedMatch bookedMatch = new BookedMatch(match, null, null, null, null);
        matchRepository.save(bookedMatch);
        if(match.id() == null) {
            logger.info("Match saved: {}", match);    
        } else {
            logger.info("Match updated: {}", match);
        }
        
    }

    public List<MatchInfo> loadMatchesOfTournament(String tournementId) {
        List<BookedMatch> matches = matchRepository.loadAllBookedMatchesForTournament(tournementId);
        
        Map<String, Tournament> tournamenMap = tournamentRepository.loadAllTournaments().stream()
                .collect(toMap(Tournament::id, Function.identity()));
        
        List<MatchInfo> matchInfos = matches.stream()
                .map(bookedMatch -> toMatchInfo(bookedMatch, tournamenMap))
                .sorted(Comparator.comparing(MatchInfo::id))
                .collect(toList());
        
        return matchInfos;
    }

    // TODO
    public Optional<Match> findNextMatch(Player player) {
        return matchRepository.loadAllMatches(player).stream()
            .filter(m -> !m.isPlayed())
            .filter(m -> m.arePlayersSet())
            .filter(m -> !m.hasPlayer(Player.BYE))
            .findAny();
    }
    
    public Match loadMatch(int id) {
        return matchRepository.loadMatch(id);
    }

}
