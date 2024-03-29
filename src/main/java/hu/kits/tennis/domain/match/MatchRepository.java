package hu.kits.tennis.domain.match;

import java.util.List;
import java.util.Map;

import hu.kits.tennis.domain.ktr.BookedMatch;
import hu.kits.tennis.domain.player.Player;
import hu.kits.tennis.domain.tournament.TournamentMatches;

public interface MatchRepository {

    Match loadMatch(int id);
    
    List<BookedMatch> loadAllPlayedMatches(Player player);
    
    List<Match> loadAllMatches(Player player);
    
    List<BookedMatch> loadAllBookedMatchesForTournament(String tournamentId);
    
    List<BookedMatch> loadAllBookedMatches();
    
    BookedMatch save(BookedMatch bookedMatch);
    
    void save(List<Match> playedMatches);
    
    void deleteMatch(int matchId);

    void replaceAllBookedMatches(List<BookedMatch> recalculatedBookedMatches);

    void deleteMatchesForTournament(String id);

    TournamentMatches loadMatchesForTournament(String tournamentId);

    void setResult(MatchResultInfo matchResultInfo);

    void setPlayer1(int matchId, Player player);
    
    void setPlayer2(int matchId, Player player);
    
    void updateTournament(int matchId, String tournamentId, int boardNumber, int tournamentMatchNumber);

    Map<String, Integer> countMatchesByTournament();

}
