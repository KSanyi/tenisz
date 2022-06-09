package hu.kits.tennis.domain.utr;

import java.util.List;
import java.util.Map;

public interface MatchRepository {

    List<BookedMatch> loadAllPlayedMatches(Player player);
    
    List<BookedMatch> loadAllBookedMatches();
    
    BookedMatch save(BookedMatch bookedMatch);
    
    void deleteMatch(int matchId);

    void replaceAllBookedMatches(List<BookedMatch> recalculatedBookedMatches);

    void deleteMatchesForTournament(String id);

    Map<Integer, Match> loadMatchesForTournament(String tournamentId);

    void setResult(int matchId, MatchResult matchResult);

    void setPlayer1(int matchId, Player player);
    
    void setPlayer2(int matchId, Player player);
}
