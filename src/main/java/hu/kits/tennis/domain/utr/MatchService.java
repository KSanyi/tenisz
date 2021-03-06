package hu.kits.tennis.domain.utr;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import hu.kits.tennis.domain.tournament.Tournament;
import hu.kits.tennis.domain.tournament.TournamentInfo;
import hu.kits.tennis.domain.tournament.TournamentRepository;

public class MatchService {

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
                bookedMatch.matchUTRForPlayer2());
    }

    public List<MatchInfo> loadAllMatches() {
        List<BookedMatch> matches = matchRepository.loadAllBookedMatches();
        
        Map<String, Tournament> tournamenMap = tournamentRepository.loadAllTournaments().stream()
                .collect(toMap(Tournament::id, Function.identity()));
        
        List<MatchInfo> matchInfos = matches.stream()
                .map(bookedMatch -> toMatchInfo(bookedMatch, tournamenMap))
                .collect(toList());
        
        return matchInfos;
    }
    
    public void deleteMatch(int id) {
        matchRepository.deleteMatch(id);
    }
    
    public void saveMatch(Match match) {
        BookedMatch bookedMatch = new BookedMatch(match, null, null, null, null);
        matchRepository.save(bookedMatch);
    }

    public List<MatchInfo> loadMatchesOfTournament(String tournementId) {
        List<BookedMatch> matches = matchRepository.loadAllBookedMatchesForTournament(tournementId);
        
        Map<String, Tournament> tournamenMap = tournamentRepository.loadAllTournaments().stream()
                .collect(toMap(Tournament::id, Function.identity()));
        
        List<MatchInfo> matchInfos = matches.stream()
                .map(bookedMatch -> toMatchInfo(bookedMatch, tournamenMap))
                .collect(toList());
        
        return matchInfos;
    }

}
