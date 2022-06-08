package hu.kits.tennis.domain.utr;

import java.util.List;

public interface MatchRepository {

    List<BookedMatch> loadAllPlayedMatches(Player player);
    
    List<BookedMatch> loadAllBookedMatches();
    
    BookedMatch save(BookedMatch bookedMatch);
    
    void deleteMatch(int matchId);

    void replaceAllBookedMatches(List<BookedMatch> recalculatedBookedMatches);

    void deleteMatchesForTournament(String id);

    List<Match> loadMatchesForTournament(String tournamentId);
}
